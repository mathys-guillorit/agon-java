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
   * @return the current player whose turn it is.
   */
  Player getCurrentPlayer();

  /**
   * @return the current state of the board.
   */
  AgonBoard getAgonBoard();

  /**
   * @return true if the match has been saved.
   */
  boolean isSaved();

  /**
   * @return the remaining time if applicable.
   */
  String getCurrentPlayerRemainingTime();

  String[] getAllPlayersRemainingTime();
  /**
   * @return true if the match is finished.
   */
  boolean isMatchOver();

  /**
   * @return the history of moves.
   */
  List<MoveDtO> getHistory();

  /**
   * @return the game configuration.
   */
  GameConfig getGameConfig();

  /**
   * @return a suggested move (hint).
   */
  Move hint();

  /**
   * @return the winner of the match, or null if game is not finished.
   */
  Player getWinner();
}
