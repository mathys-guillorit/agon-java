package fr.univ.bordeaux.integration.network;

import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.server.AgonServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TcpAgonClientPingQuit {

    @Test
    void agonClient_ping_then_quit() throws Exception {
        int port = 12345;
        AgonServer server = new AgonServer(port);
        assertTrue(server.start(), "Server should start");
        Thread.sleep(50);

        AgonClient client = new AgonClient();
        assertTrue(client.connect("127.0.0.1", port));

        String response = client.pingRttMs();

        assertNotNull(response, "Response should not be null");
        assertTrue(response.startsWith("[SERVER] PONG TIME="), "Response should follow protocol format");
        assertTrue(response.endsWith("ms"), "Response should end with 'ms'");

        client.quit();

        assertFalse(client.isConnected(), "Client should be disconnected");
        server.stop();
    }
}
