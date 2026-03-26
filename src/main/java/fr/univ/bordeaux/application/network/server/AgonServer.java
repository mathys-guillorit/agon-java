package fr.univ.bordeaux.application.network.server;

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

    /** Active connected players (id -> player). */
    private final Map<Integer, OnlinePlayer> players = new ConcurrentHashMap<>();

    /** Persistent players (name -> player), used for reconnection. */
    private final Map<String, OnlinePlayer> playersByName = new ConcurrentHashMap<>();

    private final ServerScoreboard scoreboard = new ServerScoreboard();

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
     * @param name the player's display name
     * @param handler the client handler associated with the connection
     * @return the registered or reconnected player instance, or null if the name is invalid
     */
    public OnlinePlayer registerPlayer(String name, ClientHandler handler) {

        if (name == null || name.isBlank()) return null;

        String cleanName = name.trim().toLowerCase();

        // Existing player → reconnect
        OnlinePlayer existing = playersByName.get(cleanName);

        if (existing != null) {
            existing.setHandler(handler);
            existing.setStatus(PlayerStatus.IDLE);
            players.put(existing.getId(), existing);
            scoreboard.getOrCreateStats(existing.getName());
            return existing;
        }

        // New player → create
        int id = nextPlayerId.getAndIncrement();

        OnlinePlayer player =
                new OnlinePlayer(id, name.trim(), PlayerStatus.IDLE, handler);

        players.put(id, player);
        playersByName.put(cleanName, player);
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
            return "PLAYERS_EMPTY\nEND\n";
        }

        StringBuilder sb = new StringBuilder();

        for (OnlinePlayer p : players.values()) {
            sb.append("ID=").append(p.getId())
                    .append(" NAME=").append(p.getName())
                    .append(" STATUS=").append(p.getStatus().name().toLowerCase())
                    .append("\n");
        }

        sb.append("END\n");
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
            return "SCOREBOARD_EMPTY\nEND\n";
        }

        StringBuilder sb = new StringBuilder();

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

        sb.append("END\n");
        return sb.toString();
    }
}