package fr.univ.bordeaux.application.network.server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServerDiscoveryTest {

  private ServerDiscovery discovery;

  @AfterEach
  void cleanup() {
    try {
      if (discovery != null) discovery.stop();
    } catch (Exception ignored) {
    }
    discovery = null;
  }

  @Test
  @DisplayName("ServerDiscovery starts and stops without crashing")
  void discovery_start_stop() throws Exception {
    discovery = new ServerDiscovery("S", 12345);

    discovery.start();
    discovery.stop();

    assertTrue(true);
  }

  @Test
  @DisplayName("ServerDiscovery start called twice does not crash")
  void discovery_start_twice() throws Exception {
    discovery = new ServerDiscovery("S", 12345);

    discovery.start();
    discovery.start();

    assertTrue(true);
  }

  @Test
  @DisplayName("ServerDiscovery stop called twice does not crash")
  void discovery_stop_twice() {
    discovery = new ServerDiscovery("S", 12345);

    discovery.stop();
    discovery.stop();

    assertTrue(true);
  }
}
