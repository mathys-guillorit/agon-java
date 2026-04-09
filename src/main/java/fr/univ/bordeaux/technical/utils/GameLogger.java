package fr.univ.bordeaux.technical.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized logger utility for the Agon game.
 *
 * <p>This class wraps {@link Logger} to provide configurable logging levels (debug, verbose,
 * warning, error) and ensures consistent formatting across the application.
 * It follows the Singleton pattern to ensure a unique logging context.
 */
public final class GameLogger {

  /** The unique singleton instance of the GameLogger. */
  private static GameLogger instance;

  /** The internal Java Logger instance. */
  private final Logger logger;

  /**
   * Private constructor to initialize the logger with a custom ConsoleHandler.
   * Disables parent handlers to avoid duplicate logs in the console.
   */
  private GameLogger() {
    this.logger = Logger.getLogger("AgonGame");
    this.logger.setUseParentHandlers(false);
    this.logger.setLevel(Level.OFF);

    for (Handler handler : this.logger.getHandlers()) {
      this.logger.removeHandler(handler);
    }

    final ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(Level.OFF);
    this.logger.addHandler(consoleHandler);
  }

  /**
   * Returns the singleton instance of the GameLogger.
   *
   * @return The unique {@link GameLogger} instance.
   */
  public static synchronized GameLogger getInstance() {
    if (instance == null) {
      instance = new GameLogger();
    }
    return instance;
  }

  /**
   * Enables or disables verbose mode.
   *  @param enabled If true, sets the level to INFO; otherwise, sets it to WARNING.
   */
  public void setVerbose(final boolean enabled) {
    if (enabled) {
      updateLevel(Level.INFO);
    } else {
      updateLevel(Level.WARNING);
    }
  }

  /**
   * Enables or disables debug mode.
   * @param enabled If true, sets the level to FINE; otherwise, sets it to WARNING.
   */
  public void setDebugMode(final boolean enabled) {
    if (enabled) {
      updateLevel(Level.FINE);
    } else {
      updateLevel(Level.WARNING);
    }
  }

  /**
   * Updates the logging level for both the logger and all its attached handlers.
   * @param newLevel The new {@link Level} to apply.
   */
  private void updateLevel(final Level newLevel) {
    this.logger.setLevel(newLevel);
    for (Handler handler : this.logger.getHandlers()) {
      handler.setLevel(newLevel);
    }
  }

  /**
   * Checks if DEBUG (FINE) logs are currently enabled.
   * @return {@code true} if loggable at FINE level, {@code false} otherwise.
   */
  public static boolean isDebugEnabled() {
    return getInstance().logger.isLoggable(Level.FINE);
  }

  /**
   * Checks if INFO logs are currently enabled.
   * @return {@code true} if loggable at INFO level, {@code false} otherwise.
   */
  public static boolean isInfoEnabled() {
    return getInstance().logger.isLoggable(Level.INFO);
  }

  /**
   * Checks if WARNING logs are currently enabled.
   * @return {@code true} if loggable at WARNING level, {@code false} otherwise.
   */
  public static boolean isWarnEnabled() {
    return getInstance().logger.isLoggable(Level.WARNING);
  }

  /**
   * Checks if ERROR (SEVERE) logs are currently enabled.
   * @return {@code true} if loggable at SEVERE level, {@code false} otherwise.
   */
  public static boolean isErrorEnabled() {
    return getInstance().logger.isLoggable(Level.SEVERE);
  }

  /**
   * Logs a message at the DEBUG (FINE) level.
   * @param message The message to log.
   */
  public static void debug(final String message) {
    getInstance().logger.log(Level.FINE, "[DEBUG] " + message);
  }

  /**
   * Logs a message at the INFO level.
   * @param message The message to log.
   */
  public static void info(final String message) {
    getInstance().logger.log(Level.INFO, "[INFO] " + message);
  }

  /**
   * Logs a message at the WARNING level.
   * @param message The message to log.
   */
  public static void warn(final String message) {
    getInstance().logger.log(Level.WARNING, "[WARN] " + message);
  }

  /**
   * Logs a message at the ERROR (SEVERE) level.
   * @param message The message to log.
   */
  public static void error(final String message) {
    getInstance().logger.log(Level.SEVERE, "[ERROR] " + message);
  }
}