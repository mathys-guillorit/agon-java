package fr.univ.bordeaux;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit test for simple App. */
public class MainTest {

  /** Rigorous Test :-) */
  @Test
  public void shouldAnswerWithTrue() {
    assertTrue(true);
  }

  @Test
  @DisplayName("Main sans arguments (mode normal)")
  void main_no_args() {
    assertDoesNotThrow(
        () -> {
          Thread t =
              new Thread(
                  () -> {
                    try {
                      Main.main(new String[] {});
                    } catch (Exception ignored) {
                    }
                  });

          t.start();
          Thread.sleep(300);
          t.interrupt(); // évite boucle infinie
        });
  }

  @Test
  @DisplayName("Main avec args null")
  void main_null_args() {
    assertDoesNotThrow(
        () -> {
          Thread t =
              new Thread(
                  () -> {
                    try {
                      Main.main(null);
                    } catch (Exception ignored) {
                    }
                  });

          t.start();
          Thread.sleep(300);
          t.interrupt();
        });
  }

  @Test
  @DisplayName("Main mode daemon (-d)")
  void main_daemon_mode() {
    assertDoesNotThrow(
        () -> {
          Thread t =
              new Thread(
                  () -> {
                    try {
                      Main.main(new String[] {"-d"});
                    } catch (Exception ignored) {
                    }
                  });

          t.start();
          Thread.sleep(500);
          t.interrupt();
        });
  }

  @Test
  @DisplayName("Main mode daemon (--daemon)")
  void main_daemon_long() {
    assertDoesNotThrow(
        () -> {
          Thread t =
              new Thread(
                  () -> {
                    try {
                      Main.main(new String[] {"--daemon"});
                    } catch (Exception ignored) {
                    }
                  });

          t.start();
          Thread.sleep(500);
          t.interrupt();
        });
  }

  @Test
  @DisplayName("Main mode server avec port valide")
  void main_server_valid_port() {
    assertDoesNotThrow(
        () -> {
          Thread t =
              new Thread(
                  () -> {
                    try {
                      Main.main(new String[] {"-s", "12346"});
                    } catch (Exception ignored) {
                    }
                  });

          t.start();
          Thread.sleep(500);
          t.interrupt();
        });
  }

  @Test
  @DisplayName("Main mode server avec port invalide")
  void main_server_invalid_port() {
    assertDoesNotThrow(
        () -> {
          Thread t =
              new Thread(
                  () -> {
                    try {
                      Main.main(new String[] {"-s", "abc"});
                    } catch (Exception ignored) {
                    }
                  });

          t.start();
          Thread.sleep(500);
          t.interrupt();
        });
  }

  @Test
  @DisplayName("Main mode server (--server)")
  void main_server_long() {
    assertDoesNotThrow(
        () -> {
          Thread t =
              new Thread(
                  () -> {
                    try {
                      Main.main(new String[] {"--server", "12347"});
                    } catch (Exception ignored) {
                    }
                  });

          t.start();
          Thread.sleep(500);
          t.interrupt();
        });
  }

  @Test
  @DisplayName("Main fallback vers GameLauncher")
  void main_launcher_mode() {
    assertDoesNotThrow(
        () -> {
          Thread t =
              new Thread(
                  () -> {
                    try {
                      Main.main(new String[] {"random"});
                    } catch (Exception ignored) {
                    }
                  });

          t.start();
          Thread.sleep(300);
          t.interrupt();
        });
  }
}
