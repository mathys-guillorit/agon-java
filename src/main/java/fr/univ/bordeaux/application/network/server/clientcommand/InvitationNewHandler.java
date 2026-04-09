package fr.univ.bordeaux.application.network.server.clientcommand;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import java.io.IOException;

/** Handles NEW invitation command. */
public class InvitationNewHandler {

  /** Shared helper methods. */
  private final InvitationCommandSupport support;

  /**
   * Creates a NEW command handler.
   *
   * @param support shared helper methods
   */
  public InvitationNewHandler(final InvitationCommandSupport support) {
    this.support = support;
  }

  /**
   * Handles NEW command.
   *
   * @param command parsed NEW command
   * @param handler current client handler
   * @throws IOException if sending a response fails
   */
  public void handle(final Command command, final ClientHandler handler) throws IOException {
    final OnlinePlayer requester = support.getCurrentPlayer(handler);

    if (requester == null) {
      handler.sendFromServer(InvitationCommandSupport.ERROR_NOT_LOGGED);
    } else {
      handleAuthenticatedRequest(command, handler, requester);
    }
  }

  /**
   * Handles NEW command for an authenticated player.
   *
   * @param command parsed NEW command
   * @param handler current client handler
   * @param requester authenticated requester
   * @throws IOException if sending a response fails
   */
  private void handleAuthenticatedRequest(
      final Command command, final ClientHandler handler, final OnlinePlayer requester)
      throws IOException {
    final Integer targetId = support.parsePlayerId(command.getArgs().get("PLAYER_ID"));

    if (targetId == null) {
      handler.sendFromServer("ERROR MESSAGE=INVALID_PLAYER_ID");
    } else {
      processTargetPlayer(handler, requester, targetId.intValue());
    }
  }

  /**
   * Processes the selected target player.
   *
   * @param handler current client handler
   * @param requester invitation requester
   * @param targetId selected target id
   * @throws IOException if sending a response fails
   */
  private void processTargetPlayer(
      final ClientHandler handler, final OnlinePlayer requester, final int targetId)
      throws IOException {
    final OnlinePlayer target = support.getPlayerById(handler, targetId);
    final int requesterId = requester.getId();

    if (target == null) {
      handler.sendFromServer("ERROR MESSAGE=PLAYER_NOT_FOUND");
    } else if (target.getId() == requesterId) {
      handler.sendFromServer("ERROR MESSAGE=CANNOT_PLAY_SELF");
    } else if (!support.isIdle(requester)) {
      handler.sendFromServer("ERROR MESSAGE=YOU_ARE_BUSY");
    } else if (!support.isIdle(target)) {
      handler.sendFromServer("ERROR MESSAGE=PLAYER_BUSY");
    } else if (!support.createInvitation(handler, requesterId, targetId)) {
      handler.sendFromServer("ERROR MESSAGE=INVITATION_FAILED");
    } else {
      sendInvitationMessages(handler, requester, target);
    }
  }

  /**
   * Sends success messages after invitation creation.
   *
   * @param handler current client handler
   * @param requester invitation requester
   * @param target invited player
   * @throws IOException if sending to requester fails
   */
  private void sendInvitationMessages(
      final ClientHandler handler, final OnlinePlayer requester, final OnlinePlayer target)
      throws IOException {
    handler.sendFromServer("INVITATION_SENT PLAYER=" + target.getName() + " TIMEOUT=300s");
    support.sendToPlayer(
        target, "INVITATION_RECEIVED FROM=" + requester.getName() + " EXPIRES=300s");
  }
}
