package fr.univ.bordeaux.application.network.server;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.server.game.GameLobby;
import fr.univ.bordeaux.application.network.server.game.GameManager;
import fr.univ.bordeaux.application.network.server.game.GameMode;
import fr.univ.bordeaux.application.network.server.game.GameResultNotifier;
import fr.univ.bordeaux.application.network.server.game.GameService;
import fr.univ.bordeaux.application.network.server.game.ServerGameSession;
import fr.univ.bordeaux.application.network.server.game.ServerPlayerStats;
import fr.univ.bordeaux.application.network.server.invitation.Invitation;
import fr.univ.bordeaux.application.network.server.invitation.InvitationCleaner;
import fr.univ.bordeaux.application.network.server.invitation.InvitationManager;
import fr.univ.bordeaux.application.network.server.invitation.InvitationService;
import fr.univ.bordeaux.application.network.server.lifecycle.ClientConnectionRegistry;
import fr.univ.bordeaux.application.network.server.lifecycle.ServerLifecycleManager;
import fr.univ.bordeaux.application.network.server.player.PlayerRegistry;
import fr.univ.bordeaux.application.network.server.player.PlayerService;
import fr.univ.bordeaux.application.network.server.player.PlayerViewService;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Main TCP server facade coordinating lifecycle, players, games, and invitations. */
public class AgonServer {

  /** Logger used for server-level runtime failures. */
  private static final Logger LOGGER = Logger.getLogger(AgonServer.class.getName());

  /** TCP port used by this server instance. */
  private final int port;

  /** Logical discovery name of this server instance. */
  private final String name;

  /** Registry storing currently connected client handlers. */
  private final ClientConnectionRegistry connectionRegistry = new ClientConnectionRegistry();

  /** Lifecycle manager handling socket startup, shutdown, and accept loop. */
  private final ServerLifecycleManager lifecycleManager;

  /** Player service handling registration and active player state. */
  private final PlayerService playerService = new PlayerRegistry();

  /** Formatter service building player-related network responses. */
  private final PlayerViewService playerViewService = new PlayerViewService();

  /** Game service handling active matches, lobbies, and scoreboard state. */
  private final GameService gameService = new GameManager(playerService, new GameResultNotifier());

  /** Invitation service handling invitations and their state transitions. */
  private final InvitationService invitationService = new InvitationManager(playerService);

  /** Background cleaner removing expired invitations periodically. */
  private final InvitationCleaner invitationCleaner = new InvitationCleaner(invitationService);

  /**
   * Creates a new server with a specified owner name and port.
   *
   * @param port the TCP port listened to by the server
   * @param ownerName the local profile name owning this server
   */
  public AgonServer(final int port, final String ownerName) {
    this.port = port;
    this.name = "AgonServer_" + ownerName + "_" + port;
    this.lifecycleManager = new ServerLifecycleManager(port, name);
  }

  /**
   * Creates a new server using the default port (12_345).
   *
   * @param ownerName the local profile name owning this server
   */
  public AgonServer(final String ownerName) {
    this(12_345, ownerName);
  }

  /**
   * Starts the server lifecycle and invitation cleaner.
   *
   * @return {@code true} if startup succeeded, {@code false} otherwise
   */
  public boolean start() {
    final boolean started = lifecycleManager.start(this::registerNewClient);

    if (started) {
      invitationCleaner.start();
    }

    return started;
  }

  /**
   * Stops the server and all active client handlers.
   *
   * @return {@code true} if shutdown completed
   */
  public boolean stop() {
    if (!lifecycleManager.isRunning()) {
      return true;
    }

    final List<ClientHandler> clientSnapshot = connectionRegistry.snapshot();

    for (ClientHandler handler : clientSnapshot) {
      handler.stop();
    }

    connectionRegistry.clear();
    playerService.clearActivePlayers();
    gameService.clearRuntimeState();

    invitationCleaner.stop();
    lifecycleManager.stop();

    return true;
  }

