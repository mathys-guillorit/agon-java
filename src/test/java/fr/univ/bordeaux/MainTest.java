package fr.univ.bordeaux;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MainTest {

  private static final long SHORT_DELAY_MS = 300L;
  private static final long SERVER_DELAY_MS = 500L;

  @Test
  @DisplayName("Main sans arguments (mode normal)")
  void mainNoArgs() {
    assertMainDoesNotThrow(new String[] {}, SHORT_DELAY_MS);
  }

  @Test
  @DisplayName("Main avec args null")
  void mainNullArgs() {
    assertMainDoesNotThrow(null, SHORT_DELAY_MS);
  }

  @Test
  @DisplayName("Main mode daemon (-d)")
  void mainDaemonMode() {
    assertMainDoesNotThrow(new String[] {"-d"}, SERVER_DELAY_MS);
  }

  @Test
  @DisplayName("Main mode daemon (--daemon)")
  void mainDaemonLongMode() {
    assertMainDoesNotThrow(new String[] {"--daemon"}, SERVER_DELAY_MS);
  }

  @Test
  @DisplayName("Main mode server avec port valide")
  void mainServerWithValidPort() {
    assertMainDoesNotThrow(new String[] {"-s", "12346"}, SERVER_DELAY_MS);
  }

  @Test
  @DisplayName("Main mode server avec port invalide")
  void mainServerWithInvalidPort() {
    assertMainDoesNotThrow(new String[] {"-s", "abc"}, SERVER_DELAY_MS);
  }

  @Test
  @DisplayName("Main mode server (--server)")
  void mainServerLongMode() {
    assertMainDoesNotThrow(new String[] {"--server", "12347"}, SERVER_DELAY_MS);
  }

  @Test
  @DisplayName("Main fallback vers GameLauncher")
  void mainLauncherMode() {
    assertMainDoesNotThrow(new String[] {"random"}, SHORT_DELAY_MS);
  }

  private void assertMainDoesNotThrow(final String[] args, final long waitTimeMs) {
    assertDoesNotThrow(
        () -> {
          final AtomicReference<Throwable> thrown = new AtomicReference<>();
          final InputStream originalIn = System.in;

          try {
            System.setIn(new RepeatingInputStream("Test\n1\n"));

            final Thread thread =
                new Thread(
                    () -> {
                      try {
                        Main.main(args);
                      } catch (Throwable throwable) {
                        thrown.set(throwable);
                      }
                    });

            thread.setDaemon(true);
            thread.start();

            Thread.sleep(waitTimeMs);
            thread.interrupt();

            assertNull(thrown.get());
          } finally {
            System.setIn(originalIn);
          }
        });
  }

  /**
   * Flux d'entrée qui répète indéfiniment le même contenu. Utile quand plusieurs Scanner lisent sur
   * System.in.
   */
  private static final class RepeatingInputStream extends InputStream {
    private final byte[] data;
    private int index;

    private RepeatingInputStream(final String content) {
      this.data = content.getBytes(StandardCharsets.UTF_8);
      this.index = 0;
    }

    @Override
    public int read() throws IOException {
      if (data.length == 0) {
        return -1;
      }

      final int value = data[index] & 0xFF;
      index = (index + 1) % data.length;
      return value;
    }
  }
}
