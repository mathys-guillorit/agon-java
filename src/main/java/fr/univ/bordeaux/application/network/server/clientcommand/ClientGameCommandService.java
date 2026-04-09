package fr.univ.bordeaux.application.network.server.clientcommand;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import fr.univ.bordeaux.application.network.server.game.ServerGameSession;
import java.io.IOException;

/** Handles in-game commands and cleanup triggered by disconnections. */
public class ClientGameCommandService {

  /** Message returned when a client is not authenticated. */
  private static final String ERROR_NOT_LOGGED = "ERROR MESSAGE=NOT_LOGGED_IN";

  /** Creates a new game command service. */
  public ClientGameCommandService() {}

  /**
   * Handles MOVE command.
   *
   * @param command the parsed MOVE command
   * @param handler the current client handler
   * @throws IOException if sending a response fails
   */
  public void handleMove(final Command command, final ClientHandler handler) throws IOException {
    final OnlinePlayer player = handler.getPlayer();

    if (player == null) {
      handler.sendFromServer(ERROR_NOT_LOGGED);
      return;
    }

    final ServerGameSession session = getSession(handler, player);
    if (session == null) {
      sendMoveSessionError(handler, player);
      return;
    }

    final String rawMove = command.getRawArgument();
    if (rawMove == null || rawMove.isBlank()) {
      handler.sendFromServer("ERROR MESSAGE=MISSING_MOVE");
      return;
    }

    processMove(handler, player, session, rawMove);
  }

  /**
   * Handles RESIGN command.
   *
   * @param handler the current client handler
   * @throws IOException if sending a response fails
   */
  public void handleResign(final ClientHandler handler) throws IOException {
    final OnlinePlayer player = handler.getPlayer();

    if (player == null) {
      handler.sendFromServer(ERROR_NOT_LOGGED);
      return;
    }

    final ServerGameSession session = getSession(handler, player);
    if (session == null) {
      sendResignSessionError(handler, player);
      return;
    }

    final OnlinePlayer opponent = session.getOpponent(player.getId());
    if (opponent == null) {
      handler.sendFromServer("ERROR MESSAGE=OPPONENT_NOT_FOUND");
      return;
    }

    finishGame(handler, session, opponent.getId(), "OPPONENT_LEFT");
  }

  /**
   * Handles cleanup when the client disconnects unexpectedly.
   *
   * @param handler the disconnected client handler
   */
  public void handleDisconnect(final ClientHandler handler) {
    final OnlinePlayer player = handler.getPlayer();

    if (player == null) {
      return;
    }

    final ServerGameSession session = getSession(handler, player);
    if (session != null) {
      final OnlinePlayer opponent = session.getOpponent(player.getId());
      if (opponent != null) {
        finishGameSilently(handler, session, opponent.getId(), "OPPONENT_LEFT");
      }
    }

    removePlayer(handler, player);
  }

  /**
   * Processes a valid MOVE request.
   *
   * @param handler the current client handler
   * @param player the current player
   * @param session the active game session
   * @param rawMove the played move
   * @throws IOException if sending a response fails
   */
  private void processMove(
      final ClientHandler handler,
      final OnlinePlayer player,
      final ServerGameSession session,
      final String rawMove)
      throws IOException {

    final boolean moveAccepted = session.playMove(player.getId(), rawMove);

    if (!moveAccepted) {
      sendMoveRejected(handler, session, player.getId());
      return;
    }

    handler.sendFromServer("MOVE_OK " + rawMove);
    sendOpponentMove(session, player.getId(), rawMove);

    if (session.isGameOver()) {
      finishGame(handler, session, player.getId(), "NORMAL_END");
    }
  }

  /**
   * Sends the appropriate error when a move is rejected.
   *
   * @param handler the current client handler
   * @param session the active game session
   * @param playerId the current player id
   * @throws IOException if sending a response fails
   */
  private void sendMoveRejected(
      final ClientHandler handler, final ServerGameSession session, final int playerId)
      throws IOException {
    if (session.isPlayersTurn(playerId)) {
      handler.sendFromServer("ERROR MESSAGE=INVALID_MOVE");
    } else {
      handler.sendFromServer("ERROR MESSAGE=NOT_YOUR_TURN");
    }
  }

