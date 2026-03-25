package fr.univ.bordeaux.application.network.server;

import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.protocol.CommandParser;
import fr.univ.bordeaux.application.network.protocol.CommandType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Handles the communication lifecycle for a single TCP client.
 */
public class ClientHandler implements Runnable {

    /** TCP socket associated with the connected client. */
    private final Socket socket;

    /** Server instance owning this client handler. */
    private final AgonServer server;

    /** Protocol parser used to decode client messages. */
    private final CommandParser parser = new CommandParser();

    /** Indicates whether the handler is still running. */
    private volatile boolean running = true;

    /** Output stream used to send messages to the client. */
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
     * Stops the client handler gracefully.
     *
     * <p>This method:
     * <ul>
     *     <li>marks the handler as stopped,</li>
     *     <li>sends a {@code BYE} message to the client if possible,</li>
     *     <li>closes the socket.</li>
     * </ul>
     */
    public void stop() {
        if (!running) {
            return;
        }

        running = false;

        try {
            if (out != null && socket != null && !socket.isClosed()) {
                send("BYE");
            }
        } catch (IOException ignored) {
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Error while closing client socket: " + e.getMessage());
        }
    }

    /**
     * Main execution method of the client handler thread.
     */
    @Override
    public void run() {
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
                    break;
                }

                if (line == null) {
                    break;
                }

                Command cmd = parser.parse(line);

                if (cmd.getType() == CommandType.PING) {
                    send("PONG TIME=0ms");
                } else if (cmd.getType() == CommandType.STATUS) {
                    send("STATUS_OK port=" + server.getPort()
                            + " clients=" + server.getConnectedClientsCount()
                            + " games=0");
                } else if (cmd.getType() == CommandType.QUIT) {
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
        }
    }

    /**
     * Sends a raw message to the client followed by a newline and flushes the buffer.
     *
     * @param msg the string message to send
     * @throws IOException if the message cannot be written to the socket
     */
    private void send(String msg) throws IOException {
        if (out != null) {
            out.write(msg);
            out.write('\n');
            out.flush();
        }
    }
}