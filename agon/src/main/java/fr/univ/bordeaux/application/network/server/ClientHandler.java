package fr.univ.bordeaux.application.network.server;

import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.protocol.CommandParser;
import fr.univ.bordeaux.application.network.protocol.CommandType;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

// Handles the lifecycle of a single TCP client connection.
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final CommandParser parser = new CommandParser();
    private volatile boolean running = true;

    /**
     * Creates a new client handler for the given socket.
     *
     * @param socket the TCP socket associated with the connected client
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * Stops the client handler and closes the client socket.
     */
    public void stop() {
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("[SERVER] Error while closing server socket");
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
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));

            while (running && !socket.isClosed()) {
                String line;

                try {
                    line = in.readLine();
                } catch (SocketTimeoutException e) {
                    System.out.println("[SERVER] Client timeout (60s).");
                    break;
                }

                if (line == null) {
                    // client closed connection
                    break;
                }

                Command cmd = parser.parse(line);

                if (cmd.getType() == CommandType.PING) {
                    send(out, "PONG");
                }
                else if (cmd.getType() == CommandType.QUIT) {
                    send(out, "BYE");
                    break;
                }
            }

        } catch (IOException e) {
            if (running) {
                System.err.println("[SERVER] ClientHandler error: " + e.getMessage());
            }
        } finally {
            stop();
            System.out.println("[SERVER] Client disconnected.");
        }
    }

    private void send(BufferedWriter out, String msg) throws IOException {
        out.write(msg);
        out.write('\n');
        out.flush();
    }
}
