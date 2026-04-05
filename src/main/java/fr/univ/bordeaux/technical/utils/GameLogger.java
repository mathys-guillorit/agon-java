package fr.univ.bordeaux.technical.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Handler;

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
  public static synchronized GameLogger getInstance() {
    if (instance == null) {
      instance = new GameLogger();
    }
    return instance;
  }

  /** Activé par l'option -v ou set verbose=true */
  public void setVerbose(boolean enabled) {
    if (enabled) {
      updateLevel(Level.INFO);
    } else {
      updateLevel(Level.WARNING);
    }
  }

  /** Activé par l'option -d ou set debug=true */
  public void setDebugMode(boolean enabled) {
    if (enabled) {
      updateLevel(Level.FINE); // FINE est le standard Java pour le Debug
    } else {
      updateLevel(Level.WARNING);
    }
  }

  /** Met à jour le niveau du Logger ET du Handler Console */
  private void updateLevel(Level newLevel) {
    this.logger.setLevel(newLevel);
    for (Handler h : this.logger.getHandlers()) {
      h.setLevel(newLevel);
    }
  }

  // --- MÉTHODES DE LOG STATIQUES ---

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