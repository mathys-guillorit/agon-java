package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.technical.utils.GameLogger; // Import ajouté
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread-safe timer used to manage player reflection time during a game.
 * * <p>This timer runs in a background daemon thread. it uses a {@link ReentrantLock}
 * and a {@link Condition} to efficiently pause and resume countdowns without
 * busy-waiting. When the time reaches zero, a specified callback is executed.</p>
 */
public class GameTimer {
  /** The remaining time in milliseconds, updated whenever the timer stops. */
  private long remainingTimeMillis;

  /** The system timestamp (ms) recorded when the timer was last started. */
  private long lastStartTime;

  /** Indicates whether the timer is currently counting down. */
  private volatile boolean isRunning;

  /** Lock for synchronizing access to timer state and controlling the background thread. */
  private final Lock lock = new ReentrantLock();

  /** Condition used to make the background thread wait while the timer is paused. */
  private final Condition isRunningCondition = lock.newCondition();

  /** Callback to execute when the timer expires. */
  private final Runnable onTimeout;

  /** Control flag to stop the background thread permanently. */
  private volatile boolean alive = true;

  /**
   * Constructs a GameTimer with a specified duration in minutes.
   *
   * @param time The initial time in minutes. Must be non-negative.
   * @param onTimeout The action to perform when time runs out.
   * @throws IllegalArgumentException if time is negative.
   */
  public GameTimer(long time, Runnable onTimeout) {
    if (time < 0) {
      throw new IllegalArgumentException("Time cannot be negative: " + time);
    }
    this.remainingTimeMillis = TimeUnit.MINUTES.toMillis(time);
    this.isRunning = false;
    this.onTimeout = onTimeout;

    GameLogger.info("GameTimer: Created with " + time + " minutes.");

    Thread timerThread = new Thread(this::runTimer);
    timerThread.setDaemon(true);
    timerThread.start();
  }

  /**
   * Test Constructor allowing custom time units.
   *
   * @param time The initial time value.
   * @param unit The {@link TimeUnit} for the duration.
   * @param onTimeout The action to perform when time runs out.
   * @throws IllegalArgumentException if time is negative.
   */
  GameTimer(long time, TimeUnit unit, Runnable onTimeout) {
    if (time < 0) {
      throw new IllegalArgumentException("Time cannot be negative: " + time);
    }
    this.remainingTimeMillis = unit.toMillis(time);
    this.isRunning = false;
    this.onTimeout = onTimeout;

    GameLogger.info("GameTimer: Created with custom duration (" + remainingTimeMillis + " ms).");

    Thread timerThread = new Thread(this::runTimer);
    timerThread.setDaemon(true);
    timerThread.setName("GameTimer-Custom-Thread");
    timerThread.start();
  }

  /**
   * The main loop for the background thread.
   * <p>Handles the waiting phase when paused and periodically checks
   * for expiration when running.</p>
   */
  private void runTimer() {
    try {
      while (alive) {
        lock.lock();
        try {
          while (!isRunning && alive) {
            GameLogger.debug("GameTimer Thread: Entering await state.");
            isRunningCondition.await();
          }
        } finally {
          lock.unlock();
        }

        if (!alive) break;
        long currentRemaining = getRemainingTimeMillis();

        if (currentRemaining <= 0) {
          handleTimeout();
        } else {
          // On vérifie toutes les 100ms pour être précis sur l'affichage
          Thread.sleep(100);
        }
      }
    } catch (InterruptedException e) {
      GameLogger.error("GameTimer Thread: Interrupted! " + e.getMessage());
      Thread.currentThread().interrupt();
    }
    GameLogger.debug("GameTimer Thread: Background thread exiting.");
  }

  /**
   * Starts or resumes the timer.
   * <p>Records the start time and signals the background thread to begin
   * checking the remaining duration.</p>
   */
  public void start() {
    lock.lock();
    try {
      if (!isRunning && remainingTimeMillis > 0) {
        this.lastStartTime = System.currentTimeMillis();
        this.isRunning = true;
        GameLogger.debug("GameTimer: Started/Resumed. Signal sent to background thread.");
        isRunningCondition.signal();
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Stops (pauses) the timer.
   * <p>Calculates and saves the exact remaining time so it can be
   * resumed later from the same point.</p>
   */
  public void stop() {
    lock.lock();
    try {
      if (isRunning) {
        this.remainingTimeMillis = getRemainingTimeMillis();
        this.isRunning = false;
        GameLogger.debug("GameTimer: Stopped. Remaining: " + getFormattedRemainingTime());
      }
    } finally {
      lock.unlock();
    }
  }

  /** * Handles time expiration in a synchronized manner.
   * <p>Stops the countdown, sets remaining time to zero, and executes
   * the timeout callback.</p>
   */
  private void handleTimeout() {
    lock.lock();
    try {
      if (isRunning) {
        GameLogger.info("GameTimer: EXPIRED! Executing timeout callback.");
        this.remainingTimeMillis = 0;
        this.isRunning = false;
        if (onTimeout != null) {
          onTimeout.run();
        }
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Calculates the current remaining time in milliseconds.
   * * @return The remaining time (ms). Returns 0 if time has expired.
   */
  public long getRemainingTimeMillis() {
    if (isRunning) {
      long elapsedSinceLastStart = System.currentTimeMillis() - lastStartTime;
      return Math.max(0, remainingTimeMillis - elapsedSinceLastStart);
    }
    return remainingTimeMillis;
  }

  /**
   * Permanently kills the timer thread.
   * <p>Sets the alive flag to false and signals the condition to release
   * the background thread from its waiting state.</p>
   */
  public void kill() {
    GameLogger.info("GameTimer: Killing background thread.");
    alive = false;
    lock.lock();
    try {
      isRunningCondition.signal();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Checks if the timer has reached zero.
   *
   * @return {@code true} if the time is up, {@code false} otherwise.
   */
  public boolean isExpired() {
    return getRemainingTimeMillis() <= 0;
  }

  /**
   * Returns the remaining time as a formatted string.
   *
   * @return A string in "mm:ss" format.
   */
  public String getFormattedRemainingTime() {
    long totalSeconds = getRemainingTimeMillis() / 1000;
    long minutes = totalSeconds / 60;
    long seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  /**
   * Returns the current running state of the timer.
   *
   * @return {@code true} if running, {@code false} if paused or killed.
   */
  public boolean isRunning() {
    return isRunning;
  }
}