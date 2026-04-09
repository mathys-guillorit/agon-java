package fr.univ.bordeaux.application.network.client;

import fr.univ.bordeaux.application.network.server.PresenceMessage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Client-side UDP discovery service. */
@SuppressWarnings("PMD.DoNotUseThreads")
public class ClientDiscovery {

  /** UDP port used for discovery broadcasts. */
  public static final int UDP_PORT = 12_346;

  /** Discovered servers indexed by "ip:port". */
  private final Map<String, ServerInfo> servers;

  /** UDP socket used to receive broadcast packets. */
  private DatagramSocket socket;

  /** Indicates whether the discovery loop is running. */
  private final AtomicBoolean running;

  /** Creates a new client discovery service. */
  public ClientDiscovery() {
    this.servers = new ConcurrentHashMap<>();
    this.running = new AtomicBoolean(false);
  }

  /**
   * Starts listening for UDP broadcast messages.
   *
   * @throws IOException if the socket cannot be opened
   */
  public void start() throws IOException {
    if (running.get()) {
      return;
    }

    running.set(true);

    socket = new DatagramSocket(null);
    socket.setReuseAddress(true);
    socket.bind(new InetSocketAddress(UDP_PORT));
    socket.setBroadcast(true);

    final Thread discoveryThread =
        new Thread(
            () -> {
              final byte[] receiveBuffer = new byte[512];
              final DatagramPacket receivedPacket =
                  new DatagramPacket(receiveBuffer, receiveBuffer.length);

              while (running.get()) {
                try {
                  socket.receive(receivedPacket);

                  final PresenceMessage presenceMessage =
                      PresenceMessage.parse(receivedPacket.getData(), receivedPacket.getLength());

                  if (presenceMessage == null) {
                    continue;
                  }

                  final InetAddress packetAddress = receivedPacket.getAddress();
                  final String hostAddress = packetAddress.getHostAddress();
                  final String serverKey = hostAddress + ":" + presenceMessage.getTcpPort();

                  final ServerInfo existingServer = servers.get(serverKey);
                  if (existingServer == null) {
                    servers.put(
                        serverKey,
                        new ServerInfo(
                            presenceMessage.getServerName(),
                            hostAddress,
                            presenceMessage.getTcpPort()));
                  } else {
                    existingServer.lastSeen = System.currentTimeMillis();
                  }

                  cleanup();

                } catch (IOException ignored) {
                  if (running.get()) {
                    // Ignored while discovery is active.
                  }
                } catch (RuntimeException ignored) {
                  // Invalid packet or parse failure: ignored.
                }
              }
            },
            "ClientDiscovery-Listener");

    discoveryThread.setDaemon(true);
    discoveryThread.start();
  }

  /** Removes servers not seen for more than 30 seconds. */
  private void cleanup() {
    final long currentTime = System.currentTimeMillis();
    servers.values().removeIf(serverInfo -> currentTime - serverInfo.lastSeen > 30_000);
  }

  /**
   * Returns a snapshot of currently discovered servers.
   *
   * @return list of servers
   */
  public List<ServerInfo> getServers() {
    cleanup();
    return new ArrayList<>(servers.values());
  }

  /** Stops discovery and clears the server list. */
  public void stop() {
    running.set(false);

    if (socket != null) {
      socket.close();
    }

    servers.clear();
  }
}
