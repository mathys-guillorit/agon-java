package fr.univ.bordeaux.integration.network;

import fr.univ.bordeaux.application.network.server.AgonServer;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for TCP server timeout behavior.
 */
public class TcpServerTimeout {

    @Test
    void testServerTimeout() throws Exception {
        int port = 12347; // Port différent pour éviter les conflits
        AgonServer server = new AgonServer(port);
        server.start();
        Thread.sleep(50);

        try (Socket socket = new Socket("127.0.0.1", port)) {
            InputStream is = socket.getInputStream();

            System.out.println("[TEST] Waiting 61s for server timeout...");
            Thread.sleep(61000);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII));

            String line = reader.readLine();
            assertEquals("BYE", line, "Le serveur doit notifier avec 'BYE' avant de couper");

            int nextRead = is.read();
            assertEquals(-1, nextRead, "La socket doit être fermée (EOF) après le message BYE");

            System.out.println("[TEST] Timeout");
        } finally {
            server.stop();
        }
    }
}
