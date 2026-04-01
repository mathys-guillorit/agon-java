package fr.univ.bordeaux.application.network.server;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
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

    private final Socket socket;
    private final AgonServer server;
    private final CommandParser parser = new CommandParser();
    private volatile boolean running = true;

    /** Output stream used to send messages to the client. */
    private BufferedWriter out;

    /** Player associated with this connection. */
    private OnlinePlayer player;

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
                            + " players=" + server.getPlayerCount()
                            + " games=" + server.getActiveGameCount());

                } else if (cmd.getType() == CommandType.LOGIN) {
                    handleLogin(cmd);

                } else if (cmd.getType() == CommandType.PLAYERS) {
                    send(server.getPlayersList());

                } else if (cmd.getType() == CommandType.SCOREBOARD) {
                    send(server.getScoreboard());

                }  else if (cmd.getType() == CommandType.NEW) {
                    handleNew(cmd);

                } else if (cmd.getType() == CommandType.QUIT) {
                    break;
                }
            }

        } catch (IOException e) {
            if (running) {
                System.err.println("[SERVER] ClientHandler error: " + e.getMessage());
            }
        } finally {
            if (player != null) {
                server.removePlayer(player);
            }
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
    private synchronized void send(String msg) throws IOException {
        if (out != null) {
            out.write(msg);
            out.write('\n');
            out.flush();
        }
    }

    /**
     * Handles LOGIN command.
     *
     * @param cmd commande LOGIN
     */
    private void handleLogin(Command cmd) throws IOException {

        if (player != null) {
            send("ERROR ALREADY_LOGGED_IN");
            return;
        }

        String name = cmd.getArgs().get("NAME");
        String clientId = cmd.getArgs().get("CLIENT_ID");

        if (name == null || name.isBlank()) {
            send("ERROR MESSAGE=MISSING_NAME");
            return;
        }

        if (clientId == null || clientId.isBlank()) {
            send("ERROR MESSAGE=MISSING_CLIENT_ID");
            return;
        }

        player = server.registerPlayer(clientId, name, this);

        if (player == null) {
            send("ERROR MESSAGE=LOGIN_FAILED");
            return;
        }

        send("WELCOME ID=" + player.getId()
                + " NAME=" + player.getName()
                + " STATUS=" + player.getStatus().name().toLowerCase());
    }

    /**
     * Handles NEW command.
     *
     * <p>This method validates the target player, creates a new online match
     * on the server, and notifies both players of:
     * <ul>
     *   <li>the game ID,</li>
     *   <li>the opponent information,</li>
     *   <li>their assigned color,</li>
     *   <li>the white/black role distribution.</li>
     * </ul>
     *
     * @param cmd parsed NEW command
     * @throws IOException if sending a response fails
     */
    private void handleNew(Command cmd) throws IOException {
        if (player == null) {
            send("ERROR MESSAGE=NOT_LOGGED_IN");
            return;
        }

        String targetValue = cmd.getArgs().get("PLAYER_ID");

        if (targetValue == null) {
            send("ERROR MESSAGE=MISSING_PLAYER_ID");
            return;
        }

        int targetId;
        try {
            targetId = Integer.parseInt(targetValue);
        } catch (NumberFormatException e) {
            send("ERROR MESSAGE=INVALID_PLAYER_ID");
            return;
        }

        OnlinePlayer target = server.getPlayerById(targetId);

        if (target == null) {
            send("ERROR MESSAGE=PLAYER_NOT_FOUND");
            return;
        }

        if (target.getId() == player.getId()) {
            send("ERROR MESSAGE=CANNOT_PLAY_SELF");
            return;
        }

        if (player.getStatus() != PlayerStatus.IDLE) {
            send("ERROR MESSAGE=YOU_ARE_BUSY");
            return;
        }

        if (target.getStatus() != PlayerStatus.IDLE) {
            send("ERROR MESSAGE=PLAYER_BUSY");
            return;
        }

        ServerGameSession session =
                server.startNewGame(player.getId(), targetId);

        if (session == null) {
            send("ERROR MESSAGE=GAME_CREATION_FAILED");
            return;
        }

        String roles = session.describeRoles();
        String requesterColor = session.getRoleLabel(player.getId());
        String targetColor = session.getRoleLabel(target.getId());

        // Response to the requester
        send("NEW_OK GAME_ID=" + session.getGameId()
                + " OPPONENT_ID=" + target.getId()
                + " OPPONENT_NAME=" + target.getName()
                + " COLOR=" + requesterColor
                + " " + roles);

        // Notification to the target player
        if (target.getHandler() != null) {
            target.getHandler().send(
                    "GAME_STARTED GAME_ID=" + session.getGameId()
                            + " OPPONENT_ID=" + player.getId()
                            + " OPPONENT_NAME=" + player.getName()
                            + " COLOR=" + targetColor
                            + " " + roles
            );
        }
    }
}