package fr.univ.bordeaux.application.network.server.clientcommand;

import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.protocol.CommandType;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import java.io.IOException;

/** Dispatches parsed client commands to the appropriate command service. */
public class ClientCommandProcessor {

  /** Service handling session-oriented commands. */
  private final ClientSessionCommandService sessionService;

  /** Service handling invitation and lobby-related commands. */
  private final ClientInvitationCommandService invitationService;

  /** Service handling in-game commands and disconnect cleanup. */
  private final ClientGameCommandService gameService;

  /** Creates a new client command processor. */
  public ClientCommandProcessor() {
    this.sessionService = new ClientSessionCommandService();
    this.invitationService = new ClientInvitationCommandService();
    this.gameService = new ClientGameCommandService();
  }

  /**
   * Handles one parsed client command.
   *
   * @param command the parsed command received from the client
   * @param handler the client handler owning the current socket session
   * @return {@code true} if the command requests loop termination
   * @throws IOException if sending a response fails
   */
  public boolean handleCommand(final Command command, final ClientHandler handler)
      throws IOException {
    final CommandType commandType = command.getType();

    return switch (commandType) {
      case PING -> handlePing(handler);
      case STATUS -> handleStatus(handler);
      case LOGIN -> handleLogin(command, handler);
      case PLAYERS -> handlePlayers(command, handler);
      case SCOREBOARD -> handleScoreboard(handler);
      case NEW -> handleNew(command, handler);
      case ACCEPT -> handleAccept(handler);
      case DECLINE -> handleDecline(handler);
      case CANCEL -> handleCancel(handler);
      case MODE -> handleMode(command, handler);
      case MOVE -> handleMove(command, handler);
      case RESIGN -> handleResign(handler);
      case AWAY -> handleAway(handler);
      case BACK -> handleBack(handler);
      case QUIT -> true;
      default -> handleUnknownCommand(handler);
    };
  }

  /**
   * Handles session cleanup when a client disconnects.
   *
   * @param handler the disconnected client handler
   */
  public void handleDisconnect(final ClientHandler handler) {
    gameService.handleDisconnect(handler);
  }

  /**
   * Handles the PING command.
   *
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handlePing(final ClientHandler handler) throws IOException {
    handler.sendFromServer("PONG TIME=0ms");
    return false;
  }

  /**
   * Handles the STATUS command.
   *
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handleStatus(final ClientHandler handler) throws IOException {
    sessionService.handleStatus(handler);
    return false;
  }

  /**
   * Handles the LOGIN command.
   *
   * @param command the received command
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handleLogin(final Command command, final ClientHandler handler)
      throws IOException {
    sessionService.handleLogin(command, handler);
    return false;
  }

  /**
   * Handles the PLAYERS command.
   *
   * @param command the received command
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handlePlayers(final Command command, final ClientHandler handler)
      throws IOException {
    sessionService.handlePlayers(command, handler);
    return false;
  }

  /**
   * Handles the SCOREBOARD command.
   *
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handleScoreboard(final ClientHandler handler) throws IOException {
    final String scoreboard = getScoreboard(handler);
    handler.sendFromServer(scoreboard);
    return false;
  }

  /**
   * Returns the server scoreboard for the given client handler.
   *
   * @param handler the client handler
   * @return the formatted scoreboard
   */
  private String getScoreboard(final ClientHandler handler) {
    return handler.getServer().getScoreboard();
  }

  /**
   * Handles the NEW command.
   *
   * @param command the received command
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handleNew(final Command command, final ClientHandler handler) throws IOException {
    invitationService.handleNew(command, handler);
    return false;
  }

  /**
   * Handles the ACCEPT command.
   *
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handleAccept(final ClientHandler handler) throws IOException {
    invitationService.handleAccept(handler);
    return false;
  }

  /**
   * Handles the DECLINE command.
   *
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handleDecline(final ClientHandler handler) throws IOException {
    invitationService.handleDecline(handler);
    return false;
  }

  /**
   * Handles the CANCEL command.
   *
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handleCancel(final ClientHandler handler) throws IOException {
    invitationService.handleCancel(handler);
    return false;
  }

  /**
   * Handles the MODE command.
   *
   * @param command the received command
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handleMode(final Command command, final ClientHandler handler)
      throws IOException {
    invitationService.handleMode(command, handler);
    return false;
  }

  /**
   * Handles the MOVE command.
   *
   * @param command the received command
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handleMove(final Command command, final ClientHandler handler)
      throws IOException {
    gameService.handleMove(command, handler);
    return false;
  }

  /**
   * Handles the RESIGN command.
   *
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handleResign(final ClientHandler handler) throws IOException {
    gameService.handleResign(handler);
    return false;
  }

  /**
   * Handles the AWAY command.
   *
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handleAway(final ClientHandler handler) throws IOException {
    sessionService.handleAway(handler);
    return false;
  }

  /**
   * Handles the BACK command.
   *
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handleBack(final ClientHandler handler) throws IOException {
    sessionService.handleBack(handler);
    return false;
  }

  /**
   * Handles an unknown command.
   *
   * @param handler the client handler
   * @return always {@code false}
   * @throws IOException if sending a response fails
   */
  private boolean handleUnknownCommand(final ClientHandler handler) throws IOException {
    handler.sendFromServer("ERROR MESSAGE=UNKNOWN_COMMAND");
    return false;
  }
}