  /**
   * Returns the TCP port used by this server instance.
   *
   * @return the TCP port number
   */
  public int getPort() {
    return port;
  }

  /**
   * Returns the logical discovery name of this server instance.
   *
   * @return the server discovery name
   */
  public String getName() {
    return name;
  }

  /**
   * Indicates whether the server is currently running.
   *
   * @return {@code true} if the server is running, {@code false} otherwise
   */
  public boolean isRunning() {
    return lifecycleManager.isRunning();
  }

  /**
   * Removes a client handler from the active connection registry.
   *
   * @param handler the handler to remove
   */
  public void removeClient(final ClientHandler handler) {
    connectionRegistry.remove(handler);
  }

  /**
   * Returns the number of currently connected clients.
   *
   * @return the connected client count
   */
  public int getConnectedClientsCount() {
    return connectionRegistry.count();
  }

  /**
   * Registers a player on the server or reconnects an existing one.
   *
   * @param clientId the player persistent client identifier
   * @param name the player display name
   * @param handler the handler associated with the active connection
   * @return the registered player, or {@code null} if registration fails
   */
  public OnlinePlayer registerPlayer(
      final String clientId, final String name, final ClientHandler handler) {
    final OnlinePlayer player = playerService.registerPlayer(clientId, name, handler);

    if (player != null) {
      gameService.getPlayerStats(player.getName());
    }

    return player;
  }

  /**
   * Detaches a player from the server.
   *
   * @param player the player to detach
   */
  public void removePlayer(final OnlinePlayer player) {
    if (player == null) {
      return;
    }

    invitationService.removeInvitation(player.getId());
    gameService.removeLobbyForPlayer(player.getId());
    playerService.detachPlayer(player);
  }

  /**
   * Returns the formatted list of currently active players.
   *
   * @return a protocol-compatible multi-line players response
   */
  public String getPlayersList() {
    return playerViewService.formatPlayersList(playerService.getActivePlayers());
  }

  /**
   * Returns the number of currently active players.
   *
   * @return the active player count
   */
  public int getPlayerCount() {
    return playerService.getPlayerCount();
  }

  /**
   * Returns the formatted scoreboard response.
   *
   * @return a protocol-compatible scoreboard response
   */
  public String getScoreboard() {
    return gameService.getScoreboard();
  }

  /**
   * Returns the number of currently active games.
   *
   * @return the active game count
   */
  public int getActiveGameCount() {
    return gameService.getActiveGameCount();
  }

  /**
   * Indicates whether a player is currently involved in a game.
   *
   * @param playerId the player identifier
   * @return {@code true} if the player is in a game, {@code false} otherwise
   */
  public boolean isPlayerInGame(final int playerId) {
    return gameService.isPlayerInGame(playerId);
  }

  /**
   * Returns the current game id associated with a player.
   *
   * @param playerId the player identifier
   * @return the game id, or {@code null} if none exists
   */
  public Integer getGameIdByPlayer(final int playerId) {
    return gameService.getGameIdByPlayer(playerId);
  }

  /**
   * Returns a game session by id.
   *
   * @param gameId the game identifier
   * @return the game session, or {@code null} if not found
   */
  public ServerGameSession getGameById(final int gameId) {
    return gameService.getGameById(gameId);
  }

  /**
   * Returns an active player by id.
   *
   * @param playerId the player identifier
   * @return the active player, or {@code null} if not found
   */
  public OnlinePlayer getPlayerById(final int playerId) {
    return playerService.getPlayerById(playerId);
  }

  /**
   * Starts a new game between two players.
   *
   * @param requesterId the identifier of the requesting player
   * @param targetId the identifier of the opponent player
   * @param mode the selected game mode
   * @return the created game session, or {@code null} if creation failed
   */
  public ServerGameSession startNewGame(
      final int requesterId, final int targetId, final GameMode mode) {
    return gameService.startNewGame(requesterId, targetId, mode);
  }

