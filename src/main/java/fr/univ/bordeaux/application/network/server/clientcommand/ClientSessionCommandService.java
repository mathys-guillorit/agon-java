package fr.univ.bordeaux.application.network.server.clientcommand;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import java.io.IOException;
import java.util.Locale;

/** Handles session-related commands such as LOGIN, STATUS, PLAYERS, AWAY, and BACK. */
public class ClientSessionCommandService {

  /** Message returned when a client is not authenticated. */
  private static final String ERROR_NOT_LOGGED = "ERROR MESSAGE=NOT_LOGGED_IN";

  /** Creates a new session command service. */
  public ClientSessionCommandService() {
    // Explicit constructor required by PMD.
  }

  /**
   * Handles STATUS command.
   *
   * @param handler the current client handler
   * @throws IOException if sending a response fails
   */
  public void handleStatus(final ClientHandler handler) throws IOException {
    handler.sendFromServer(buildStatusResponse(handler));
  }

  /**
   * Handles LOGIN command.
   *
   * @param command the parsed LOGIN command
   * @param handler the current client handler
   * @throws IOException if sending a response fails
   */
  public void handleLogin(final Command command, final ClientHandler handler) throws IOException {
    final String response;

    if (hasLoggedPlayer(handler)) {
      response = "ERROR ALREADY_LOGGED_IN";
    } else {
      response = buildLoginResponse(command, handler);
    }

    handler.sendFromServer(response);
  }

  /**
   * Handles PLAYERS command.
   *
   * @param command the parsed PLAYERS command
   * @param handler the current client handler
   * @throws IOException if sending a response fails
   */
  public void handlePlayers(final Command command, final ClientHandler handler) throws IOException {
    handler.sendFromServer(buildPlayersResponse(command, handler));
  }

  /**
   * Handles AWAY command.
   *
   * @param handler the current client handler
   * @throws IOException if sending a response fails
   */
  public void handleAway(final ClientHandler handler) throws IOException {
    final OnlinePlayer player = handler.getPlayer();
    final String response;

    if (player == null) {
      response = ERROR_NOT_LOGGED;
    } else if (!isIdle(player)) {
      response = "ERROR MESSAGE=CANNOT_SET_AWAY_NOW";
    } else {
      player.setStatus(PlayerStatus.AWAY);
      response = "AWAY_OK STATUS=away";
    }

    handler.sendFromServer(response);
  }

  /**
   * Handles BACK command.
   *
   * @param handler the current client handler
   * @throws IOException if sending a response fails
   */
  public void handleBack(final ClientHandler handler) throws IOException {
    final OnlinePlayer player = handler.getPlayer();
    final String response;

    if (player == null) {
      response = ERROR_NOT_LOGGED;
    } else if (player.getStatus() != PlayerStatus.AWAY) {
      response = "ERROR MESSAGE=CANNOT_SET_BACK_NOW";
    } else {
      player.setStatus(PlayerStatus.IDLE);
      response = "BACK_OK STATUS=idle";
    }

    handler.sendFromServer(response);
  }

  /**
   * Builds the STATUS response.
   *
   * @param handler the current client handler
   * @return formatted STATUS response
   */
  private String buildStatusResponse(final ClientHandler handler) {
    return "STATUS_OK port="
        + getServerPort(handler)
        + " clients="
        + getConnectedClientsCount(handler)
        + " players="
        + getPlayerCount(handler)
        + " games="
        + getActiveGameCount(handler);
  }

  /**
   * Builds the LOGIN response.
   *
   * @param command the parsed LOGIN command
   * @param handler the current client handler
   * @return formatted LOGIN response
   */
  private String buildLoginResponse(final Command command, final ClientHandler handler) {
    final String name = command.getArgs().get("NAME");
    final String clientId = command.getArgs().get("CLIENT_ID");

    if (name == null || name.isBlank()) {
      return "ERROR MESSAGE=MISSING_NAME";
    }

    if (clientId == null || clientId.isBlank()) {
      return "ERROR MESSAGE=MISSING_CLIENT_ID";
    }

    registerPlayer(handler, clientId, name);

    final OnlinePlayer player = handler.getPlayer();
    if (player == null) {
      return "ERROR MESSAGE=LOGIN_FAILED";
    }

    return buildWelcomeResponse(player);
  }

