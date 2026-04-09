package fr.univ.bordeaux.application.network.client.runtime;

import fr.univ.bordeaux.technical.utils.GameLogger;

/** Utility class for guarded asynchronous event logging. */
public final class AsyncEventLogger {

  /** Utility class constructor. */
  private AsyncEventLogger() {
    // Prevent instantiation.
  }

  /**
   * Logs an info message if info logging is enabled.
   *
   * @param message message to log
   */
  public static void logInfo(final String message) {
    if (GameLogger.isInfoEnabled()) {
      GameLogger.info(message);
    }
  }

  /**
   * Logs a warning message if warning logging is enabled.
   *
   * @param message message to log
   */
  public static void logWarn(final String message) {
    if (GameLogger.isWarnEnabled()) {
      GameLogger.warn(message);
    }
  }

  /**
   * Logs an error message if error logging is enabled.
   *
   * @param message message to log
   */
  public static void logError(final String message) {
    if (GameLogger.isErrorEnabled()) {
      GameLogger.error(message);
    }
  }
}