  /**
   * Finishes an active game session.
   *
   * @param session the game session to finish
   * @param winnerPlayerId the winner player identifier
   * @param reason the end-of-game reason
   */
  public void finishGame(
      final ServerGameSession session, final int winnerPlayerId, final String reason) {
    gameService.finishGame(session, winnerPlayerId, reason);
  }

  /**
   * Returns the formatted details of a connected player.
   *
   * @param playerId the player identifier to describe
   * @return a protocol-compatible player details response
   */
  public String getPlayerDetails(final int playerId) {
    final OnlinePlayer player = playerService.getPlayerById(playerId);

    if (player == null) {
      return playerViewService.formatPlayerDetails(null, new ServerPlayerStats("UNKNOWN"));
    }

    final ServerPlayerStats stats = gameService.getPlayerStats(player.getName());
    return playerViewService.formatPlayerDetails(player, stats);
  }

  /**
   * Returns the invitation sent by a player.
   *
   * @param inviterId the inviter player identifier
   * @return the invitation, or {@code null} if none exists
   */
  public Invitation getInvitationByInviter(final int inviterId) {
    return invitationService.getInvitationByInviter(inviterId);
  }

  /**
   * Returns the invitation received by a player.
   *
   * @param invitedId the invited player identifier
   * @return the invitation, or {@code null} if none exists
   */
  public Invitation getInvitationByInvited(final int invitedId) {
    return invitationService.getInvitationByInvited(invitedId);
  }

  /**
   * Creates a new invitation between two players.
   *
   * @param inviterId the inviter player identifier
   * @param invitedId the invited player identifier
   * @return {@code true} if the invitation was created, {@code false} otherwise
   */
  public boolean createInvitation(final int inviterId, final int invitedId) {
    return invitationService.createInvitation(inviterId, invitedId);
  }

  /**
   * Removes an invitation involving a player.
   *
   * @param playerId the inviter or invited player identifier
   * @return the removed invitation, or {@code null} if none existed
   */
  public Invitation removeInvitation(final int playerId) {
    return invitationService.removeInvitation(playerId);
  }

  /**
   * Accepts an invitation and creates the associated lobby.
   *
   * @param invitedId the invited player identifier
   * @return the created lobby, or {@code null} if acceptance failed
   */
  public GameLobby acceptInvitation(final int invitedId) {
    final Invitation invitation = invitationService.acceptInvitation(invitedId);

    if (invitation == null) {
      return null;
    }

    return gameService.createLobby(invitation.getInviterId(), invitation.getInvitedId());
  }

  /**
   * Returns the lobby associated with a player.
   *
   * @param playerId the player identifier
   * @return the corresponding lobby, or {@code null} if none exists
   */
  public GameLobby getLobbyByPlayer(final int playerId) {
    return gameService.getLobbyByPlayer(playerId);
  }

  /**
   * Starts a game from a lobby using the selected mode.
   *
   * @param hostId the host player identifier
   * @param mode the selected game mode
   * @return the created game session, or {@code null} if creation failed
   */
  public ServerGameSession chooseMode(final int hostId, final GameMode mode) {
    return gameService.chooseMode(hostId, mode);
  }

  /**
   * Creates and registers a new client handler for an accepted socket.
   *
   * @param clientSocket the accepted client socket
   */
  private void registerNewClient(final Socket clientSocket) {
    try {
      final ClientHandler handler = new ClientHandler(clientSocket, this);
      connectionRegistry.add(handler);
      lifecycleManager.submitClientHandler(handler);
    } catch (RuntimeException e) {
      LOGGER.log(Level.FINE, "[SERVER] Error while registering a client", e);

      try {
        clientSocket.close();
      } catch (Exception ignored) {
        // Ignored
      }

      throw e;
    }
  }
}
