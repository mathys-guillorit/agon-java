package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import java.util.List;

/**
 * Provides a read-only view of a match state. This interface is intended for use by UI components
 * (Observers) to prevent accidental state modifications during updates.
 */
public interface ReadOnlyMatch {

  /**
   * Retrieves the player who is currently required to move.
   *
   * @return the current player whose turn it is.
   */
  Player getCurrentPlayer();

  /**
   * Accesses the current representation of the game board.
   *
   * @return the current state of the board.
   */
  AgonBoard getAgonBoard();

  /**
   * Checks the persistence status of the current match.
   *
   * @return true if the match has been saved.
   */
  boolean isSaved();

  /**
   * Gets the time left for the current turn or match as a string.
   *
   * @return the remaining time if applicable.
   */
  String getCurrentPlayerRemainingTime();

  String[] getAllPlayersRemainingTime();

  /**
   * Determines whether the game has reached a final state.
   *
   * @return true if the match is finished.
   */
  boolean isMatchOver();

  /**
   * Provides the full list of moves played since the start of the game.
   *
   * @return the history of moves.
   */
  List<MoveDtO> getHistory();

  /**
   * Returns the settings and rules defined for this match.
   *
   * @return the game configuration.
   */
  GameConfig getGameConfig();

  /**
   * Computes or retrieves a recommended move for the current player.
   *
   * @return a suggested move (hint).
   */
  Move hint();

  /**
   * Identifies the player who won the game.
   *
   * @return the winner of the match, or null if game is not finished.
   */
  Player getWinner();
}
