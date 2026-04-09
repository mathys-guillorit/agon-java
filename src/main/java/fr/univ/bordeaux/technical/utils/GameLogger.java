package fr.univ.bordeaux.technical.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class GameLogger {
  private static GameLogger instance;
  private final Logger logger;

  private GameLogger() {
    this.logger = Logger.getLogger("AgonGame");

    this.logger.setUseParentHandlers(false);
    this.logger.setLevel(Level.OFF);
    for (Handler h : this.logger.getHandlers()) {
      this.logger.removeHandler(h);
    }

    ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(Level.OFF);
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
      updateLevel(Level.FINE);
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
