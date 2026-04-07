package fr.univ.bordeaux.application.network.client;

import static org.junit.jupiter.api.Assertions.*;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClientDiscoveryTest {

  private static final long INVALID_PACKET_WAIT_MS = 150;

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

  @Test
  @DisplayName("Start and stop are safe")
  void start_and_stop_are_safe() {
    discovery = new ClientDiscovery();

    assertDoesNotThrow(
        () -> {
          discovery.start();
          discovery.stop();
          discovery.stop();
        });

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

  @Test
  @DisplayName("Invalid UDP payload is ignored")
  void invalid_packet_is_ignored() throws Exception {
    discovery = new ClientDiscovery();
    discovery.start();

    sendUdp("invalid-payload-without-required-format");
    Thread.sleep(INVALID_PACKET_WAIT_MS);

    assertTrue(discovery.getServers().isEmpty());
  }
}