  /**
   * Sends the appropriate error when no valid session exists for MOVE.
   *
   * @param handler the current client handler
   * @param player the current player
   * @throws IOException if sending a response fails
   */
  private void sendMoveSessionError(final ClientHandler handler, final OnlinePlayer player)
      throws IOException {
    final Integer gameId = getGameId(handler, player);

    if (gameId == null) {
      handler.sendFromServer("ERROR MESSAGE=NOT_IN_GAME");
    } else {
      handler.sendFromServer("ERROR MESSAGE=GAME_NOT_FOUND");
    }
  }

  /**
   * Sends the appropriate error when no valid session exists for RESIGN.
   *
   * @param handler the current client handler
   * @param player the current player
   * @throws IOException if sending a response fails
   */
  private void sendResignSessionError(final ClientHandler handler, final OnlinePlayer player)
      throws IOException {
    final Integer gameId = getGameId(handler, player);

    if (gameId == null) {
      handler.sendFromServer("ERROR MESSAGE=NOT_IN_GAME");
    } else {
      handler.sendFromServer("ERROR MESSAGE=GAME_NOT_FOUND");
    }
  }

  /**
   * Returns the active game session for the given player.
   *
   * @param handler the current client handler
   * @param player the current player
   * @return the active session, or null if none is found
   */
  private ServerGameSession getSession(final ClientHandler handler, final OnlinePlayer player) {
    final Integer gameId = getGameId(handler, player);

    if (gameId == null) {
      return null;
    }

    return getGameById(handler, gameId);
  }

  /**
   * Returns the active game id for the given player.
   *
   * @param handler the current client handler
   * @param player the current player
   * @return the game id, or null if none exists
   */
  private Integer getGameId(final ClientHandler handler, final OnlinePlayer player) {
    return handler.getServer().getGameIdByPlayer(player.getId());
  }

  /**
   * Returns the active game session by id.
   *
   * @param handler the current client handler
   * @param gameId the game identifier
   * @return the active session, or null if none exists
   */
  private ServerGameSession getGameById(final ClientHandler handler, final Integer gameId) {
    return handler.getServer().getGameById(gameId);
  }

  /**
   * Finishes a game through the server.
   *
   * @param handler the current client handler
   * @param session the game session
   * @param winnerId the winner player id
   * @param reason the finish reason
   */
  private void finishGame(
      final ClientHandler handler,
      final ServerGameSession session,
      final int winnerId,
      final String reason) {
    handler.getServer().finishGame(session, winnerId, reason);
  }

  /**
   * Finishes a game and ignores runtime failures.
   *
   * @param handler the current client handler
   * @param session the game session
   * @param winnerId the winner player id
   * @param reason the finish reason
   */
  private void finishGameSilently(
      final ClientHandler handler,
      final ServerGameSession session,
      final int winnerId,
      final String reason) {
    try {
      finishGame(handler, session, winnerId, reason);
    } catch (RuntimeException ignored) {
      // Ignored
    }
  }

  /**
   * Removes a player from the server.
   *
   * @param handler the current client handler
   * @param player the player to remove
   */
  private void removePlayer(final ClientHandler handler, final OnlinePlayer player) {
    handler.getServer().removePlayer(player);
  }

  /**
   * Sends an opponent move notification when the opponent is connected.
   *
   * @param session the active game session
   * @param playerId the identifier of the moving player
   * @param rawMove the played move
   */
  private void sendOpponentMove(
      final ServerGameSession session, final int playerId, final String rawMove) {
    final OnlinePlayer opponent = session.getOpponent(playerId);

    if (opponent == null) {
      return;
    }

    final ClientHandler opponentHandler = opponent.getHandler();
    if (opponentHandler == null) {
      return;
    }

    try {
      opponentHandler.sendFromServer("OPPONENT_MOVE " + rawMove);
    } catch (IOException ignored) {
      // Ignored
    }
  }
}
