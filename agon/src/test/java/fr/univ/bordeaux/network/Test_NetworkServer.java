package fr.univ.bordeaux.network;

import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.application.network.server.PresenceMessage;
import fr.univ.bordeaux.application.network.server.ServerDiscovery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class Test_NetworkServer {

    private AgonServer server;
    private ServerDiscovery discovery;

    @AfterEach
    void cleanup() {
        try { if (discovery != null) discovery.stop(); } catch (Exception ignored) {}
        try { if (server != null) server.stop(); } catch (Exception ignored) {}
        discovery = null;
        server = null;
    }

    @Test
    void server_default_ctor_values() {
        AgonServer s = new AgonServer();
        assertEquals(12345, s.getPort());
        assertNotNull(s.getName());
        assertFalse(s.isRunning());
    }

    @Test
    void server_start_stop() throws Exception {

        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port);
        assertTrue(server.start());
        assertTrue(server.isRunning());

        assertTrue(server.stop());
        assertFalse(server.isRunning());
    }

    @Test
    void server_start_port_used() throws Exception {

        int port = 33111;
        try (ServerSocket lock = new ServerSocket(port)) {
            AgonServer s = new AgonServer(port);
            assertFalse(s.start());
            assertFalse(s.isRunning());
        }
    }

    @Test
    void server_stop_when_off() {
        AgonServer s = new AgonServer(33222);
        assertTrue(s.stop());        // should not crash
        assertFalse(s.isRunning());
    }

    @Test
    void server_start_twice() throws Exception {

        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port);
        assertTrue(server.start());
        assertTrue(server.start());  // running branch
        assertTrue(server.isRunning());
    }

    // -----------------
    // PresenceMessage
    // -----------------

    @Test
    void presence_encode_parse_ok() {
        PresenceMessage pm = new PresenceMessage("S", 12345);
        byte[] data = pm.toBytes();

        PresenceMessage parsed = PresenceMessage.parse(data, data.length);
        assertNotNull(parsed);
        assertEquals("S", parsed.getServerName());
        assertEquals(12345, parsed.getTcpPort());
    }

    @Test
    void presence_parse_invalid_returns_null() {
        byte[] bad = "name=S".getBytes(StandardCharsets.US_ASCII); // missing tcp=
        assertNull(PresenceMessage.parse(bad, bad.length));
    }

    // -----------------
    // ServerDiscovery
    // -----------------

    @Test
    void discovery_start_stop() throws Exception {
        discovery = new ServerDiscovery("S", 12345);
        discovery.start();
        discovery.stop(); // should not crash
        assertTrue(true);
    }
}