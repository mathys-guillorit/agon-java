package fr.univ.bordeaux.application.ai.strategy;

import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;

/**
 * Common interface for all Artificial Intelligence strategies in the Agon game.
 *
 * <p>This interface defines the <b>Strategy Pattern</b> contract that all AI implementations (e.g.,
 * Minimax, MCTS) must follow. It ensures that the game controller can interact with any AI agent
 * interchangeably without knowing the specific algorithm used.
 */
public interface AgonAi {

  /**
   * Computes and returns the best possible move for the current game state.
   *
   * <p>The implementation of this method contains the core logic of the AI strategy (e.g.,
   * exploring the game tree with Minimax, running Monte Carlo simulations). The method is expected
   * to return a valid {@link Move} within the time limit set by {@link #setTimeLimit(long)}.
   *
   * @param board The current state of the game board.
   * @return The optimal {@link Move} calculated by the AI strategy.
   */
  Move getBestMove(AgonBoard board);

  /**
   * Sets the maximum execution time allowed for the AI to calculate its move.
   *
   * <p>This constraint is critical for tournament play or to ensure a responsive user experience.
   * AI implementations must monitor the elapsed time during their search (e.g., using Iterative
   * Deepening or checking the clock between MCTS simulations) and return the best move found so far
   * if the limit is reached.
   *
   * @param millis The time limit in milliseconds (e.g., 2000 for 2 seconds).
   */
  void setTimeLimit(long millis);
}
