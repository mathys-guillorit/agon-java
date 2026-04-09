package fr.univ.bordeaux.application.network.server.clientcommand;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import fr.univ.bordeaux.application.network.server.game.GameLobby;
import fr.univ.bordeaux.application.network.server.game.GameMode;
import fr.univ.bordeaux.application.network.server.game.ServerGameSession;
import fr.univ.bordeaux.application.network.server.invitation.Invitation;
import java.io.IOException;

/** Shared helpers for invitation-related server commands. */
public class InvitationCommandSupport {

  /** Message returned when a client is not authenticated. */
  public static final String ERROR_NOT_LOGGED = "ERROR MESSAGE=NOT_LOGGED_IN";

  /** Textual protocol value for normal game mode. */
  public static final String MODE_NORMAL = "normal";

  /** Textual protocol value for blitz game mode. */
  public static final String MODE_BLITZ = "blitz";

  /**
   * Parses a player identifier from protocol text.
   *
   * @param rawPlayerId raw player id string
   * @return parsed player id, or {@code null} if invalid
   */
  public Integer parsePlayerId(final String rawPlayerId) {
    Integer playerId = null;

    if (rawPlayerId != null) {
      try {
        playerId = Integer.parseInt(rawPlayerId);
      } catch (NumberFormatException ignored) {
        playerId = null;
      }
    }

    return playerId;
  }

  /**
   * Parses the selected game mode.
   *
   * @param rawMode raw protocol mode argument
   * @return matching game mode, or {@code null} if invalid
   */
  public GameMode parseMode(final String rawMode) {
    GameMode mode = null;

    if (rawMode != null && !rawMode.isBlank()) {
      if (MODE_NORMAL.equalsIgnoreCase(rawMode)) {
        mode = GameMode.NORMAL;
      } else if (MODE_BLITZ.equalsIgnoreCase(rawMode)) {
        mode = GameMode.BLITZ;
      }
    }

    return mode;
  }

  /**
   * Returns true if the player is idle.
   *
   * @param player player to inspect
   * @return true if the player is idle
   */
  public boolean isIdle(final OnlinePlayer player) {
    return player.getStatus() == PlayerStatus.IDLE;
  }

  /**
   * Returns the player name when available.
   *
   * @param player player to inspect
   * @return player name or {@code UNKNOWN}
   */
  public String getPlayerNameOrUnknown(final OnlinePlayer player) {
    String playerName = "UNKNOWN";

    if (player != null) {
      playerName = player.getName();
    }

    return playerName;
  }

  /**
   * Returns the current player linked to the handler.
   *
   * @param handler current client handler
   * @return current player, or {@code null} if not authenticated
   */
  public OnlinePlayer getCurrentPlayer(final ClientHandler handler) {
    return handler.getPlayer();
  }

  /**
   * Returns a player by id.
   *
   * @param handler current client handler
   * @param playerId player identifier
   * @return matching player, or {@code null} if not found
   */
  public OnlinePlayer getPlayerById(final ClientHandler handler, final int playerId) {
    return handler.getServer().getPlayerById(playerId);
  }

  /**
   * Creates an invitation.
   *
   * @param handler current client handler
   * @param inviterId inviter identifier
   * @param invitedId invited identifier
   * @return true if the invitation was created
   */
  public boolean createInvitation(
      final ClientHandler handler, final int inviterId, final int invitedId) {
    return handler.getServer().createInvitation(inviterId, invitedId);
  }

  /**
   * Returns the invitation targeting the given player.
   *
   * @param handler current client handler
   * @param invitedId invited player id
   * @return pending invitation, or {@code null} if none exists
   */
  public Invitation getInvitationByInvited(final ClientHandler handler, final int invitedId) {
    return handler.getServer().getInvitationByInvited(invitedId);
  }

  /**
   * Returns the invitation sent by the given player.
   *
   * @param handler current client handler
   * @param inviterId inviter player id
   * @return pending invitation, or {@code null} if none exists
   */
  public Invitation getInvitationByInviter(final ClientHandler handler, final int inviterId) {
    return handler.getServer().getInvitationByInviter(inviterId);
  }

  /**
   * Accepts an invitation for the given player.
   *
   * @param handler current client handler
   * @param invitedId invited player id
   * @return created lobby, or {@code null} on failure
   */
  public GameLobby acceptInvitation(final ClientHandler handler, final int invitedId) {
    return handler.getServer().acceptInvitation(invitedId);
  }

  /**
   * Removes an invitation.
   *
   * @param handler current client handler
   * @param playerId related player id
   */
  public void removeInvitation(final ClientHandler handler, final int playerId) {
    handler.getServer().removeInvitation(playerId);
  }

  /**
   * Returns the lobby containing the given player.
   *
   * @param handler current client handler
   * @param playerId player id
   * @return matching lobby, or {@code null} if none exists
   */
  public GameLobby getLobbyByPlayer(final ClientHandler handler, final int playerId) {
    return handler.getServer().getLobbyByPlayer(playerId);
  }

  /**
   * Chooses the game mode for the given host.
   *
   * @param handler current client handler
   * @param hostId host player id
   * @param mode selected mode
   * @return created session, or {@code null} on failure
   */
  public ServerGameSession chooseMode(
      final ClientHandler handler, final int hostId, final GameMode mode) {
    return handler.getServer().chooseMode(hostId, mode);
  }

  /**
   * Sends a message to another connected player when possible.
   *
   * @param player target player
   * @param message message to send
   */
  public void sendToPlayer(final OnlinePlayer player, final String message) {
    if (player != null) {
      final ClientHandler targetHandler = player.getHandler();

      if (targetHandler != null) {
        try {
          targetHandler.sendFromServer(message);
        } catch (IOException ignored) {
          // Ignored
        }
      }
    }
  }
}
