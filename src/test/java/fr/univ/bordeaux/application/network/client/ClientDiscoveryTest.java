package fr.univ.bordeaux.application.network.client;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.network.server.ServerDiscovery;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClientDiscoveryTest {

  private static final long POLL_INTERVAL_MS = 20;
  private static final long DISCOVERY_TIMEOUT_MS = 3000;
  private static final long STARTUP_DELAY_MS = 200;
  private static final long INVALID_PACKET_WAIT_MS = 100;

  private ClientDiscovery discovery;

  @AfterEach
  void cleanup() {
    if (discovery != null) {
      discovery.stop();
    }
  }

  private void sendUdp(String payload) throws Exception {
    try (DatagramSocket socket = new DatagramSocket()) {
      byte[] data = payload.getBytes();
      socket.send(
          new DatagramPacket(
              data, data.length, InetAddress.getByName("127.0.0.1"), ClientDiscovery.UDP_PORT));
    }
  }

  private void waitUntil(BooleanSupplier condition, long timeoutMs) throws Exception {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < deadline) {
      if (condition.getAsBoolean()) {
        return;
      }
      Thread.sleep(POLL_INTERVAL_MS);
    }
    fail("Condition non atteinte dans le délai");
  }

  @Test
  @DisplayName("Discovery finds a broadcasted server")
  void discovery_find_server() throws Exception {
    discovery = new ClientDiscovery();
    discovery.start();

    ServerDiscovery server = new ServerDiscovery("TestServer", 9999);
    server.start();

    try {
      Thread.sleep(STARTUP_DELAY_MS);
      waitUntil(() -> !discovery.getServers().isEmpty(), DISCOVERY_TIMEOUT_MS);

      List<ServerInfo> servers = discovery.getServers();
      assertFalse(servers.isEmpty());
    } finally {
      server.stop();
    }
  }

  @Test
  @DisplayName("Invalid UDP payload is ignored")
  void invalid_packet_is_ignored() throws Exception {
    discovery = new ClientDiscovery();
    discovery.start();

    sendUdp("invalid-payload-without-required-format");
    Thread.sleep(INVALID_PACKET_WAIT_MS);

    assertTrue(discovery.getServers().isEmpty());
  }

  @Test
  @DisplayName("Stop is safe before start and after start")
  void stop_is_safe() {
    discovery = new ClientDiscovery();

    assertDoesNotThrow(
        () -> {
          discovery.stop();
          discovery.start();
          discovery.stop();
          discovery.stop();
        });

    assertTrue(discovery.getServers().isEmpty());
  }
}
