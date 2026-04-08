package fr.univ.bordeaux.technical.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameLoggerTest {

  private GameLogger loggerInstance;

  @BeforeEach
  void setUp() {
    loggerInstance = GameLogger.getInstance();
    loggerInstance.setDebugMode(false);
    loggerInstance.setVerbose(false);
  }

  @AfterAll
  static void tearDown() {
    GameLogger.getInstance().setVerbose(false);
    GameLogger.getInstance().setDebugMode(false);
  }

  @Test
  void testSingletonInstance() {
    assertNotNull(loggerInstance);
    assertSame(
        loggerInstance, GameLogger.getInstance(), "The Singleton must return the same instance");
  }

  @Test
  void testSetVerboseEnabled() {
    loggerInstance.setVerbose(true);
    Logger internalLogger = Logger.getLogger("AgonGame");
    assertEquals(Level.INFO, internalLogger.getLevel(), "The level should be INFO in verbose mode");
  }

  @Test
  void testSetVerboseDisabled() {
    loggerInstance.setVerbose(false);
    Logger internalLogger = Logger.getLogger("AgonGame");
    assertEquals(
        Level.WARNING,
        internalLogger.getLevel(),
        "The level should revert to WARNING if verbose is false");
  }

  @Test
  void testSetDebugModeEnabled() {
    loggerInstance.setDebugMode(true);
    Logger internalLogger = Logger.getLogger("AgonGame");
    assertEquals(Level.FINE, internalLogger.getLevel(), "The level should be FINE in debug mode");
  }

  @Test
  void testSetDebugModeDisabled() {
    loggerInstance.setDebugMode(false);
    Logger internalLogger = Logger.getLogger("AgonGame");
    assertEquals(
        Level.WARNING,
        internalLogger.getLevel(),
        "The level should revert to WARNING if debug is false");
  }

  @Test
  void testLogMethodsExecution() {
    assertDoesNotThrow(
        () -> {
          GameLogger.debug("Debug message");
          GameLogger.info("Info message");
          GameLogger.warn("Warning message");
          GameLogger.error("Error message");
        });
  }

  @Test
  void testUpdateLevelWithMultipleHandlers() {
    loggerInstance.setDebugMode(true);
    Logger internalLogger = Logger.getLogger("AgonGame");

    Handler[] handlers = internalLogger.getHandlers();
    assertTrue(handlers.length > 0, "Logger should have at least one handler");
    for (Handler h : handlers) {
      assertEquals(
          Level.FINE, h.getLevel(), "The handler must be synchronized with the logger level");
    }
  }
}