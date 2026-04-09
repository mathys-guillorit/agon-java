package fr.univ.bordeaux.application.network.client.runtime;

import fr.univ.bordeaux.technical.utils.GameLogger;
import java.util.function.Consumer;

/** Utility class for guarded asynchronous event logging. */
public final class AsyncEventLogger {

  /** The observer that directly links network events to the Graphical User Interface (GUI). */
  private static Consumer<String> eventObserver;

  /** Utility class constructor. */
  private AsyncEventLogger() {
    // Prevent instantiation.
  }

  public static void setEventObserver(Consumer<String> observer) {
    eventObserver = observer;
  }

  /**
   * Logs an info message if info logging is enabled.
   *
   * @param message message to log
   */
  public static void logInfo(final String message) {
    if (eventObserver != null) {
      eventObserver.accept(message);
    }

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
    if (eventObserver != null) {
      eventObserver.accept(message);
    }

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
    if (eventObserver != null) {
      eventObserver.accept(message);
    }

    if (GameLogger.isErrorEnabled()) {
      GameLogger.error(message);
    }
  }
}
