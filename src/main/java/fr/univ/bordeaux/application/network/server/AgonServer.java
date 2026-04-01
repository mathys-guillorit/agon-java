package fr.univ.bordeaux.application.network.server;

import fr.univ.bordeaux.application.match.*;
import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TCP game server entry point.
 *
 * <p>This class is responsible for:
 * <ul>
 *   <li>accepting incoming client connections,</li>
 *   <li>managing connected players,</li>
 *   <li>handling player registration and reconnection,</li>
 *   <li>providing server discovery information.</li>
 * </ul>
 */
public class AgonServer {

    private final int port;
    private final String name;
    private ServerSocket serverSocket;
    private Thread acceptClientThread;
    private final List<ClientHandler> currentClients =
            Collections.synchronizedList(new ArrayList<>());
    private ServerDiscovery discovery;
    private volatile boolean running = false;
    private final AtomicInteger nextPlayerId = new AtomicInteger(1);

    private final Map<String, OnlinePlayer> playersByClientId = new ConcurrentHashMap<>();

    /** Active connected players (id -> player). */
    private final Map<Integer, OnlinePlayer> players = new ConcurrentHashMap<>();

    /** Persistent players (name -> player), used for reconnection. */
    private final Map<String, OnlinePlayer> playersByName = new ConcurrentHashMap<>();

    private final ServerScoreboard scoreboard = new ServerScoreboard();

    private final Map<Integer, ServerGameSession> activeGames = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> playerToGame = new ConcurrentHashMap<>();
    private final AtomicInteger nextGameId = new AtomicInteger(1);

    /**
     * Creates a new server with a specified owner name and port.
     *
     * @param port the TCP port to listen on
     * @param ownerName the name of the local profile owning this server
     */
    public AgonServer(int port, String ownerName) {
        this.port = port;
        this.name = "AgonServer_" + ownerName + "_" + port;
    }

    /**
     * Creates a new server using the default port (12345).
     *
     * @param ownerName the name of the local profile owning this server
     */
    public AgonServer(String ownerName) {
        this(12345, ownerName);
    }

    /**
     * Starts the TCP server and initializes the discovery service.
     *
     * <p>This method opens the server socket and launches a dedicated thread
     * to accept incoming client connections.
     *
     * @return true if the server started successfully, false otherwise
     */
    public boolean start() {
        if (running) return true;

        try {
            serverSocket = new ServerSocket(port);
            discovery = new ServerDiscovery(name, port);
            discovery.start();
        } catch (IOException e) {
            System.err.println("[SERVER] Failed to start on port " + port);
            return false;
        }

        running = true;
        acceptClientThread = new Thread(this::acceptClientLoop, "acceptClientThread");
        acceptClientThread.start();

        return true;
    }

