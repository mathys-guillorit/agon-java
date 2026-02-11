package fr.univ.bordeaux.integration.network;

import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.server.AgonServer;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class TcpProtocolServerPingQuit {

    @Test
    void protocol_server_ping_then_quit() throws Exception {
        int port = 12345;

        AgonServer server = new AgonServer(port);
        assertTrue(server.start(), "Server should start");

        // Petit délai pour être sûr que le thread accept() est prêt
        Thread.sleep(50);

        try (Socket socket = new Socket("127.0.0.1", port);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
             BufferedWriter out = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII))) {

            System.out.println("[TEST] Connected to server.");

            System.out.println("[TEST] SEND: PING");
            out.write("PING\n");
            out.flush();

            String pong = in.readLine();
            System.out.println("[TEST] RECV: " + pong);

            assertNotNull(pong, "Server should reply to PING");
            assertEquals("PONG TIME=0ms", pong, "Server must reply with protocol format");

            System.out.println("[TEST] SEND: QUIT");
            out.write("QUIT\n");
            out.flush();

            String bye = in.readLine();
            System.out.println("[TEST] RECV: " + bye);

            assertNotNull(bye, "Server should reply to QUIT");
            assertEquals("BYE", bye, "Server must reply exactly 'BYE' to QUIT");

        } finally {
            server.stop();
        }
    }
}
