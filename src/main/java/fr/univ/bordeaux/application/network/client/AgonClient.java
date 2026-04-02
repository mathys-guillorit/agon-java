package fr.univ.bordeaux.application.network.client;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.network.OnlineGameInfo;
import fr.univ.bordeaux.application.network.OnlineGameStartListener;

import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

/**
 * The AgonClient class manages the TCP connection to the game server.
 *
 * <p>This version is adapted for F39:
 * <ul>
 *   <li>only one thread reads from the socket,</li>
 *   <li>synchronous responses are stored line by line,</li>
 *   <li>asynchronous events are handled separately,</li>
 *   <li>multi-line responses such as PLAYERS and SCOREBOARD are supported.</li>
 * </ul>
 */
public class AgonClient {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    /** Local profile containing the player's name and server ids. */
    private final LocalProfile profile;
    private Thread keepAliveThread;
    private volatile boolean keepAliveRunning = false;

    private Thread readerThread;
    private volatile boolean readerRunning = false;

    /**
     * Stores synchronous server responses in arrival order.
     *
     * <p>This is used by commands such as STATUS, PLAYERS, SCOREBOARD, NEW, PING.
     */
    private final LinkedList<String> pendingResponses = new LinkedList<>();

    /** Lock used to wait for incoming responses */
    private final Object responseLock = new Object();

    /**
     * Lock used to ensure only one synchronous command is active at a time.
     *
     * <p>This avoids mixing PLAYERS / SCOREBOARD / PING / NEW replies.
     */
    private final Object commandLock = new Object();

    private OnlineGameStartListener onlineGameStartListener;

    /**
     * Constructor with a local profile.
     *
     * @param profile the local profile of the player
     */
    public AgonClient(LocalProfile profile) {
        this.profile = profile;
    }

    /**
     * Connects to a TCP server and logs in using the local profile name.
     *
     * @param host server host (e.g. "127.0.0.1")
     * @param port server port (e.g. 12345)
     * @return true if the connection and login succeed, false otherwise
     */
    public boolean connect(String host, int port) {
        if (isConnected()) {
            return true;
        }

        try {
            socket = new Socket(host, port);

            // No timeout on client read side: the reader thread can wait normally.
            socket.setSoTimeout(0);

            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            out = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));

            // Send login request
            sendLine("LOGIN NAME=" + profile.getName() + " CLIENT_ID=" + profile.getClientId());

            // Read first response directly before starting the reader thread
            String response = in.readLine();

            if (response == null || !response.startsWith("WELCOME")) {
                disconnectSilently();
                return false;
            }

            Integer id = extractId(response);
            if (id != null) {
                String serverKey = host + ":" + port;
                profile.setIdForServer(serverKey, id);
            }

            startReader();
            startKeepAlive();