    /**
     * Main loop responsible for accepting incoming client connections.
     *
     * <p>Each accepted socket is associated with a new {@link ClientHandler}
     * running in its own thread.
     */
    private void acceptClientLoop() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(clientSocket, this);
                currentClients.add(handler);
                new Thread(handler).start();

            } catch (IOException e) {
                break;
            }
        }
    }

    /**
     * Stops the server and closes all active connections.
     *
     * <p>This method:
     * <ul>
     *   <li>terminates all connected client handlers,</li>
     *   <li>clears the active players list,</li>
     *   <li>stops the discovery service,</li>
     *   <li>closes the server socket.</li>
     * </ul>
     *
     * @return true if the server was stopped successfully
     */
    public boolean stop() {
        if (!running) return true;

        running = false;

        List<ClientHandler> clientSnapshot;
        synchronized (currentClients) {
            clientSnapshot = new ArrayList<>(currentClients);
        }

        for (ClientHandler handler : clientSnapshot) {
            handler.stop();
        }

        currentClients.clear();
        players.clear();

        if (discovery != null) {
            discovery.stop();
            discovery = null;
        }

        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {}

        return true;
    }

    /**
     * Returns the TCP port used by this server instance.
     *
     * @return the TCP port number on which the server is listening
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the logical name of the server used for discovery.
     *
     * @return the server name broadcasted over the network
     */
    public String getName() {
        return name;
    }

    /**
     * Indicates whether the server is currently running.
     *
     * @return true if the server is active, false otherwise
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Removes a client handler from the active connections list.
     *
     * @param handler the client handler to remove
     */
    public void removeClient(ClientHandler handler) {
        currentClients.remove(handler);
    }

    /**
     * Returns the number of currently connected clients.
     *
     * @return the number of active client connections
     */
    public int getConnectedClientsCount() {
        return currentClients.size();
    }

    /**
     * Registers a player on the server or reconnects an existing one.
     *
     * <p>If a player with the same name already exists, their previous instance
     * is reused and associated with the new connection. Otherwise, a new player
     * is created with a unique identifier.
     *
     * @param clientId the player's client id
     * @param name the player's display name
     * @param handler the client handler associated with the connection
     * @return the registered or reconnected player instance, or null if the name is invalid
     */
    public OnlinePlayer registerPlayer(String clientId, String name, ClientHandler handler) {

        if (clientId == null || clientId.isBlank() || name == null || name.isBlank()) {
            return null;
        }

        String cleanClientId = clientId.trim();

        OnlinePlayer existing = playersByClientId.get(cleanClientId);

        if (existing != null) {
            existing.setHandler(handler);
            existing.setStatus(PlayerStatus.IDLE);
            players.put(existing.getId(), existing);
            scoreboard.getOrCreateStats(existing.getName());
            return existing;
        }

        int id = nextPlayerId.getAndIncrement();

        OnlinePlayer player =
                new OnlinePlayer(id, cleanClientId, name.trim(), PlayerStatus.IDLE, handler);

        players.put(id, player);
        playersByClientId.put(cleanClientId, player);
        scoreboard.getOrCreateStats(player.getName());

        return player;
    }

    /**
     * Detaches a player from the server without deleting their data.
     *
     * <p>The player remains stored for future reconnections, but is removed
     * from the list of currently active players.
     *
     * @param player the player to disconnect
     */
    public void removePlayer(OnlinePlayer player) {
        if (player != null) {
            player.setHandler(null);
            player.setStatus(PlayerStatus.IDLE);
            players.remove(player.getId());
        }
    }

    /**
     * Returns the formatted list of currently connected players.
     *
     * <p>Each player is represented as a single line containing their ID,
     * name, and status. The response is terminated with an "END" marker.
     *
     * @return a multi-line string describing all active players
     */
    public String getPlayersList() {
        if (players.isEmpty()) {
            return "PLAYERS_EMPTY\nEND";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("=== PLAYERS ===\n");
        for (OnlinePlayer p : players.values()) {
            sb.append("ID=").append(p.getId())
                    .append(" NAME=").append(p.getName())
                    .append(" STATUS=").append(p.getStatus().name().toLowerCase())
                    .append("\n");
        }
        sb.append("===============\n");
        sb.append("END");
        return sb.toString();
    }

    /**
     * Returns the number of currently connected players.
     *
     * @return the number of active players
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * Returns the scoreboard of all players registered on this server.
     *
     * <p>The scoreboard includes each player's number of wins, losses,
     * and total games played. The response is formatted as multiple lines
     * and terminated by an "END" marker.
     *
     * @return a formatted multi-line scoreboard string
     */
    public String getScoreboard() {

        if (scoreboard.isEmpty()) {
            return "SCOREBOARD_EMPTY\nEND";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("=== SCOREBOARD ===\n");
        for (ServerPlayerStats stats : scoreboard.getAllStats()) {
            OnlinePlayer player = playersByName.get(stats.getPlayerName().toLowerCase());

            if (player != null) {
                sb.append("ID=").append(player.getId())
                        .append(" NAME=").append(stats.getPlayerName())
                        .append(" WINS=").append(stats.getWins())
                        .append(" LOSSES=").append(stats.getLosses())
                        .append(" GAMES=").append(stats.getGames())
                        .append("\n");
            }
        }
        sb.append("==================\n");
        sb.append("END");
        return sb.toString();
    }

    /**
     * Returns the number of currently active game sessions.
     *
     * @return the number of games in progress
     */
    public int getActiveGameCount() {
        return activeGames.size();
    }

    /**
     * Checks whether a player is currently involved in a game.
     *
     * @param playerId the ID of the player
     * @return true if the player is in a game, false otherwise
     */
    public boolean isPlayerInGame(int playerId) {
        return playerToGame.containsKey(playerId);
    }

    /**
     * Returns the game ID associated with a given player.
     *
     * <p>If the player is not currently in a game, null is returned.
     *
     * @param playerId the ID of the player
     * @return the game ID, or null if none exists
     */
    public Integer getGameIdByPlayer(int playerId) {
        return playerToGame.get(playerId);
    }

    /**
     * Retrieves a game session by its identifier.
     *
     * @param gameId the ID of the game
     * @return the corresponding game session, or null if not found
     */
    public ServerGameSession getGameById(int gameId) {
        return activeGames.get(gameId);
    }

    /**
     * Returns a connected player by its ID.
     *
     * @param playerId the player ID
     * @return the player instance, or null if not found
     */
    public OnlinePlayer getPlayerById(int playerId) {
        return players.get(playerId);
    }

    /**
     * Starts a new game between two players.
     *
     * <p>This method:
     * <ul>
     *   <li>randomly assigns colors,</li>
     *   <li>creates a real server-side match,</li>
     *   <li>creates and registers a new game session,</li>
     *   <li>links both players to the created game,</li>
     *   <li>updates both players to INGAME status.</li>
     * </ul>
     *
     * @param requesterId the player requesting the game
     * @param targetId the opponent player
     * @return the created game session, or null if creation failed
     */
    public ServerGameSession startNewGame(int requesterId, int targetId) {

        OnlinePlayer requester = players.get(requesterId);
        OnlinePlayer target = players.get(targetId);

        if (requester == null || target == null) {
            return null;
        }

        int gameId = nextGameId.getAndIncrement();

        // random color for players
        boolean requesterIsWhite = Math.random() < 0.5;
        OnlinePlayer whitePlayer = requesterIsWhite ? requester : target;
        OnlinePlayer blackPlayer = requesterIsWhite ? target : requester;

        Match match = MatchFactory.createOnlineMatch(
                whitePlayer.getName(),
                blackPlayer.getName()
        );

        ServerGameSession session =
                new ServerGameSession(gameId, whitePlayer, blackPlayer, match);

        activeGames.put(gameId, session);

        playerToGame.put(requester.getId(), gameId);
        playerToGame.put(target.getId(), gameId);

        requester.setStatus(PlayerStatus.INGAME);
        target.setStatus(PlayerStatus.INGAME);

        return session;
    }
}