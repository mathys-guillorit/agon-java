package fr.univ.bordeaux.application.network.server;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.match.MatchManager;
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
 *   <li>holding the associated {@link MatchManager},
 *   <li>providing utility methods to identify players in the session.
 * </ul>
 */
public class ServerGameSession {

  private final int gameId;

  private final OnlinePlayer whitePlayer;

  private final OnlinePlayer blackPlayer;

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
      int gameId, OnlinePlayer whitePlayer, OnlinePlayer blackPlayer, Match match) {
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
  public boolean containsPlayer(int playerId) {
    return whitePlayer.getId() == playerId || blackPlayer.getId() == playerId;
  }

  /**
   * Returns the opponent of the given player in this game session.
   *
   * <p>If the provided player ID corresponds to the white player, the black player is returned, and
   * vice versa.
   *
   * @param playerId the ID of the player
   * @return the opponent player, or null if the player is not part of this session
   */
  public OnlinePlayer getOpponent(int playerId) {
    if (whitePlayer.getId() == playerId) {
      return blackPlayer;
    }
    if (blackPlayer.getId() == playerId) {
      return whitePlayer;
    }
    return null;
  }

  /**
   * Returns the color associated with a given player in this session.
   *
   * <p>The color is determined by the role assigned at game creation: one player is white and the
   * other is black.
   *
   * @param playerId the ID of the player
   * @return {@link Color#WHITE}, {@link Color#BLACK}, or null if the player is not part of this
   *     session
   */
  public Color getColorOfPlayer(int playerId) {
    if (whitePlayer.getId() == playerId) {
      return Color.WHITE;
    }
    if (blackPlayer.getId() == playerId) {
      return Color.BLACK;
    }
    return null;
  }

  /**
   * Returns a human-readable label describing the role of a player.
   *
   * <p>This method converts the player's color into a string representation used in the network
   * protocol (e.g., "WHITE" or "BLACK").
   *
   * @param playerId the ID of the player
   * @return "WHITE", "BLACK", or "UNKNOWN" if the player is not part of this session
   */
  public String getRoleLabel(int playerId) {
    Color color = getColorOfPlayer(playerId);

    if (color == Color.WHITE) {
      return "WHITE";
    }
    if (color == Color.BLACK) {
      return "BLACK";
    }

    return "UNKNOWN";
  }

  /**
   * Indicates whether it is currently the turn of the given player.
   *
   * <p>This method compares the player's assigned color with the current player in the underlying
   * {@link Match} instance.
   *
   * @param playerId the ID of the player
   * @return true if it is this player's turn, false otherwise
   */
  public boolean isPlayersTurn(int playerId) {
    if (match == null) {
      return false;
    }

    Color color = getColorOfPlayer(playerId);
    if (color == null) {
      return false;
    }

    Player current = match.getCurrentPlayer();
    return current != null && current.getColor() == color;
  }

  /**
   * Returns a formatted string describing the role distribution in this game.
   *
   * <p>This is mainly used for debugging or initial client notification, showing which player is
   * assigned to white and which to black.
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
  public boolean playMove(int playerId, String rawMove) {
    if (!containsPlayer(playerId)) {
      return false;
    }

    if (match == null) {
      return false;
    }

    if (!isPlayersTurn(playerId)) {
      return false;
    }

    MoveParsed parsedMove = MoveProtocolParser.parse(rawMove);
    if (parsedMove == null) {
      return false;
    }

    Color playerColor = getColorOfPlayer(playerId);
    if (playerColor == null) {
      return false;
    }

    boolean replacementRequired = isReplacementMoveRequired(playerId);

    // Replacement phase: only short format like "e3" is allowed
    if (replacementRequired) {
      if (parsedMove.hasSource()) {
        return false;
      }

      Move replacementMove = new Move(-1, parsedMove.getToIndex(), playerColor);

      return match.move(replacementMove);
    }

    // Normal phase: only full format like "j5i4" is allowed
    if (!parsedMove.hasSource()) {
      return false;
    }

    Move normalMove = new Move(parsedMove.getFromIndex(), parsedMove.getToIndex(), playerColor);

    return match.move(normalMove);
  }

  /**
   * Checks whether the given player must perform a replacement move.
   *
   * <p>A replacement move is required when the underlying match indicates that the player still has
   * pieces to relocate instead of playing a normal move.
   *
   * @param playerId the ID of the player to check
   * @return true if the player must perform a replacement move, false otherwise
   */
  private boolean isReplacementMoveRequired(int playerId) {
    if (match == null) {
      return false;
    }

    Color color = getColorOfPlayer(playerId);
    if (color == null) {
      return false;
    }

    return match.isReplacementMoveRequired(color);
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
