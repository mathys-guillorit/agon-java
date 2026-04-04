package fr.univ.bordeaux.application.match;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** A timer used to manage player reflection time during a game. */
public class GameTimer {
  private long remainingTimeMillis;
  private long lastStartTime;
  private volatile boolean isRunning;

  private final Lock lock = new ReentrantLock();
  private final Condition isRunningCondition = lock.newCondition();

  private final Runnable onTimeout;
  private volatile boolean alive = true;

  /**
   * Initializes and starts a countdown timer.
   *
   * @param time The duration of the timer in minutes.
   * @param onTimeout A callback to be executed when the timer reaches zero.
   * @throws IllegalArgumentException If the provided time is negative.
   */
  public GameTimer(long time, Runnable onTimeout) {
    if (time < 0) {
      throw new IllegalArgumentException("Le temps ne peut pas être négatif : " + time);
    }
    this.remainingTimeMillis = TimeUnit.MINUTES.toMillis(time);
    this.isRunning = false;
    this.onTimeout = onTimeout;
    Thread timerThread = new Thread(this::runTimer);
    timerThread.setDaemon(true);
    timerThread.start();
  }

  /**
   * Constructs a custom timer with a specific time unit and starts the countdown thread.
   *
   * @param time The duration of the timer.
   * @param unit The time unit for the duration (e.g., SECONDS, MINUTES).
   * @param onTimeout The action to perform when the timer expires.
   * @throws IllegalArgumentException If the provided time is negative.
   */
  GameTimer(long time, TimeUnit unit, Runnable onTimeout) {
    if (time < 0) {
      throw new IllegalArgumentException("Le temps ne peut pas être négatif : " + time);
    }
    this.remainingTimeMillis = unit.toMillis(time);
    this.isRunning = false;
    this.onTimeout = onTimeout;

    Thread timerThread = new Thread(this::runTimer);
    timerThread.setDaemon(true);
    timerThread.setName("GameTimer-Custom-Thread");
    timerThread.start();
  }

  private void runTimer() {
    try {
      while (alive) {
        // --- PHASE 1 : ATTENTE (SOUS VERROU) ---
        lock.lock();
        try {
          while (!isRunning && alive) {
            isRunningCondition.await();
          }
        } finally {
          lock.unlock();
        }

        if (!alive) {
          break;
        }
        long currentRemaining = getRemainingTimeMillis();

        if (currentRemaining <= 0) {
          handleTimeout();
        } else {
          Thread.sleep(100);
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Resumes or starts the timer countdown. Signals the internal thread to begin decreasing the
   * remaining time.
   */
  public void start() {
    lock.lock();
    try {
      if (!isRunning && remainingTimeMillis > 0) {
        this.lastStartTime = System.currentTimeMillis();
        this.isRunning = true;
        isRunningCondition.signal();
      }
    } finally {
      lock.unlock();
    }
  }

  /** Pauses the timer countdown. Calculates and saves the remaining time before stopping. */
  public void stop() {
    lock.lock();
    try {
      if (isRunning) {

        this.remainingTimeMillis = getRemainingTimeMillis();
        this.isRunning = false;
      }
    } finally {
      lock.unlock();
    }
  }

  /** Gère l'expiration du temps de manière synchronisée. */
  private void handleTimeout() {
    lock.lock();
    try {
      if (isRunning) {
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
   *
   * @return The remaining time, ensuring it is never negative.
   */
  public long getRemainingTimeMillis() {
    if (isRunning) {
      long elapsedSinceLastStart = System.currentTimeMillis() - lastStartTime;
      return Math.max(0, remainingTimeMillis - elapsedSinceLastStart);
    }
    return remainingTimeMillis;
  }

  /**
   * Permanently terminates the timer thread. Once killed, the timer thread will exit its execution
   * loop.
   */
  public void kill() {
    alive = false;
    lock.lock();
    try {
      isRunningCondition.signal();
    } finally {
      lock.unlock();
    }
  }

  public boolean isExpired() {
    return getRemainingTimeMillis() <= 0;
  }

  /**
   * Formats the remaining time into a human-readable string.
   *
   * @return A string formatted as "MM:SS".
   */
  public String getFormattedRemainingTime() {
    long totalSeconds = getRemainingTimeMillis() / 1000;
    long minutes = totalSeconds / 60;
    long seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  public boolean isRunning() {
    return isRunning;
  }
}
