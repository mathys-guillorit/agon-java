package fr.univ.bordeaux.application.network.server.game;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.protocol.MoveParsed;
import fr.univ.bordeaux.application.network.protocol.MoveProtocolParser;

/**
 * Represents a single active game session on the server.
 *
 * <p>This class is responsible for:
 *
 * <ul>
 *   <li>linking two connected players to a game instance,
 *   <li>holding the associated match,
 *   <li>providing utility methods to identify players in the session.
 * </ul>
 */
public class ServerGameSession {

  /** Unique identifier of the game session. */
  private final int gameId;

  /** Player assigned to the white side. */
  private final OnlinePlayer whitePlayer;

  /** Player assigned to the black side. */
  private final OnlinePlayer blackPlayer;

  /** Underlying match instance used to validate and apply moves. */
  private final Match match;

  /**
   * Creates a new game session between two players.
   *
   * @param gameId unique identifier of the game
   * @param whitePlayer first player
   * @param blackPlayer second player
   * @param match the match associated with this game
   */
  public ServerGameSession(
      final int gameId,
      final OnlinePlayer whitePlayer,
      final OnlinePlayer blackPlayer,
      final Match match) {
    this.gameId = gameId;
    this.whitePlayer = whitePlayer;
    this.blackPlayer = blackPlayer;
    this.match = match;
  }

  /**
   * Returns the unique identifier of this game session.
   *
   * @return the game ID
   */
  public int getGameId() {
    return gameId;
  }

  /**
   * Returns the first player.
   *
   * @return player 1
   */
  public OnlinePlayer getwhitePlayer() {
    return whitePlayer;
  }

  /**
   * Returns the second player.
   *
   * @return player 2
   */
  public OnlinePlayer getblackPlayer() {
    return blackPlayer;
  }

  /**
   * Returns the match associated with this game.
   *
   * @return the match
   */
  public Match getMatch() {
    return match;
  }

  /**
   * Checks whether a player is part of this game session.
   *
   * @param playerId the player ID to check
   * @return true if the player belongs to this session, false otherwise
   */
  public boolean containsPlayer(final int playerId) {
    return whitePlayer.getId() == playerId || blackPlayer.getId() == playerId;
  }

  /**
   * Returns the opponent of the given player in this game session.
   *
   * @param playerId the ID of the player
   * @return the opponent player, or null if the player is not part of this session
   */
  public OnlinePlayer getOpponent(final int playerId) {
    OnlinePlayer opponent = null;

    if (whitePlayer.getId() == playerId) {
      opponent = blackPlayer;
    } else if (blackPlayer.getId() == playerId) {
      opponent = whitePlayer;
    }

    return opponent;
  }

  /**
   * Returns the color associated with a given player in this session.
   *
   * @param playerId the ID of the player
   * @return {@link Color#WHITE}, {@link Color#BLACK}, or null if the player is not part of this
   *     session
   */
  public Color getColorOfPlayer(final int playerId) {
    Color color = null;

    if (whitePlayer.getId() == playerId) {
      color = Color.WHITE;
    } else if (blackPlayer.getId() == playerId) {
      color = Color.BLACK;
    }

    return color;
  }

  /**
   * Returns a human-readable label describing the role of a player.
   *
   * @param playerId the ID of the player
   * @return "WHITE", "BLACK", or "UNKNOWN" if the player is not part of this session
   */
  public String getRoleLabel(final int playerId) {
    final Color color = getColorOfPlayer(playerId);
    String roleLabel = "UNKNOWN";

    if (color == Color.WHITE) {
      roleLabel = "WHITE";
    } else if (color == Color.BLACK) {
      roleLabel = "BLACK";
    }

    return roleLabel;
  }

  /**
   * Indicates whether it is currently the turn of the given player.
   *
   * @param playerId the ID of the player
   * @return true if it is this player's turn, false otherwise
   */
  public boolean isPlayersTurn(final int playerId) {
    boolean playersTurn = false;

    if (match != null) {
      final Color color = getColorOfPlayer(playerId);

      if (color != null) {
        final Player currentPlayer = getCurrentPlayer();

        if (currentPlayer != null && currentPlayer.getColor() == color) {
          playersTurn = true;
        }
      }
    }

    return playersTurn;
  }

  /**
   * Returns the current player of the underlying match.
   *
   * @return the current player, or null if unavailable
   */
  private Player getCurrentPlayer() {
    Player currentPlayer = null;

    if (match != null) {
      currentPlayer = match.getCurrentPlayer();
    }

    return currentPlayer;
  }

  /**
   * Returns a formatted string describing the role distribution in this game.
   *
   * @return a string formatted as "WHITE=name BLACK=name"
   */
  public String describeRoles() {
    return "WHITE=" + whitePlayer.getName() + " BLACK=" + blackPlayer.getName();
  }

  /**
   * Applies a move to the underlying server-side match.
   *
   * @param playerId the ID of the player attempting the move
   * @param rawMove the compact move text (e.g. "e2e4")
   * @return true if the move was accepted and applied, false otherwise
   */
  public boolean playMove(final int playerId, final String rawMove) {
    boolean moveAccepted = false;

    if (containsPlayer(playerId) && match != null && isPlayersTurn(playerId)) {
      final MoveParsed parsedMove = MoveProtocolParser.parse(rawMove);

      if (parsedMove != null) {
        final Color playerColor = getColorOfPlayer(playerId);

        if (playerColor != null) {
          final boolean replacementMove = isReplacementMoveRequired(playerId);

          if (replacementMove) {
            moveAccepted = playReplacementMove(parsedMove, playerColor);
          } else {
            moveAccepted = playNormalMove(parsedMove, playerColor);
          }
        }
      }
    }

    return moveAccepted;
  }

  /**
   * Applies a replacement move when the current phase requires it.
   *
   * @param parsedMove the parsed move
   * @param playerColor the moving player color
   * @return true if the move was accepted, false otherwise
   */
  private boolean playReplacementMove(final MoveParsed parsedMove, final Color playerColor) {
    boolean moveAccepted = false;

    if (!parsedMove.hasSource()) {
      final Move replacementMove = new Move(-1, parsedMove.getToIndex(), playerColor);
      moveAccepted = match.move(replacementMove);
    }

    return moveAccepted;
  }

  /**
   * Applies a normal move when the current phase allows it.
   *
   * @param parsedMove the parsed move
   * @param playerColor the moving player color
   * @return true if the move was accepted, false otherwise
   */
  private boolean playNormalMove(final MoveParsed parsedMove, final Color playerColor) {
    boolean moveAccepted = false;

    if (parsedMove.hasSource()) {
      final Move normalMove =
          new Move(parsedMove.getFromIndex(), parsedMove.getToIndex(), playerColor);
      moveAccepted = match.move(normalMove);
    }

    return moveAccepted;
  }

  /**
   * Checks whether the given player must perform a replacement move.
   *
   * @param playerId the ID of the player to check
   * @return true if the player must perform a replacement move, false otherwise
   */
  private boolean isReplacementMoveRequired(final int playerId) {
    final Color color = getColorOfPlayer(playerId);
    return match != null && color != null && match.isReplacementMoveRequired(color);
  }

  /**
   * Indicates whether this game session has ended.
   *
   * @return true if the underlying match is over, false otherwise
   */
  public boolean isGameOver() {
    return match != null && match.isMatchOver();
  }
}
