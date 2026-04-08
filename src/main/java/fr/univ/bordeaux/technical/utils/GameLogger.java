package fr.univ.bordeaux.technical.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used for application error messages centralization for easy activation/deactivation across all
 * the app. group all information about what is shown in the app.
 *
 * @apiNote do the if here so when pdm send Logger problems it's already treated for the specific
 *     conditional display.
 */
public class GameLogger {
  private static GameLogger instance;
  private final Logger logger;

  private GameLogger() {
    this.logger = Logger.getLogger("AgonGame");

    // FORCE LE SILENCE ABSOLU AU DÉBUT
    this.logger.setUseParentHandlers(false);
    this.logger.setLevel(Level.OFF); // On commence par tout éteindre

    // Nettoyage des handlers existants
    for (Handler h : this.logger.getHandlers()) {
      this.logger.removeHandler(h);
    }

    ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(Level.OFF); // Éteint aussi le handler
    this.logger.addHandler(consoleHandler);
  }

  /**
   * Get the logger object (entrypoint).
   *
   * @return {@link GameLogger}
   */
  public static synchronized GameLogger getInstance() {
    if (instance == null) {
      instance = new GameLogger();
    }
    return instance;
  }

  /** Activated by the option {@code -v} or {@code set verbose=true}. */
  public void setVerbose(boolean enabled) {
    if (enabled) {
      updateLevel(Level.INFO);
    } else {
      updateLevel(Level.WARNING);
    }
  }

  /** Activated by the option {@code -d} or {@code set debug=true}. */
  public void setDebugMode(boolean enabled) {
    if (enabled) {
      updateLevel(Level.FINE); // FINE est le standard Java pour le Debug
    } else {
      updateLevel(Level.WARNING);
    }
  }

  /** Update the logger level AND console handler. */
  private void updateLevel(Level newLevel) {
    this.logger.setLevel(newLevel);
    for (Handler h : this.logger.getHandlers()) {
      h.setLevel(newLevel);
    }
  }

  // --- STATIC LOGGING METHODS ---

  public static void debug(String msg) {
    getInstance().logger.log(Level.FINE, "[DEBUG] " + msg);
  }

  public static void info(String msg) {
    getInstance().logger.log(Level.INFO, "[INFO] " + msg);
  }

  public static void warn(String msg) {
    getInstance().logger.log(Level.WARNING, "[WARN] " + msg);
  }

  public static void error(String msg) {
    getInstance().logger.log(Level.SEVERE, "[ERROR] " + msg);
  }
}
