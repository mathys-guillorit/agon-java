package fr.univ.bordeaux.application.match;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
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
  @DisplayName("Test de l'expiration réelle avec 1 seconde")
  void testRealTimeoutInSeconds() throws InterruptedException {
    AtomicBoolean timeoutCalled = new AtomicBoolean(false);

    // On crée un timer de 1 SECONDE (possible grâce au nouveau constructeur)
    GameTimer timer = new GameTimer(1, TimeUnit.SECONDS, () -> timeoutCalled.set(true));

    timer.start();
    assertTrue(timer.isRunning());

    // On attend 1.5s (le temps que le thread sleep(100) détecte la fin)
    Thread.sleep(1500);

    assertFalse(timer.isRunning(), "Le timer devrait être arrêté");
    assertTrue(timer.isExpired(), "Le timer devrait être expiré");
    assertTrue(timeoutCalled.get(), "Le callback doit avoir été exécuté");
  }

  @Test
  @DisplayName("Test du formatage avec des petites valeurs")
  void testFormattingSeconds() {
    // 90 secondes = 01:30
    GameTimer timer = new GameTimer(90, TimeUnit.SECONDS, null);
    assertEquals("01:30", timer.getFormattedRemainingTime());

    // 10 secondes = 00:10
    GameTimer timer2 = new GameTimer(10, TimeUnit.SECONDS, null);
    assertEquals("00:10", timer2.getFormattedRemainingTime());
  }

  @Test
  @DisplayName("Le constructeur doit empêcher un temps négatif")
    void testNegativeTimeConstructor() {

      assertThrows(IllegalArgumentException.class, () -> {
        new GameTimer(-10, TimeUnit.SECONDS, null);
      }, "Le constructeur aurait dû rejeter un temps de -10s");
    }


  @Test
  @DisplayName("Test de la transition vers zéro pendant l'exécution")
  void testTransitionToZero() throws InterruptedException {
    // On crée un timer très court (10ms)
    GameTimer timer = new GameTimer(10, TimeUnit.MILLISECONDS, null);

    timer.start();

    // On attend 150ms pour être sûr qu'il a largement dépassé le temps
    Thread.sleep(150);

    long remaining = timer.getRemainingTimeMillis();

    // Vérification de la sécurité Math.max(0, ...)
    assertEquals(0, remaining, "Le temps restant ne doit jamais être négatif");
    assertTrue(timer.isExpired());
    assertFalse(timer.isRunning(), "Le thread doit avoir arrêté le timer après le timeout");
  }
}
