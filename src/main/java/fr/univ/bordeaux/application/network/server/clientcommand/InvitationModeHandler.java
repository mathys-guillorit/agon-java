package fr.univ.bordeaux.application.network.server.clientcommand;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import fr.univ.bordeaux.application.network.server.game.GameLobby;
import fr.univ.bordeaux.application.network.server.game.GameMode;
import fr.univ.bordeaux.application.network.server.game.ServerGameSession;
import java.io.IOException;

/** Handles MODE command. */
public class InvitationModeHandler {

  /** Shared helper methods. */
  private final InvitationCommandSupport support;

  /**
   * Creates a MODE command handler.
   *
   * @param support shared helper methods
   */
  public InvitationModeHandler(final InvitationCommandSupport support) {
    this.support = support;
  }

  /**
   * Handles MODE command.
   *
   * @param command parsed MODE command
   * @param handler current client handler
   * @throws IOException if sending a response fails
   */
  public void handle(final Command command, final ClientHandler handler) throws IOException {
    final OnlinePlayer host = support.getCurrentPlayer(handler);

    if (host == null) {
      handler.sendFromServer(InvitationCommandSupport.ERROR_NOT_LOGGED);
    } else {
      processModeSelection(command, handler, host);
    }
  }

  /**
   * Processes mode selection for an authenticated host.
   *
   * @param command parsed MODE command
   * @param handler current client handler
   * @param host authenticated host
   * @throws IOException if sending a response fails
   */
  private void processModeSelection(
      final Command command, final ClientHandler handler, final OnlinePlayer host)
      throws IOException {
    final GameMode mode = support.parseMode(command.getRawArgument());

    if (mode == null) {
      handler.sendFromServer("ERROR MESSAGE=INVALID_MODE");
    } else {
      processLobbyState(handler, host, mode);
    }
  }

  /**
   * Processes lobby state before creating the game session.
   *
   * @param handler current client handler
   * @param host authenticated host
   * @param mode selected mode
   * @throws IOException if sending a response fails
   */
  private void processLobbyState(
      final ClientHandler handler, final OnlinePlayer host, final GameMode mode)
      throws IOException {
    final int hostId = host.getId();
    final GameLobby lobby = support.getLobbyByPlayer(handler, hostId);

    if (lobby == null) {
      handler.sendFromServer("ERROR MESSAGE=NOT_IN_LOBBY");
    } else if (lobby.getHostId() != hostId) {
      handler.sendFromServer("ERROR MESSAGE=ONLY_HOST_CAN_CHOOSE_MODE");
    } else {
      final ServerGameSession session = support.chooseMode(handler, hostId, mode);

      if (session == null) {
        handler.sendFromServer("ERROR MESSAGE=GAME_CREATION_FAILED");
      } else {
        final OnlinePlayer guest = support.getPlayerById(handler, lobby.getGuestId());

        if (guest == null) {
          handler.sendFromServer("ERROR MESSAGE=OPPONENT_NOT_FOUND");
        } else {
          sendGameStartedMessages(session, mode, host, guest, handler);
        }
      }
    }
  }

  /**
   * Sends GAME_STARTED messages to both players.
   *
   * @param session created game session
   * @param mode selected game mode
   * @param host player owning the current handler
   * @param guest opponent player
   * @param handler current client handler
   * @throws IOException if sending to the current client fails
   */
  private void sendGameStartedMessages(
      final ServerGameSession session,
      final GameMode mode,
      final OnlinePlayer host,
      final OnlinePlayer guest,
      final ClientHandler handler)
      throws IOException {
    final int hostId = host.getId();
    final int guestId = guest.getId();
    final String roles = session.describeRoles();
    final String hostColor = session.getRoleLabel(hostId);
    final String guestColor = session.getRoleLabel(guestId);

    handler.sendFromServer(
        "GAME_STARTED GAME_ID="
            + session.getGameId()
            + " MODE="
            + mode.name()
            + " OPPONENT_ID="
            + guestId
            + " OPPONENT_NAME="
            + guest.getName()
            + " COLOR="
            + hostColor
            + " "
            + roles);

    support.sendToPlayer(
        guest,
        "GAME_STARTED GAME_ID="
            + session.getGameId()
            + " MODE="
            + mode.name()
            + " OPPONENT_ID="
            + hostId
            + " OPPONENT_NAME="
            + host.getName()
            + " COLOR="
            + guestColor
            + " "
            + roles);
  }
}
