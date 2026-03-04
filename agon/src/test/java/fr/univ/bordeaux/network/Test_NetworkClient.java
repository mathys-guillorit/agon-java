package fr.univ.bordeaux.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.client.ClientDiscovery;
import fr.univ.bordeaux.application.network.client.ServerInfo;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.application.network.server.ServerDiscovery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Test_NetworkClient {

    private AgonServer server;
    private ServerDiscovery serverDiscovery;
    private ClientDiscovery clientDiscovery;

    @AfterEach
    void cleanup() {
        try { if (serverDiscovery != null) serverDiscovery.stop(); } catch (Exception ignored) {}
        try { if (clientDiscovery != null) clientDiscovery.stop(); } catch (Exception ignored) {}
        try { if (server != null) server.stop(); } catch (Exception ignored) {}

        server = null;
        serverDiscovery = null;
        clientDiscovery = null;
    }

    // -----------------
    // AgonClient
    // -----------------

    @Test
    void connect_fail() throws Exception {

        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        } // port free now, no server listening

        AgonClient client = new AgonClient();
        assertFalse(client.connect("127.0.0.1", port));
        assertFalse(client.isConnected());
    }

    @Test
    void connect_ping_quit() throws Exception {

        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port);
        assertTrue(server.start());

        AgonClient client = new AgonClient();
        assertTrue(client.connect("127.0.0.1", port));
        assertTrue(client.isConnected());

        String rtt = client.pingRttMs();
        assertNotNull(rtt);
        assertTrue(rtt.contains("PONG"));
        assertTrue(rtt.endsWith("ms"));

        client.quit();
        assertFalse(client.isConnected());
    }

    @Test
    void ping_not_connected() {
        AgonClient client = new AgonClient();
        assertNull(client.pingRttMs());
        assertFalse(client.isConnected());
    }

    @Test
    void quit_not_connected() {
        AgonClient client = new AgonClient();
        client.quit();
        assertFalse(client.isConnected());
    }

    @Test
    void disconnect_safe() {
        AgonClient client = new AgonClient();
        client.disconnectSilently();
        assertFalse(client.isConnected());
    }

    @Test
    void ping_after_server_stop() throws Exception {

        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port);
        assertTrue(server.start());

        AgonClient client = new AgonClient();
        assertTrue(client.connect("127.0.0.1", port));
        assertTrue(client.isConnected());

        server.stop();
        server = null;

        String r = client.pingRttMs();
        assertTrue(r == null || r.contains("PONG"));

        client.disconnectSilently();
        assertFalse(client.isConnected());
    }

    @Test
    void quit_after_server_stop() throws Exception {

        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port);
        assertTrue(server.start());

        AgonClient client = new AgonClient();
        assertTrue(client.connect("127.0.0.1", port));
        assertTrue(client.isConnected());

        server.stop();
        server = null;

        client.quit();
        assertFalse(client.isConnected());
    }

    // -----------------
    // ServerInfo
    // -----------------

    @Test
    void serverInfo_key() {
        ServerInfo s = new ServerInfo("S1", "127.0.0.1", 12345);
        assertNotNull(s.key());
        assertFalse(s.key().isBlank());
    }

    // -----------------
    // ClientDiscovery
    // -----------------

    @Test
    void discovery_find_server() throws Exception {

        clientDiscovery = new ClientDiscovery();
        clientDiscovery.start();

        serverDiscovery = new ServerDiscovery("DiscServer", 27111);
        serverDiscovery.start();

        boolean found = false;
        long deadline = System.currentTimeMillis() + 12_000;

        while (System.currentTimeMillis() < deadline) {
            List<ServerInfo> servers = clientDiscovery.getServers();
            for (ServerInfo s : servers) {
                if (s.tcpPort == 27111 && "DiscServer".equals(s.name)) {
                    found = true;
                    break;
                }
            }
            if (found) break;
            Thread.sleep(200);
        }

        assertTrue(found);
    }

    @Test
    void discovery_start_twice_stop_twice() throws Exception {

        ClientDiscovery d = new ClientDiscovery();

        d.stop();

        d.start();
        d.start();

        d.stop();
        d.stop();

        assertTrue(d.getServers().isEmpty());
    }

    @Test
    void discovery_cleanup_removes_old() throws Exception {

        ClientDiscovery d = new ClientDiscovery();
        d.start();

        try (java.net.DatagramSocket socket = new java.net.DatagramSocket()) {
            byte[] ok = "name=S;tcp=9999".getBytes();
            socket.send(new java.net.DatagramPacket(
                    ok, ok.length,
                    java.net.InetAddress.getByName("127.0.0.1"),
                    ClientDiscovery.UDP_PORT
            ));
        }

        Thread.sleep(300);

        List<ServerInfo> servers = d.getServers();
        assertFalse(servers.isEmpty());

        servers.get(0).lastSeen = System.currentTimeMillis() - 40_000;

        assertTrue(d.getServers().isEmpty());

        d.stop();
    }

    @Test
    void discovery_update_last_seen_branch() throws Exception {

        ClientDiscovery d = new ClientDiscovery();
        d.start();

        try (java.net.DatagramSocket socket = new java.net.DatagramSocket()) {

            byte[] ok = "name=S;tcp=9999".getBytes();

            socket.send(new java.net.DatagramPacket(
                    ok, ok.length,
                    java.net.InetAddress.getByName("127.0.0.1"),
                    ClientDiscovery.UDP_PORT
            ));

            socket.send(new java.net.DatagramPacket(
                    ok, ok.length,
                    java.net.InetAddress.getByName("127.0.0.1"),
                    ClientDiscovery.UDP_PORT
            ));
        }

        Thread.sleep(300);

        boolean found = false;
        for (ServerInfo s : d.getServers()) {
            if (s.tcpPort == 9999 && "S".equals(s.name)) {
                found = true;
                break;
            }
        }

        assertTrue(found);

        d.stop();
    }

    @Test
    void ctx_isConnected_false() {
        AppContext ctx = new AppContext();
        assertFalse(ctx.isConnected());
    }
}