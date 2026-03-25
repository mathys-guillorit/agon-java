package fr.univ.bordeaux.application.match;

public class GameTimer {

  private long remainingTimeMillis;
  private long lastStartTime;
  private boolean isRunning;

  public GameTimer(long initialTimeMillis) {
    this.remainingTimeMillis = initialTimeMillis;
    this.isRunning = false;
  }

  /** Lance ou reprend le chronomètre. */
  public void start() {
    if (!isRunning) {
      this.lastStartTime = System.currentTimeMillis();
      this.isRunning = true;
    }
  }

  /** Arrête le chronomètre et déduit le temps écoulé. */
  public void stop() {
    if (isRunning) {
      long elapsed = System.currentTimeMillis() - lastStartTime;
      this.remainingTimeMillis -= elapsed;
      this.isRunning = false;
    }
  }

  /** Vérifie si le temps est écoulé (même pendant que le chrono tourne). */
  public boolean isExpired() {
    if (isRunning) {
      long currentElapsed = System.currentTimeMillis() - lastStartTime;
      return (remainingTimeMillis - currentElapsed) <= 0;
    }
    return remainingTimeMillis <= 0;
  }

  public long getRemainingTimeMillis() {
    if (isRunning) {
      long currentElapsed = System.currentTimeMillis() - lastStartTime;
      return Math.max(0, remainingTimeMillis - currentElapsed);
    }
    return Math.max(0, remainingTimeMillis);
  }
}