            return true;

        } catch (IOException e) {
            disconnectSilently();
            return false;
        }
    }

    /**
     * Indicates whether the client is currently connected.
     *
     * @return true if the socket is connected and not closed
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Starts the unique reader thread.
     *
     * <p>This thread is the only one allowed to read incoming lines from the socket.
     */
    private void startReader() {
        if (readerRunning) {
            return;
        }

        readerRunning = true;

        readerThread = new Thread(() -> {
            try {
                while (readerRunning && isConnected()) {
                    String line = in.readLine();

                    if (line == null) {
                        disconnectSilently();
                        break;
                    }

                    if ("BYE".equalsIgnoreCase(line.trim())) {
                        disconnectSilently();
                        break;
                    }

                    if (isAsyncEvent(line)) {
                        handleAsyncEvent(line);
                    } else {
                        synchronized (responseLock) {
                            pendingResponses.addLast(line);
                            responseLock.notifyAll();
                        }
                    }
                }
            } catch (IOException e) {
                disconnectSilently();
            }
        }, "AgonClient-Reader");

        readerThread.setDaemon(true);
        readerThread.start();
    }

    /**
     * Returns true if the received line is an asynchronous event.
     *
     * @param line received protocol line
     * @return true if it is an async event
     */
    private boolean isAsyncEvent(String line) {
        return line.startsWith("GAME_STARTED")
                || line.startsWith("NEW_OK")
                || line.startsWith("MOVE_OK")
                || line.startsWith("OPPONENT_MOVE")
                || line.startsWith("GAME_OVER")
                || line.startsWith("YOUR_TURN")
                || line.startsWith("ERROR MESSAGE=");
    }

    /**
     * Handles asynchronous events sent by the server.
     *
     * @param line async event line
     */
    private void handleAsyncEvent(String line) {
        if (line.startsWith("NEW_OK") || line.startsWith("GAME_STARTED")) {
            System.out.println("[ONLINE] " + line);
            handleGameStartMessage(line);
            return;
        }

        if (line.startsWith("MOVE_OK")) {
            String moveText = line.substring("MOVE_OK".length()).trim();
            System.out.println("[ONLINE] Move accepted: " + moveText);

            if (onlineGameStartListener != null && !moveText.isBlank()) {
                onlineGameStartListener.onLocalMoveConfirmed(moveText);
            }
            return;
        }

        if (line.startsWith("OPPONENT_MOVE")) {
            String moveText = line.substring("OPPONENT_MOVE".length()).trim();
            System.out.println("[ONLINE] Opponent played: " + moveText);

            if (onlineGameStartListener != null && !moveText.isBlank()) {
                onlineGameStartListener.onOpponentMoveReceived(moveText);
            }
            return;
        }

        if (line.startsWith("GAME_OVER")) {
            if (line.contains("RESULT=WIN") && line.contains("REASON=OPPONENT_LEFT")) {
                System.out.println("[ONLINE] Opponent left the game. You win by forfeit.");
            } else if (line.contains("RESULT=LOSS") && line.contains("REASON=OPPONENT_LEFT")) {
                System.out.println("[ONLINE] You resigned. You lose the game.");
            } else if (line.contains("RESULT=WIN")) {
                System.out.println("[ONLINE] You win.");
            } else if (line.contains("RESULT=LOSS")) {
                System.out.println("[ONLINE] You lose.");
            } else {
                System.out.println("[ONLINE] " + line);
            }

            if (onlineGameStartListener != null) {
                onlineGameStartListener.onGameOver(line);
            }
            return;
        }

        if (line.startsWith("ERROR MESSAGE=")) {
            String msg = line.substring("ERROR MESSAGE=".length()).trim();

            switch (msg) {
                case "INVALID_MOVE" -> System.out.println("[SERVER] Illegal move.");
                case "NOT_YOUR_TURN" -> System.out.println("[SERVER] Not your turn.");
                case "MISSING_MOVE" -> System.out.println("[SERVER] Missing move.");
                case "NOT_IN_GAME" -> System.out.println("[SERVER] You are not in a game.");
                case "GAME_NOT_FOUND" -> System.out.println("[SERVER] Game not found.");
                default -> System.out.println("[SERVER] " + msg);
            }

            if (onlineGameStartListener != null) {
                onlineGameStartListener.onOnlineBoardRefreshRequested();
            }

            return;
        }

        System.out.println("[SERVER] " + line);
    }

    /**
     * Waits for the next synchronous response line from the server.
     *
     * @param timeoutMs maximum wait time in milliseconds
     * @return the received line, or null if timeout/disconnection occurs
     */
    private String waitResponse(long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;

        synchronized (responseLock) {
            while (isConnected()) {
                while (!pendingResponses.isEmpty()) {
                    String line = pendingResponses.removeFirst();

                    if (line != null && !line.isBlank()) {
                        return line;
                    }
                }

                long remaining = end - System.currentTimeMillis();

                if (remaining <= 0) {
                    return null;
                }

                try {
                    responseLock.wait(remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
            return null;
        }
    }

    /**
     * Starts a background thread that sends PING messages every 30 seconds
     * to prevent server-side timeout.
     */
    private void startKeepAlive() {
        if (keepAliveRunning) {
            return;
        }

        keepAliveRunning = true;

        keepAliveThread = new Thread(() -> {
            while (keepAliveRunning) {
                try {
                    Thread.sleep(30_000);

                    if (!isConnected()) {
                        break;
                    }

                    synchronized (commandLock) {
                        sendLine("PING");

                        String resp = waitResponse(5000);

                        if (resp == null || !resp.startsWith("PONG")) {
                            disconnectSilently();
                            break;
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (IOException e) {
                    disconnectSilently();
                    break;
                }
            }
        }, "AgonClient-KeepAlive");

        keepAliveThread.setDaemon(true);
        keepAliveThread.start();
    }

    /**
     * Requests the status of the connected remote server.
     *
     * @return the raw server response if successful, null otherwise
     */
    public String requestServerStatus() {
        if (!isConnected()) {
            return null;
        }

        synchronized (commandLock) {
            try {
                sendLine("STATUS");

                String resp = waitResponse(5000);

                if (resp == null || !resp.startsWith("STATUS_OK")) {
                    return null;
                }

                return resp;

            } catch (IOException e) {
                disconnectSilently();
                return null;
            }
        }
    }

    /**
     * Requests the list of connected players from the server.
     *
     * @return the raw players response if successful, null otherwise
     */
    public String requestPlayers() {
        if (!isConnected()) {
            return null;
        }

        synchronized (commandLock) {
            try {
                sendLine("PLAYERS");

                StringBuilder sb = new StringBuilder();

                while (true) {
                    String line = waitResponse(5000);

                    if (line == null) {
                        return null;
                    }

                    if ("END".equals(line)) {
                        break;
                    }

                    sb.append(line).append("\n");
                }

                return sb.toString().trim();

            } catch (IOException e) {
                disconnectSilently();
                return null;
            }
        }
    }

    /**
     * Requests the scoreboard from the connected server.
     *
     * @return the formatted scoreboard response, or null if the request fails
     */
    public String requestScoreboard() {
        if (!isConnected()) {
            return null;
        }

        synchronized (commandLock) {
            try {
                sendLine("SCOREBOARD");

                StringBuilder sb = new StringBuilder();

                while (true) {
                    String line = waitResponse(5000);

                    if (line == null) {
                        return null;
                    }

                    if ("END".equals(line)) {
                        break;
                    }

                    sb.append(line).append("\n");
                }

                return sb.toString().trim();

            } catch (IOException e) {
                disconnectSilently();
                return null;
            }
        }
    }

    /**
     * Requests the server to start a new game against a specific player.
     *
     * @param targetPlayerId the ID of the target player
     * @return the raw server response if successful, null otherwise
     */
    public String requestNewGame(int targetPlayerId) {
        if (!isConnected()) {
            return null;
        }

        synchronized (commandLock) {
            try {
                sendLine("NEW PLAYER_ID=" + targetPlayerId);
                return "[CLIENT] New game request sent.";

            } catch (IOException e) {
                disconnectSilently();
                return null;
            }
        }
    }

    /**
     * Checks whether the connection is still alive by sending a PING.
     *
     * @return true if the server replies with PONG, false otherwise
     */
    public boolean isAlive() {
        if (!isConnected()) {
            return false;
        }

        synchronized (commandLock) {
            try {
                sendLine("PING");

                String resp = waitResponse(5000);

                if (resp == null || !resp.startsWith("PONG")) {
                    disconnectSilently();
                    return false;
                }

                return true;

            } catch (IOException e) {
                disconnectSilently();
                return false;
            }
        }
    }

    /**
     * Sends PING to the server and waits for a PONG reply.
     *
     * @return a formatted RTT string if successful, null otherwise
     */
    public String pingRttMs() {
        if (!isConnected()) {
            return null;
        }

        long t0 = System.currentTimeMillis();

        synchronized (commandLock) {
            try {
                sendLine("PING");

                String resp = waitResponse(5000);

                if (resp == null || !resp.startsWith("PONG")) {
                    return null;
                }

                long rtt = System.currentTimeMillis() - t0;
                return "[SERVER] PONG TIME=" + rtt + "ms";

            } catch (IOException e) {
                disconnectSilently();
                return null;
            }
        }
    }

    /**
     * Sends QUIT to the server then closes the connection.
     */
    public void quit() {
        if (!isConnected()) {
            disconnectSilently();
            return;
        }

        synchronized (commandLock) {
            try {
                sendLine("QUIT");
                waitResponse(2000);
            } catch (IOException ignored) {
            } finally {
                disconnectSilently();
            }
        }
    }

    /**
     * Closes everything without throwing exceptions.
     */
    public void disconnectSilently() {
        keepAliveRunning = false;
        readerRunning = false;

        synchronized (responseLock) {
            pendingResponses.clear();
            responseLock.notifyAll();
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }

        socket = null;
        in = null;
        out = null;
    }

    /**
     * Writes a single line to the server followed by a newline character.
     *
     * @param msg message to send
     * @throws IOException if the client is not connected or the write fails
     */
    private synchronized void sendLine(String msg) throws IOException {
        if (out == null) {
            throw new IOException("Not connected");
        }

        out.write(msg);
        out.write('\n');
        out.flush();
    }

    /**
     * Extracts the player id from a server response.
     *
     * @param response raw server response
     * @return extracted id, or null if not found
     */
    private Integer extractId(String response) {
        String[] parts = response.split("\\s+");

        for (String part : parts) {
            if (part.startsWith("ID=")) {
                try {
                    return Integer.parseInt(part.substring(3));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Registers a listener notified when an online game starts.
     *
     * @param listener the listener to notify
     */
    public void setOnlineGameStartListener(OnlineGameStartListener listener) {
        this.onlineGameStartListener = listener;
    }

    /**
     * Parses a protocol line formatted as:
     * COMMAND KEY=VALUE KEY=VALUE ...
     *
     * @param line the protocol line
     * @return a map of parsed key/value pairs
     */
    private Map<String, String> parseProtocolArgs(String line) {
        Map<String, String> args = new HashMap<>();

        if (line == null || line.isBlank()) {
            return args;
        }

        String[] parts = line.trim().split("\\s+");

        for (int i = 1; i < parts.length; i++) {
            String token = parts[i];
            int eq = token.indexOf('=');

            if (eq <= 0) {
                continue;
            }

            String key = token.substring(0, eq);
            String value = token.substring(eq + 1);

            if (!key.isEmpty()) {
                args.put(key, value);
            }
        }

        return args;
    }

    /**
     * Handles a game-start protocol message and notifies the registered listener.
     *
     * <p>Supported messages:
     * <ul>
     *   <li>NEW_OK ...</li>
     *   <li>GAME_STARTED ...</li>
     * </ul>
     *
     * @param line the received protocol line
     */
    private void handleGameStartMessage(String line) {
        Map<String, String> args = parseProtocolArgs(line);

        try {
            String gameIdValue = args.get("GAME_ID");
            String colorValue = args.get("COLOR");
            String whiteName = args.get("WHITE");
            String blackName = args.get("BLACK");

            if (gameIdValue == null || colorValue == null || whiteName == null || blackName == null) {
                System.err.println("[CLIENT] Invalid game start message: " + line);
                return;
            }

            int gameId = Integer.parseInt(gameIdValue);
            Color localColor = Color.valueOf(colorValue.toUpperCase());

            // At game start, WHITE always starts.
            boolean myTurn = (localColor == Color.WHITE);

            OnlineGameInfo info = new OnlineGameInfo(
                    gameId,
                    localColor,
                    whiteName,
                    blackName,
                    myTurn
            );

            if (onlineGameStartListener != null) {
                onlineGameStartListener.onOnlineGameStarted(info);
            }

        } catch (Exception e) {
            System.err.println("[CLIENT] Failed to parse game start message: " + line);
        }
    }

    public boolean sendMove(int from, int to) {
        if (!isConnected()) {
            return false;
        }

        try {
            String moveText =
                    CoordinateMapper.toAbaPro(from).toLowerCase()
                            + CoordinateMapper.toAbaPro(to).toLowerCase();

            synchronized (commandLock) {
                sendLine("MOVE " + moveText);
            }

            return true;

        } catch (Exception e) {
            disconnectSilently();
            return false;
        }
    }

    public boolean sendRawMove(String rawMove) {
        if (!isConnected() || rawMove == null || rawMove.isBlank()) {
            return false;
        }

        try {
            synchronized (commandLock) {
                sendLine("MOVE " + rawMove.trim().toUpperCase());
            }
            return true;
        } catch (IOException e) {
            disconnectSilently();
            return false;
        }
    }

    public void resignGame() {
        if (!isConnected()) {
            return;
        }

        try {
            sendLine("RESIGN");
        } catch (IOException e) {
            System.err.println("[CLIENT] Failed to resign from online match: " + e.getMessage());
        }
    }
}