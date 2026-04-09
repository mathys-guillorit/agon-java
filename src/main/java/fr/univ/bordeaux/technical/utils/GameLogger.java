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
 */
public final class GameLogger {
  private static GameLogger instance;
  private final Logger logger;

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
   * @return the unique GameLogger instance
   */
  public static synchronized GameLogger getInstance() {
    if (instance == null) {
      instance = new GameLogger();
    }
    return instance;
  }

  /** Enables or disables verbose mode. */
  public void setVerbose(final boolean enabled) {
    if (enabled) {
      updateLevel(Level.INFO);
    } else {
      updateLevel(Level.WARNING);
    }
  }

  /** Enables or disables debug mode. */
  public void setDebugMode(final boolean enabled) {
    if (enabled) {
      updateLevel(Level.FINE);
    } else {
      updateLevel(Level.WARNING);
    }
  }

  /** Updates the logger and handler levels. */
  private void updateLevel(final Level newLevel) {
    this.logger.setLevel(newLevel);
    for (Handler handler : this.logger.getHandlers()) {
      handler.setLevel(newLevel);
    }
  }

  /** Returns true if DEBUG/FINE logs are enabled. */
  public static boolean isDebugEnabled() {
    return getInstance().logger.isLoggable(Level.FINE);
  }

  /** Returns true if INFO logs are enabled. */
  public static boolean isInfoEnabled() {
    return getInstance().logger.isLoggable(Level.INFO);
  }

  /** Returns true if WARNING logs are enabled. */
  public static boolean isWarnEnabled() {
    return getInstance().logger.isLoggable(Level.WARNING);
  }

  /** Returns true if ERROR/SEVERE logs are enabled. */
  public static boolean isErrorEnabled() {
    return getInstance().logger.isLoggable(Level.SEVERE);
  }

  /** Logs a debug message. */
  public static void debug(final String message) {
    getInstance().logger.log(Level.FINE, "[DEBUG] " + message);
  }

  /** Logs an info message. */
  public static void info(final String message) {
    getInstance().logger.log(Level.INFO, "[INFO] " + message);
  }

  /** Logs a warning message. */
  public static void warn(final String message) {
    getInstance().logger.log(Level.WARNING, "[WARN] " + message);
  }

  /** Logs an error message. */
  public static void error(final String message) {
    getInstance().logger.log(Level.SEVERE, "[ERROR] " + message);
  }
}
