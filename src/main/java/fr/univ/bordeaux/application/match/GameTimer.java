package fr.univ.bordeaux.application.match;

import java.util.concurrent.TimeUnit;

/** A timer used to manage player reflection time during a game. */
public class GameTimer {

  private long remainingTimeMillis;
  private long lastStartTime;
  private volatile boolean isRunning;
  private Thread timerThread;
  private final Runnable onTimeout;

  /**
   * Constructs a GameTimer with a specific duration.
   *
   * @param time The duration of the timer.
   * @param onTimeout Action to perform when the timer expires.
   */
  public GameTimer(long time, Runnable onTimeout) {
    this.remainingTimeMillis = TimeUnit.MINUTES.toMillis(time);
    this.isRunning = false;
    this.onTimeout = onTimeout;
  }

  /** Starts or resumes the timer in a new background thread. */
  public synchronized void start() {
    if (!isRunning && remainingTimeMillis > 0) {
      this.lastStartTime = System.currentTimeMillis();
      this.isRunning = true;

      timerThread =
          new Thread(
              () -> {
                try {
                  while (isRunning) {
                    if (isExpired()) {
                      synchronized (GameTimer.this) {
                        remainingTimeMillis = 0;
                        isRunning = false;
                      }
                      if (onTimeout != null) {
                        onTimeout.run();
                      }
                      break;
                    }
                    Thread.sleep(100);
                  }
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              });
      timerThread.setDaemon(true);
      timerThread.start();
    }
  }

  /** Stops the timer and calculates the remaining time. */
  public synchronized void stop() {
    if (isRunning) {
      long elapsed = System.currentTimeMillis() - lastStartTime;
      this.remainingTimeMillis -= elapsed;
      this.isRunning = false;
      if (timerThread != null) {
        timerThread.interrupt();
      }
    }
  }

  /**
   * Checks if the timer has expired.
   *
   * @return true if the remaining time is zero or less.
   */
  public boolean isExpired() {
    return getRemainingTimeMillis() <= 0;
  }

  /**
   * Gets the remaining time in milliseconds.
   *
   * @return The remaining time.
   */
  public long getRemainingTimeMillis() {
    if (isRunning) {
      long currentElapsed = System.currentTimeMillis() - lastStartTime;
      return Math.max(0, remainingTimeMillis - currentElapsed);
    }
    return Math.max(0, remainingTimeMillis);
  }

  /**
   * Returns a formatted string representing the remaining time (mm:ss).
   *
   * @return The formatted remaining time.
   */
  public String getFormattedRemainingTime() {
    long totalSeconds = getRemainingTimeMillis() / 1000;
    long minutes = totalSeconds / 60;
    long seconds = totalSeconds % 60;
    return String.format("%02d:%02d", minutes, seconds);
  }

  /**
   * Checks if the timer is currently running.
   *
   * @return true if the timer is running.
   */
  public boolean isRunning() {
    return isRunning;
  }
}
