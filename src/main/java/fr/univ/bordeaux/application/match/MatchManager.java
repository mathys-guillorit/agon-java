package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.application.match.player.Player;
import java.util.List;

/**
 * Represent Match Behavior.
 */
public interface MatchManager {

  /**
   * Move a piece.
   *
   * @param move {@link Move}
   *
   * @return true | false
   */
  boolean move(Move move);

  /**
   * Undo a turn (if enough history for it, at least 1 turn must be done).
   *
   * @return true undo succeeded else false
   */
  boolean undo();

  /**
   * redo a tun if enough history (at least 1 undo have been done before).
   *
   * @return true undo succeeded else false
   */
  boolean redo();

  /**
   * Stop turn state.
   *
   * @return true if it's possible to pause else false.
   */
  boolean pause();

  /**
   * Explicit.
   *
   * @return {@link Player}
   */
  Player getCurrentPlayer();

  /**
   * Explicit.
   *
   * @return {@link RestrictedAgonBoard}
   */
  RestrictedAgonBoard getAgonBoard();

  /**
   * Predict next turn.
   *
   * @return {@link Move}
   */
  Move hint();

  /**
   * Explicit.
   *
   * @return {@link String}
   */
  String getRemainingTime();

  /**
   * Explicit.
   *
   * @return true | false
   */
  boolean isMatchOver();

  /**
   * get the last Moves.
   *
   * @return {@link List}
   */
  List<MoveDtO> getHistory();

  /**
   * Quit the game.
   */
  void quit();
}
