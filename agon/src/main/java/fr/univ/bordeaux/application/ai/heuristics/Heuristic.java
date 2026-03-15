package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;

/**
 * Functional interface representing a heuristic evaluation function for the Agon game.
 *
 * <p>In game theory and AI, a heuristic is used to estimate the value of a game state (a node in
 * the game tree) without exploring the tree all the way to the end of the game.
 *
 * <p>This interface is the core of the <b>Strategy Pattern</b> used in this project. It allows the
 * AI to switch between different evaluation strategies (e.g., Centrality, Mobility, Mixed)
 * dynamically.
 */
public interface Heuristic {

  /**
   * Evaluates the current board state and returns a score representing the advantage of the AI
   * player.
   *
   * <p>The scoring convention used throughout the AI logic (Minimax/Alpha-Beta) is:
   *
   * <ul>
   *   <li><b>Positive Score (&gt; 0):</b> The board is favorable to the {@code aiColor}.
   *   <li><b>Negative Score (&lt; 0):</b> The board is favorable to the opponent.
   *   <li><b>Zero (0):</b> The position is neutral or balanced.
   * </ul>
   *
   * @param board   The current state of the game board to evaluate.
   * @param aiColor The color of the player for whom the score is calculated (the Maximizing
   *                player).
   * @return A {@code long} integer representing the heuristic score.
   */
  public long evaluate(AgonBoard board, Color aiColor);
}
