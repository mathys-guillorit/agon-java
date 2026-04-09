package fr.univ.bordeaux.application.network.server.clientcommand;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import fr.univ.bordeaux.application.network.server.game.GameLobby;
import fr.univ.bordeaux.application.network.server.invitation.Invitation;
import java.io.IOException;

/** Handles ACCEPT, DECLINE, and CANCEL commands. */
public class InvitationResponseHandler {

  /** Shared helper methods. */
  private final InvitationCommandSupport support;

  /**
   * Creates a response command handler.
   *
   * @param support shared helper methods
   */
  public InvitationResponseHandler(final InvitationCommandSupport support) {
    this.support = support;
  }

  /**
   * Handles ACCEPT command.
   *
   * @param handler current client handler
   * @throws IOException if sending a response fails
   */
  public void handleAccept(final ClientHandler handler) throws IOException {
    final OnlinePlayer player = support.getCurrentPlayer(handler);

    if (player == null) {
      handler.sendFromServer(InvitationCommandSupport.ERROR_NOT_LOGGED);
    } else {
      processAccept(handler, player);
    }
  }

  /**
   * Handles DECLINE command.
   *
   * @param handler current client handler
   * @throws IOException if sending a response fails
   */
  public void handleDecline(final ClientHandler handler) throws IOException {
    final OnlinePlayer player = support.getCurrentPlayer(handler);

    if (player == null) {
      handler.sendFromServer(InvitationCommandSupport.ERROR_NOT_LOGGED);
    } else {
      processDecline(handler, player);
    }
  }

  /**
   * Handles CANCEL command.
   *
   * @param handler current client handler
   * @throws IOException if sending a response fails
   */
  public void handleCancel(final ClientHandler handler) throws IOException {
    final OnlinePlayer player = support.getCurrentPlayer(handler);

    if (player == null) {
      handler.sendFromServer(InvitationCommandSupport.ERROR_NOT_LOGGED);
    } else {
      processCancel(handler, player);
    }
  }

  /**
   * Processes ACCEPT command for an authenticated player.
   *
   * @param handler current client handler
   * @param player authenticated player
   * @throws IOException if sending a response fails
   */
  private void processAccept(final ClientHandler handler, final OnlinePlayer player)
      throws IOException {
    final Invitation invitation = support.getInvitationByInvited(handler, player.getId());

    if (invitation == null) {
      handler.sendFromServer("ERROR MESSAGE=NO_PENDING_INVITATION");
    } else {
      final GameLobby lobby = support.acceptInvitation(handler, player.getId());

      if (lobby == null) {
        handler.sendFromServer("ERROR MESSAGE=INVITATION_ACCEPT_FAILED");
      } else {
        final OnlinePlayer host = support.getPlayerById(handler, lobby.getHostId());

        handler.sendFromServer("LOBBY_JOINED HOST=" + support.getPlayerNameOrUnknown(host));
        handler.sendFromServer("WAITING_MODE");

        if (host != null) {
          support.sendToPlayer(host, "INVITATION_ACCEPTED PLAYER=" + player.getName());
          support.sendToPlayer(host, "CHOOSE_MODE COMMAND=mode OPTIONS=normal|blitz");
        }
      }
    }
  }

  /**
   * Processes DECLINE command for an authenticated player.
   *
   * @param handler current client handler
   * @param player authenticated player
   * @throws IOException if sending a response fails
   */
  private void processDecline(final ClientHandler handler, final OnlinePlayer player)
      throws IOException {
    final Invitation invitation = support.getInvitationByInvited(handler, player.getId());

    if (invitation == null) {
      handler.sendFromServer("ERROR MESSAGE=NO_PENDING_INVITATION");
    } else {
      final OnlinePlayer inviter = support.getPlayerById(handler, invitation.getInviterId());

      support.removeInvitation(handler, player.getId());
      handler.sendFromServer("DECLINE_OK");
      support.sendToPlayer(inviter, "INVITATION_DECLINED PLAYER=" + player.getName());
    }
  }

  /**
   * Processes CANCEL command for an authenticated player.
   *
   * @param handler current client handler
   * @param player authenticated player
   * @throws IOException if sending a response fails
   */
  private void processCancel(final ClientHandler handler, final OnlinePlayer player)
      throws IOException {
    final Invitation invitation = support.getInvitationByInviter(handler, player.getId());

    if (invitation == null) {
      handler.sendFromServer("ERROR MESSAGE=NO_SENT_INVITATION");
    } else {
      final OnlinePlayer invited = support.getPlayerById(handler, invitation.getInvitedId());

      support.removeInvitation(handler, player.getId());
      handler.sendFromServer("CANCEL_OK");
      support.sendToPlayer(invited, "INVITATION_CANCELED PLAYER=" + player.getName());
    }
  }
}
