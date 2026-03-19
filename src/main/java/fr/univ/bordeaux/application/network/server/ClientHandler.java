package fr.univ.bordeaux.application.network.server;

import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.protocol.CommandParser;
import fr.univ.bordeaux.application.network.protocol.CommandType;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Handles the communication lifecycle for a single TCP client.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final AgonServer server;
    private final CommandParser parser = new CommandParser();
    private volatile boolean running = true;
    private BufferedWriter out;

    /**
     * Creates a new client handler for the given socket.
     *
     * @param socket the TCP socket associated with the connected client
     * @param server the server instance this handler belongs to
     */
    public ClientHandler(Socket socket, AgonServer server) {
        this.socket = socket;
        this.server = server;
    }

    /**
     * Stops the client handler and closes the client socket.
     */
    public void stop() {
        if (!running) return;
        running = false;

        try {
            if (out != null && !socket.isClosed()) {
                send("BYE"); // Notification demandée par l'énoncé
            }
        } catch (IOException ignored) {}

        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("[SERVER] Error while closing client socket");
        }
    }

    /**
     * Main execution method of the client handler thread.
     */
    @Override
    public void run() {
        System.out.println("[SERVER] ClientHandler started for " + socket.getRemoteSocketAddress());
        try {
            socket.setSoTimeout(60_000);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            this.out = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));

            while (running && !socket.isClosed()) {
                String line;
                try {
                    line = in.readLine();
                } catch (SocketTimeoutException e) {
                    System.out.println("[SERVER] Client timeout (60s).");
                    break;
                }

                if (line == null) break; // Déconnexion inopinée

                Command cmd = parser.parse(line);

                if (cmd.getType() == CommandType.PING) {
                    send("PONG TIME=0ms");
                }
                else if (cmd.getType() == CommandType.STATUS) {
                    send("STATUS_OK port=" + server.getPort()
                            + " clients=" + server.getConnectedClientsCount()
                            + " games=0");
                }
                else if (cmd.getType() == CommandType.QUIT) {
                    send("BYE");
                    break;
                }
            }

        } catch (IOException e) {
            if (running) {
                System.err.println("[SERVER] ClientHandler error: " + e.getMessage());
            }
        } finally {
            if (server != null) {
                server.removeClient(this);
            }
            stop();
            System.out.println("[SERVER] Client disconnected.");
        }
    }

    /**
     * Sends a raw message to the client followed by a newline and flushes the buffer.
     *
     * @param msg The string message to send.
     */
    private void send(String msg) throws IOException {
        if (out != null) {
            out.write(msg);
            out.write('\n');
            out.flush();
        }
    }
}
