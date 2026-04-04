package fr.univ.bordeaux.application.network.client;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.network.server.ServerDiscovery;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClientDiscoveryTest {

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

  private void sendUdpSeveralTimes(String payload, int count, long pauseMs) throws Exception {
    for (int i = 0; i < count; i++) {
      sendUdp(payload);
      Thread.sleep(pauseMs);
    }
  }

  private void waitUntil(BooleanSupplier condition, long timeoutMs) throws Exception {
    long deadline = System.currentTimeMillis() + timeoutMs;
    while (System.currentTimeMillis() < deadline) {
      if (condition.getAsBoolean()) {
        return;
      }
      Thread.sleep(25);
    }
    fail("Condition non atteinte dans le délai");
  }

  @SuppressWarnings("unchecked")
  private Map<Object, ServerInfo> getInternalServersMap(ClientDiscovery target) throws Exception {
    for (Field f : target.getClass().getDeclaredFields()) {
      f.setAccessible(true);
      Object value = f.get(target);
      if (value instanceof Map<?, ?> map && !map.isEmpty()) {
        Object firstValue = map.values().iterator().next();
        if (firstValue instanceof ServerInfo) {
          return (Map<Object, ServerInfo>) map;
        }
      }
    }

    for (Field f : target.getClass().getDeclaredFields()) {
      f.setAccessible(true);
      Object value = f.get(target);
      if (value instanceof Map<?, ?> map) {
        return (Map<Object, ServerInfo>) map;
      }
    }

    fail("Impossible de trouver la map interne des serveurs");
    return null;
  }

  @Test
  @DisplayName("Discovery finds a broadcasted server")
  void discovery_find_server() throws Exception {
    discovery = new ClientDiscovery();
    discovery.start();

    ServerDiscovery server = new ServerDiscovery("TestServer", 9999);
    server.start();

    waitUntil(() -> !discovery.getServers().isEmpty(), 3000);

    List<ServerInfo> servers = discovery.getServers();
    assertFalse(servers.isEmpty());

    server.stop();
  }

  @Test
  @DisplayName("Invalid UDP payload is ignored")
  void invalid_packet_is_ignored() throws Exception {
    discovery = new ClientDiscovery();
    discovery.start();

    sendUdp("invalid-payload-without-required-format");
    Thread.sleep(300);

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
