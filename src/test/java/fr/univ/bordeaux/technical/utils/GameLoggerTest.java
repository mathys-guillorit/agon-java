package fr.univ.bordeaux.technical.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameLoggerTest {

  private GameLogger loggerInstance;

  @BeforeEach
  void setUp() {
    // On récupère l'instance
    loggerInstance = GameLogger.getInstance();
    // On reset l'état pour chaque test (Silence total)
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
        loggerInstance, GameLogger.getInstance(), "Le Singleton doit retourner la même instance");
  }

  @Test
  void testSetVerboseEnabled() {
    loggerInstance.setVerbose(true);
    // On récupère le logger interne via réflexion ou on vérifie indirectement le niveau
    Logger internalLogger = Logger.getLogger("AgonGame");
    assertEquals(Level.INFO, internalLogger.getLevel(), "Le niveau doit être INFO en mode verbose");
  }

  @Test
  void testSetVerboseDisabled() {
    loggerInstance.setVerbose(false);
    Logger internalLogger = Logger.getLogger("AgonGame");
    assertEquals(
        Level.WARNING,
        internalLogger.getLevel(),
        "Le niveau doit revenir à WARNING si verbose est false");
  }

  @Test
  void testSetDebugModeEnabled() {
    loggerInstance.setDebugMode(true);
    Logger internalLogger = Logger.getLogger("AgonGame");
    assertEquals(Level.FINE, internalLogger.getLevel(), "Le niveau doit être FINE en mode debug");
  }

  @Test
  void testSetDebugModeDisabled() {
    loggerInstance.setDebugMode(false);
    Logger internalLogger = Logger.getLogger("AgonGame");
    assertEquals(
        Level.WARNING,
        internalLogger.getLevel(),
        "Le niveau doit revenir à WARNING si debug est false");
  }

  @Test
  void testLogMethodsExecution() {
    // On teste que l'appel aux méthodes statiques ne crash pas (Coverage des méthodes de log)
    // Comme on a reset en début de test, ces messages ne s'afficheront pas en console (Level
    // WARNING)
    assertDoesNotThrow(
        () -> {
          GameLogger.debug("Message de debug");
          GameLogger.info("Message d'info");
          GameLogger.warn("Message de warning");
          GameLogger.error("Message d'erreur");
        });
  }

  @Test
  void testUpdateLevelWithMultipleHandlers() {
    // Pour couvrir la boucle for (Handler h : this.logger.getHandlers())
    // On vérifie que les handlers suivent bien le changement de niveau
    loggerInstance.setDebugMode(true);
    Logger internalLogger = Logger.getLogger("AgonGame");

    java.util.logging.Handler[] handlers = internalLogger.getHandlers();
    assertTrue(handlers.length > 0);
    for (java.util.logging.Handler h : handlers) {
      assertEquals(
          Level.FINE, h.getLevel(), "Le handler doit être synchronisé avec le niveau du logger");
    }
  }
}
