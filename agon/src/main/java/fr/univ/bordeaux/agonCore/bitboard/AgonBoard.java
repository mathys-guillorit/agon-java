package fr.univ.bordeaux.agonCore.bitboard;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;

/**
 * Defines the core operations and state evaluations for an Agon game board. *
 *
 * <p>This interface extends {@link RestrictedAgonBoard} to provide advanced game logic, including
 * move application, history management (undo/redo), and heuristic evaluation for AI agents.
 */
public interface AgonBoard extends RestrictedAgonBoard {

  /**
   * Attempts to apply a move to the board.
   *
   * <p>This includes standard moves and mandatory relocations. If successful, the move is added to
   * the history and any resulting captures are processed.
   *
   * @param move The {@link Move} to be applied.
   * @return {@code true} if the move was valid and successfully applied; {@code false} otherwise.
   */
  boolean applyMove(Move move);

  /**
   * Reverts the last sequence of moves played by the current player.
   *
   * <p>This method undoes the physical displacement of pieces and restores previous game state
   * variables such as relocation counters.
   *
   * @return {@code true} if a move sequence was successfully reverted; {@code false} if the history
   *     is empty.
   */
  boolean undoMove();

  /**
   * Determines if the specified player has achieved the victory condition.
   *
   * <p>In Agon, victory is typically achieved when the Queen occupies the Throne and is surrounded
   * by six adjacent friendly pawns.
   *
   * @param color The {@link Color} of the player to check.
   * @return {@code true} if the player has won the game; {@code false} otherwise.
   */
  boolean isGameWon(Color color);

  /**
   * Re-applies the last move sequence that was undone.
   *
   * <p>Restores the state that existed before the last {@link #undoMove()} call, moving pieces
   * forward and updating the game state accordingly.
   *
   * @return {@code true} if a move sequence was successfully reapplied; {@code false} if the redo
   *     stack is empty.
   */
  boolean redoMove();

  /**
   * Calculates a heuristic score for the current board state from the perspective of the given
   * player.
   *
   * <p>A higher score indicates a more favorable position for the player.
   *
   * @param color The {@link Color} of the player for whom to evaluate the score.
   * @return An integer representing the board evaluation.
   */
  int getScore(Color color);

  /**
   * Evaluates the mobility of a piece at a given index.
   *
   * <p>Mobility is defined by the number of legal destinations available to that specific piece,
   * considering movement rules and obstacles.
   *
   * @param index The tile index of the piece.
   * @return The number of legal moves for the piece, or -1 if no piece exists at the specified
   *     index.
   */
  int getMobility(int index);

  /**
   * Returns the centrality level of a specific tile.
   *
   * <p>Lower values represent tiles closer to the center (Circle 0 / Throne), which is essential
   * for enforcing the "no retreating" rule.
   *
   * @param index The tile index to check.
   * @return The circle index (0 to 5) or -1 if the index is invalid.
   */
  int getCentrality(int index);
}
