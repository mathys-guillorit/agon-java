package fr.univ.bordeaux.application.match;

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
   * @return The current {@link Player} whose turn it is.
   */
  Player getCurrentPlayer();

  /**
   * Accesses the current representation of the game board.
   *
   * @return The {@link AgonBoard} representing the current state of the board.
   */
  AgonBoard getAgonBoard();

  /**
   * Checks the persistence status of the current match.
   *
   * @return {@code true} if the match has been saved, {@code false} otherwise.
   */
  boolean isSaved();

  /**
   * Gets the time left for the current player's turn as a formatted string.
   *
   * @return A {@link String} representing the remaining time (e.g., "00:30").
   */
  String getCurrentPlayerRemainingTime();

  /**
   * Retrieves the remaining time for all players involved in the match.
   *
   * @return An array of {@link String} where each element represents a player's remaining time.
   */
  String[] getAllPlayersRemainingTime();

  /**
   * Determines whether the game has reached a final state (win, draw, or repetition).
   *
   * @return {@code true} if the match is finished, {@code false} otherwise.
   */
  boolean isMatchOver();

  /**
   * Provides the full list of moves played since the start of the game.
   *
   * @return A {@link List} of {@link MoveDtO} objects representing the match history.
   */
  List<MoveDtO> getHistory();

  /**
   * Returns the settings and rules defined for this match.
   *
   * @return The {@link GameConfig} associated with this match.
   */
  GameConfig getGameConfig();

  /**
   * Identifies the player who won the game.
   *
   * @return The winning {@link Player}, or {@code null} if the game is still in progress or ended in a draw.
   */
  Player getWinner();
}