  /**
   * Builds the PLAYERS response.
   *
   * @param command the parsed PLAYERS command
   * @param handler the current client handler
   * @return formatted PLAYERS response
   */
  private String buildPlayersResponse(final Command command, final ClientHandler handler) {
    final String playerIdValue = command.getRawArgument();

    if (playerIdValue == null || playerIdValue.isBlank()) {
      return getPlayersList(handler);
    }

    try {
      final int playerId = Integer.parseInt(playerIdValue);
      return getPlayerDetails(handler, playerId);
    } catch (NumberFormatException exception) {
      return "ERROR MESSAGE=INVALID_PLAYER_ID";
    }
  }

  /**
   * Builds the WELCOME response for a newly logged player.
   *
   * @param player the authenticated player
   * @return formatted WELCOME response
   */
  private String buildWelcomeResponse(final OnlinePlayer player) {
    return "WELCOME ID="
        + player.getId()
        + " NAME="
        + player.getName()
        + " STATUS="
        + formatStatus(player.getStatus());
  }

  /**
   * Registers a player on the server.
   *
   * @param handler the current client handler
   * @param clientId the client identifier
   * @param name the player name
   */
  private void registerPlayer(
      final ClientHandler handler, final String clientId, final String name) {
    handler.setPlayer(handler.getServer().registerPlayer(clientId, name, handler));
  }

  /**
   * Indicates whether the current handler already owns a player.
   *
   * @param handler the current client handler
   * @return {@code true} if a player is already attached, {@code false} otherwise
   */
  private boolean hasLoggedPlayer(final ClientHandler handler) {
    return handler.getPlayer() != null;
  }

  /**
   * Returns the server port.
   *
   * @param handler the current client handler
   * @return server TCP port
   */
  private int getServerPort(final ClientHandler handler) {
    return handler.getServer().getPort();
  }

  /**
   * Returns the number of connected clients.
   *
   * @param handler the current client handler
   * @return number of connected clients
   */
  private int getConnectedClientsCount(final ClientHandler handler) {
    return handler.getServer().getConnectedClientsCount();
  }

  /**
   * Returns the number of registered players.
   *
   * @param handler the current client handler
   * @return number of players
   */
  private int getPlayerCount(final ClientHandler handler) {
    return handler.getServer().getPlayerCount();
  }

  /**
   * Returns the number of active games.
   *
   * @param handler the current client handler
   * @return number of active games
   */
  private int getActiveGameCount(final ClientHandler handler) {
    return handler.getServer().getActiveGameCount();
  }

  /**
   * Returns the formatted list of players.
   *
   * @param handler the current client handler
   * @return players list response
   */
  private String getPlayersList(final ClientHandler handler) {
    return handler.getServer().getPlayersList();
  }

  /**
   * Returns the details for a specific player.
   *
   * @param handler the current client handler
   * @param playerId the player id
   * @return player details response
   */
  private String getPlayerDetails(final ClientHandler handler, final int playerId) {
    return handler.getServer().getPlayerDetails(playerId);
  }

  /**
   * Indicates whether a player is currently idle.
   *
   * @param player the player to inspect
   * @return {@code true} if the player is idle, {@code false} otherwise
   */
  private boolean isIdle(final OnlinePlayer player) {
    return player.getStatus() == PlayerStatus.IDLE;
  }

  /**
   * Formats a player status using the protocol convention.
   *
   * @param status the player status
   * @return the formatted status string
   */
  private String formatStatus(final PlayerStatus status) {
    return status.name().toLowerCase(Locale.ROOT);
  }
}
