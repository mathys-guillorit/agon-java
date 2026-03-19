package fr.univ.bordeaux.application.network.client;

import fr.univ.bordeaux.application.network.server.PresenceMessage;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side UDP discovery service.
 */
public class ClientDiscovery {
    public static final int UDP_PORT = 12346;

    private final Map<String, ServerInfo> servers = new HashMap<>();

    private DatagramSocket socket;
    private Thread thread;
    private volatile boolean running = false;

    /**
     * Starts listening for UDP broadcast messages.
     *
     * @throws Exception if socket cannot be opened
     */
    public void start() throws Exception {

        if (running) return;

        running = true;

        socket = new DatagramSocket(null);
        socket.setReuseAddress(true);
        socket.bind(new java.net.InetSocketAddress(UDP_PORT));
        socket.setBroadcast(true);

        thread = new Thread(() -> {

            byte[] buffer = new byte[512];

            while (running) {
                try {
                    // Receive UDP packet
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    // Parse presence message
                    PresenceMessage pm =
                            PresenceMessage.parse(packet.getData(), packet.getLength());

                    if (pm == null) continue;

                    String ip = packet.getAddress().getHostAddress();
                    String key = ip + ":" + pm.getTcpPort();

                    synchronized (this) {
                        ServerInfo s = servers.get(key);
                        if (s == null) {
                            s = new ServerInfo(pm.getServerName(), ip, pm.getTcpPort());
                            servers.put(s.key(), s);
                        } else {
                            s.lastSeen = System.currentTimeMillis();
                        }
                    }

                    // Cleanup outdated servers
                    cleanup();

                } catch (Exception ignored) {}
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Removes servers not seen for more than 30 seconds.
     */
    private synchronized void cleanup() {
        long now = System.currentTimeMillis();
        servers.values().removeIf(s -> now - s.lastSeen > 30_000);
    }

    /**
     * Returns a snapshot of currently discovered servers.
     *
     * @return list of servers
     */
    public synchronized List<ServerInfo> getServers() {
        cleanup();
        return new ArrayList<>(servers.values());
    }

    /**
     * Stops discovery and clears the server list.
     */
    public void stop() {
        running = false;
        if (socket != null) socket.close();
        servers.clear();
    }
}
