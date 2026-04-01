package fr.univ.bordeaux.application.match;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GameTimerTest {

  @Test
  @DisplayName("Test du constructeur et état initial")
  void testConstructor() {
    GameTimer timer = new GameTimer(5, null);
    assertEquals(300000, timer.getRemainingTimeMillis(), "5 minutes devraient être 300 000 ms");
    assertFalse(timer.isRunning());
    assertFalse(timer.isExpired());
    assertEquals("05:00", timer.getFormattedRemainingTime());
  }

  @Test
  @DisplayName("Test start et stop standard")
  void testStartStop() throws InterruptedException {
    GameTimer timer = new GameTimer(1, null);
    timer.start();
    assertTrue(timer.isRunning());

    // On attend un peu pour voir le temps s'écouler
    Thread.sleep(200);
    long remaining = timer.getRemainingTimeMillis();
    assertTrue(remaining < 60000 && remaining > 59000, "Le temps devrait avoir diminué");

    timer.stop();
    assertFalse(timer.isRunning());
    long afterStop = timer.getRemainingTimeMillis();

    Thread.sleep(100);
    assertEquals(
        afterStop, timer.getRemainingTimeMillis(), "Le temps ne doit plus couler après stop");
  }

  @Test
  @DisplayName("Test start sur un timer déjà lancé ou vide")
  void testStartEdgeCases() {
    GameTimer timer = new GameTimer(1, null);
    timer.start();
    assertTrue(timer.isRunning());
    timer.start(); // Ne devrait rien faire de plus
    assertTrue(timer.isRunning());

    GameTimer emptyTimer = new GameTimer(0, null);
    emptyTimer.start();
    assertFalse(emptyTimer.isRunning(), "Un timer à 0 ne devrait pas démarrer");
  }

  @Test
  @DisplayName("Test stop sur un timer déjà arrêté")
  void testStopEdgeCases() {
    GameTimer timer = new GameTimer(1, null);
    timer.stop(); // Ne devrait pas crash
    assertFalse(timer.isRunning());
  }

  @Test
  @DisplayName("Test du formatage du temps")
  void testFormatting() throws Exception {
    GameTimer timer = new GameTimer(1, null);
    // On force le temps restant à 65 secondes via réflexion pour tester mm:ss
    setPrivateField(timer, "remainingTimeMillis", 65000L);
    assertEquals("01:05", timer.getFormattedRemainingTime());

    setPrivateField(timer, "remainingTimeMillis", 10000L);
    assertEquals("00:10", timer.getFormattedRemainingTime());

    setPrivateField(timer, "remainingTimeMillis", 0L);
    assertEquals("00:00", timer.getFormattedRemainingTime());
  }

  @Test
  @DisplayName("Test de l'expiration et du callback onTimeout")
  void testTimeout() throws Exception {
    AtomicBoolean timeoutCalled = new AtomicBoolean(false);
    GameTimer timer = new GameTimer(1, () -> timeoutCalled.set(true));

    // On réduit le temps restant à 100ms pour ne pas attendre 1 minute
    setPrivateField(timer, "remainingTimeMillis", 100L);

    timer.start();
    assertTrue(timer.isRunning());

    // On attend que le thread de surveillance détecte l'expiration (check toutes les 100ms)
    // On donne un peu de marge (500ms)
    Thread.sleep(500);

    assertFalse(timer.isRunning(), "Le timer devrait s'arrêter de lui-même");
    assertTrue(timer.isExpired(), "Le timer devrait être expiré");
    assertTrue(timeoutCalled.get(), "Le callback onTimeout devrait avoir été appelé");
  }

  @Test
  @DisplayName("Test de la sécurité getRemainingTimeMillis (pas de négatif)")
  void testNoNegativeTime() throws Exception {
    GameTimer timer = new GameTimer(1, null);
    setPrivateField(timer, "remainingTimeMillis", -5000L);
    assertEquals(0, timer.getRemainingTimeMillis());
    assertTrue(timer.isExpired());

    // Test pendant qu'il tourne
    setPrivateField(timer, "remainingTimeMillis", 10L);
    timer.start();
    Thread.sleep(100);
    assertEquals(0, timer.getRemainingTimeMillis());
  }

  /** Utilitaire pour modifier les champs privés sans changer le code source. */
  private void setPrivateField(Object obj, String fieldName, Object value) throws Exception {
    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(obj, value);
  }
}
