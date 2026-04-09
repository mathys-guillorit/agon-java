package fr.univ.bordeaux.application.network.server.clientcommand;

import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import java.io.IOException;

/** Facade handling invitation, lobby, and mode-selection commands. */
public class ClientInvitationCommandService {

  /** Shared helper methods for invitation commands. */
  private final InvitationCommandSupport support;

  /** Handler dedicated to NEW command. */
  private final InvitationNewHandler newHandler;

  /** Handler dedicated to ACCEPT, DECLINE, and CANCEL commands. */
  private final InvitationResponseHandler responseHandler;

  /** Handler dedicated to MODE command. */
  private final InvitationModeHandler modeHandler;

  /** Creates a new invitation command service. */
  public ClientInvitationCommandService() {
    this.support = new InvitationCommandSupport();
    this.newHandler = new InvitationNewHandler(support);
    this.responseHandler = new InvitationResponseHandler(support);
    this.modeHandler = new InvitationModeHandler(support);
  }

  /**
   * Handles NEW command.
   *
   * @param command parsed NEW command
   * @param handler current client handler
   * @throws IOException if sending a response fails
   */
  public void handleNew(final Command command, final ClientHandler handler) throws IOException {
    newHandler.handle(command, handler);
  }

  /**
   * Handles ACCEPT command.
   *
   * @param handler current client handler
   * @throws IOException if sending a response fails
   */
  public void handleAccept(final ClientHandler handler) throws IOException {
    responseHandler.handleAccept(handler);
  }

  /**
   * Handles DECLINE command.
   *
   * @param handler current client handler
   * @throws IOException if sending a response fails
   */
  public void handleDecline(final ClientHandler handler) throws IOException {
    responseHandler.handleDecline(handler);
  }

  /**
   * Handles CANCEL command.
   *
   * @param handler current client handler
   * @throws IOException if sending a response fails
   */
  public void handleCancel(final ClientHandler handler) throws IOException {
    responseHandler.handleCancel(handler);
  }

  /**
   * Handles MODE command.
   *
   * @param command parsed MODE command
   * @param handler current client handler
   * @throws IOException if sending a response fails
   */
  public void handleMode(final Command command, final ClientHandler handler) throws IOException {
    modeHandler.handle(command, handler);
  }
}
