package fr.univ.bordeaux.application.network.server;

import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.match.MatchFactory;
import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TCP game server entry point.
 *
 * <p>This class is responsible for:
 *
 * <ul>
 *   <li>accepting incoming client connections,
 *   <li>managing connected players,
 *   <li>handling player registration and reconnection,
 *   <li>providing server discovery information.
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

  private final ServerScoreboard scoreboard = new ServerScoreboard();

  private final Map<Integer, ServerGameSession> activeGames = new ConcurrentHashMap<>();
  private final Map<Integer, Integer> playerToGame = new ConcurrentHashMap<>();
  private final AtomicInteger nextGameId = new AtomicInteger(1);

  private static final int INVITATION_TIMEOUT_SECONDS = 300;

  private final Map<Integer, Invitation> invitationsByInviter = new ConcurrentHashMap<>();
  private final Map<Integer, Invitation> invitationsByInvited = new ConcurrentHashMap<>();

  private final Map<Integer, GameLobby> lobbiesByPlayer = new ConcurrentHashMap<>();

  private Thread invitationCleanerThread;

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
   * <p>This method opens the server socket and launches a dedicated thread to accept incoming
   * client connections.
   *
   * @return true if the server started successfully, false otherwise
   */
  public boolean start() {
    if (running) {
      return true;
    }

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
    startInvitationCleaner();

    return true;
  }

  /**
   * Main loop responsible for accepting incoming client connections.
   *
   * <p>Each accepted socket is associated with a new {@link ClientHandler} running in its own
   * thread.
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
   *
   * <ul>
   *   <li>terminates all connected client handlers,
   *   <li>clears the active players list,
   *   <li>stops the discovery service,
   *   <li>closes the server socket.
   * </ul>
   *
   * @return true if the server was stopped successfully
   */
  public boolean stop() {
    if (!running) {
      return true;
    }

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
      if (serverSocket != null) {
        serverSocket.close();
      }
    } catch (IOException ignored) {
      // Ignored
    }

    if (invitationCleanerThread != null) {
      invitationCleanerThread.interrupt();
    }

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
   * <p>If a player with the same name already exists, their previous instance is reused and
   * associated with the new connection. Otherwise, a new player is created with a unique
   * identifier.
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
   * Detaches a player from the server.
   *
   * <p>The player remains stored for future reconnections, but is removed from the list of
   * currently active players.
   *
   * @param player the player to disconnect
   */
  public synchronized void removePlayer(OnlinePlayer player) {
    if (player == null) {
      return;
    }

    removeInvitation(player.getId());

    GameLobby lobby = lobbiesByPlayer.remove(player.getId());
    if (lobby != null) {
      lobbiesByPlayer.remove(lobby.getHostId());
      lobbiesByPlayer.remove(lobby.getGuestId());

      OnlinePlayer host = players.get(lobby.getHostId());
      OnlinePlayer guest = players.get(lobby.getGuestId());

      if (host != null) {
        host.setStatus(PlayerStatus.IDLE);
      }

      if (guest != null) {
        guest.setStatus(PlayerStatus.IDLE);
      }
    }

    player.setHandler(null);
    player.setStatus(PlayerStatus.IDLE);
    players.remove(player.getId());
  }

  /**
   * Returns the formatted list of currently connected players.
   *
   * <p>Each player is represented as a single line containing their ID, name, and status. The
   * response is terminated with an "END" marker.
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
      sb.append("ID=")
          .append(p.getId())
          .append(" NAME=")
          .append(p.getName())
          .append(" STATUS=")
          .append(p.getStatus().name().toLowerCase())
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
   * <p>The scoreboard includes each player's number of wins, losses, and total games played. The
   * response is formatted as multiple lines and terminated by an "END" marker.
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
      sb.append("NAME=")
          .append(stats.getPlayerName())
          .append(" WINS=")
          .append(stats.getWins())
          .append(" LOSSES=")
          .append(stats.getLosses())
          .append(" GAMES=")
          .append(stats.getGames())
          .append("\n");
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
   *
   * <ul>
   *   <li>randomly assigns colors,
   *   <li>creates a real server-side match,
   *   <li>creates and registers a new game session,
   *   <li>links both players to the created game,
   *   <li>updates both players to INGAME status.
   * </ul>
   *
   * @param requesterId the player requesting the game
   * @param targetId the opponent player
   * @return the created game session, or null if creation failed
   */
  public ServerGameSession startNewGame(int requesterId, int targetId, GameMode mode) {

    OnlinePlayer requester = players.get(requesterId);
    OnlinePlayer target = players.get(targetId);

    if (requester == null || target == null || mode == null) {
      return null;
    }

    int gameId = nextGameId.getAndIncrement();

    // random color for players
    boolean requesterIsWhite = Math.random() < 0.5;
    OnlinePlayer whitePlayer = requesterIsWhite ? requester : target;
    OnlinePlayer blackPlayer = requesterIsWhite ? target : requester;

    Match match =
        MatchFactory.createOnlineMatch(
            whitePlayer.getName(), blackPlayer.getName(), mode == GameMode.BLITZ);
    ServerGameSession session = new ServerGameSession(gameId, whitePlayer, blackPlayer, match);

    activeGames.put(gameId, session);
    playerToGame.put(requester.getId(), gameId);
    playerToGame.put(target.getId(), gameId);

    requester.setStatus(PlayerStatus.INGAME);
    target.setStatus(PlayerStatus.INGAME);

    return session;
  }

  /**
   * Finishes an active game session and updates the server state.
   *
   * <p>This method removes the game from the active sessions, resets both players to idle status,
   * updates the scoreboard, and notifies the winner and loser with a final {@code GAME_OVER}
   * message.
   *
   * @param session the game session to finish
   * @param winnerPlayerId the ID of the winning player
   * @param reason the reason associated with the game end
   */
  public synchronized void finishGame(
      ServerGameSession session, int winnerPlayerId, String reason) {
    if (session == null) {
      return;
    }

    int gameId = session.getGameId();

    ServerGameSession removed = activeGames.remove(gameId);
    if (removed == null) {
      return;
    }

    OnlinePlayer white = removed.getwhitePlayer();
    OnlinePlayer black = removed.getblackPlayer();

    playerToGame.remove(white.getId());
    playerToGame.remove(black.getId());

    white.setStatus(PlayerStatus.IDLE);
    black.setStatus(PlayerStatus.IDLE);

    OnlinePlayer loser = removed.getOpponent(winnerPlayerId);
    OnlinePlayer winner = null;

    if (white.getId() == winnerPlayerId) {
      winner = white;
    } else if (black.getId() == winnerPlayerId) {
      winner = black;
    }

    if (winner != null && loser != null) {
      scoreboard.getOrCreateStats(winner.getName()).addWin();
      scoreboard.getOrCreateStats(loser.getName()).addLoss();

      if (winner.getHandler() != null) {
        try {
          winner
              .getHandler()
              .sendFromServer(
                  "GAME_OVER RESULT=WIN REASON=" + reason + " OPPONENT=" + loser.getName());
        } catch (IOException ignored) {
          // Ignored
        }
      }

      if (loser.getHandler() != null) {
        try {
          loser
              .getHandler()
              .sendFromServer(
                  "GAME_OVER RESULT=LOSS REASON=" + reason + " OPPONENT=" + winner.getName());
        } catch (IOException ignored) {
          // Ignored
        }
      }
    }
  }

  /**
   * Returns the formatted details of a connected player.
   *
   * <p>The returned information includes the player id, name, client id, current status, and
   * scoreboard statistics.
   *
   * @param playerId the id of the player to describe
   * @return a formatted string describing the player, or an error message if not found
   */
  public String getPlayerDetails(int playerId) {
    OnlinePlayer player = players.get(playerId);

    if (player == null) {
      return "ERROR MESSAGE=PLAYER_NOT_FOUND";
    }

    ServerPlayerStats stats = scoreboard.getOrCreateStats(player.getName());

    return "PLAYER ID="
        + player.getId()
        + " NAME="
        + player.getName()
        + " STATUS="
        + player.getStatus().name().toLowerCase()
        + " WINS="
        + stats.getWins()
        + " LOSSES="
        + stats.getLosses()
        + " GAMES="
        + stats.getGames();
  }

  /** Starts the background thread that removes expired invitations. */
  private void startInvitationCleaner() {
    invitationCleanerThread =
        new Thread(
            () -> {
              while (running) {
                try {
                  Thread.sleep(1000);
                  cleanExpiredInvitations();
                } catch (InterruptedException ignored) {
                  break;
                }
              }
            },
            "invitation-cleaner-thread");

    invitationCleanerThread.start();
  }

  /** Removes all expired invitations from the server. */
  private synchronized void cleanExpiredInvitations() {
    List<Invitation> expiredInvitations = new ArrayList<>();

    for (Invitation invitation : invitationsByInviter.values()) {
      if (invitation.isExpired()) {
        expiredInvitations.add(invitation);
      }
    }

    for (Invitation invitation : expiredInvitations) {
      invitationsByInviter.remove(invitation.getInviterId());
      invitationsByInvited.remove(invitation.getInvitedId());

      OnlinePlayer inviter = players.get(invitation.getInviterId());
      OnlinePlayer invited = players.get(invitation.getInvitedId());

      if (inviter != null && inviter.getStatus() == PlayerStatus.WAITGAME) {
        inviter.setStatus(PlayerStatus.IDLE);
      }

      if (invited != null && invited.getStatus() == PlayerStatus.WAITGAME) {
        invited.setStatus(PlayerStatus.IDLE);
      }
    }
  }

  /**
   * Returns the invitation sent by a player.
   *
   * @param inviterId the inviter player ID
   * @return the invitation, or null if none exists
   */
  public Invitation getInvitationByInviter(int inviterId) {
    return invitationsByInviter.get(inviterId);
  }

  /**
   * Returns the invitation received by a player.
   *
   * @param invitedId the invited player ID
   * @return the invitation, or null if none exists
   */
  public Invitation getInvitationByInvited(int invitedId) {
    return invitationsByInvited.get(invitedId);
  }

  /**
   * Creates a new invitation between two players.
   *
   * @param inviterId the inviter player ID
   * @param invitedId the invited player ID
   * @return true if the invitation was created, false otherwise
   */
  public synchronized boolean createInvitation(int inviterId, int invitedId) {
    OnlinePlayer inviter = players.get(inviterId);
    OnlinePlayer invited = players.get(invitedId);

    if (inviter == null || invited == null) {
      return false;
    }

    if (inviterId == invitedId) {
      return false;
    }

    if (inviter.getStatus() != PlayerStatus.IDLE || invited.getStatus() != PlayerStatus.IDLE) {
      return false;
    }

    long expiresAt = System.currentTimeMillis() + INVITATION_TIMEOUT_SECONDS * 1000;
    Invitation invitation = new Invitation(inviterId, invitedId, expiresAt);

    invitationsByInviter.put(inviterId, invitation);
    invitationsByInvited.put(invitedId, invitation);

    inviter.setStatus(PlayerStatus.WAITGAME);
    invited.setStatus(PlayerStatus.WAITGAME);

    return true;
  }

  /**
   * Removes an invitation and resets both players to idle.
   *
   * @param invitation the invitation to clear
   */
  private void clearInvitation(Invitation invitation) {
    if (invitation == null) {
      return;
    }

    invitationsByInviter.remove(invitation.getInviterId());
    invitationsByInvited.remove(invitation.getInvitedId());

    OnlinePlayer inviter = players.get(invitation.getInviterId());
    OnlinePlayer invited = players.get(invitation.getInvitedId());

    if (inviter != null) {
      inviter.setStatus(PlayerStatus.IDLE);
    }

    if (invited != null) {
      invited.setStatus(PlayerStatus.IDLE);
    }
  }

  /**
   * Declines or cancels a pending invitation.
   *
   * @param playerId the inviter or invited player ID
   * @return the removed invitation, or null if none exists
   */
  public synchronized Invitation removeInvitation(int playerId) {
    Invitation invitation = invitationsByInviter.get(playerId);

    if (invitation == null) {
      invitation = invitationsByInvited.get(playerId);
    }

    if (invitation == null) {
      return null;
    }

    clearInvitation(invitation);
    return invitation;
  }

  /**
   * Accepts the pending invitation received by a player.
   *
   * @param invitedId the invited player ID
   * @return the created lobby, or null if the invitation could not be accepted
   */
  public synchronized GameLobby acceptInvitation(int invitedId) {
    Invitation invitation = invitationsByInvited.get(invitedId);

    if (invitation == null) {
      return null;
    }

    OnlinePlayer inviter = players.get(invitation.getInviterId());
    OnlinePlayer invited = players.get(invitation.getInvitedId());

    if (invitation.isExpired() || inviter == null || invited == null) {
      clearInvitation(invitation);
      return null;
    }

    invitationsByInviter.remove(invitation.getInviterId());
    invitationsByInvited.remove(invitation.getInvitedId());

    GameLobby lobby = new GameLobby(inviter.getId(), invited.getId());
    lobbiesByPlayer.put(inviter.getId(), lobby);
    lobbiesByPlayer.put(invited.getId(), lobby);

    return lobby;
  }

  /**
   * Returns the lobby associated with a player.
   *
   * @param playerId the player ID
   * @return the lobby, or null if none exists
   */
  public GameLobby getLobbyByPlayer(int playerId) {
    return lobbiesByPlayer.get(playerId);
  }

  /**
   * Starts a game from a lobby using the selected mode.
   *
   * @param hostId the host player ID
   * @param mode the selected game mode
   * @return the created game session, or null if creation failed
   */
  /**
   * Starts a game from a lobby using the selected mode.
   *
   * @param hostId the host player ID
   * @param mode the selected game mode
   * @return the created game session, or null if creation failed
   */
  public synchronized ServerGameSession chooseMode(int hostId, GameMode mode) {
    if (mode == null) {
      return null;
    }

    GameLobby lobby = lobbiesByPlayer.get(hostId);

    if (lobby == null || lobby.getHostId() != hostId) {
      return null;
    }

    int guestId = lobby.getGuestId();

    lobbiesByPlayer.remove(hostId);
    lobbiesByPlayer.remove(guestId);

    ServerGameSession session = startNewGame(hostId, guestId, mode);

    if (session == null) {
      OnlinePlayer host = players.get(hostId);
      OnlinePlayer guest = players.get(guestId);

      if (host != null) {
        host.setStatus(PlayerStatus.IDLE);
      }

      if (guest != null) {
        guest.setStatus(PlayerStatus.IDLE);
      }
    }

    return session;
  }
}
