package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;

/**
 * Heuristic strategy that evaluates the board based on the <b>Centrality</b> of pieces.
 *
 * <p>This heuristic encourages the AI to move its pieces towards the center of the board (the
 * Throne). Unlike a linear approach, this implementation uses a <b>Quadratic function</b> to
 * calculate the score. This means that the value of a square increases exponentially as it gets
 * closer to the center, creating a strong "gravity" effect that prioritizes the inner rings over
 * the outer rings.
 */
public class CentralityHeuristic extends AbstractHeuristic {

  /**
   * Constructs the Centrality heuristic with specific weights.
   *
   * <p>The weights are configured as follows:
   *
   * <ul>
   *   <li><b>Pawns (Weight: 5):</b> Encouraged to control the center, but are expendable.
   *   <li><b>Queen (Weight: 200):</b> Heavily prioritized. The Queen's position is the primary
   *       factor in this evaluation to ensure she reaches the Throne.
   * </ul>
   */
  public CentralityHeuristic() {
    super(5, 200);
  }

  /**
   * Calculates the centrality factor using a quadratic inversion of the distance.
   *
   * <p>The formula used is: <code>(6 - distance)^2</code>. <br>
   * Examples of return values:
   *
   * <ul>
   *   <li><b>Throne (Distance 0):</b> (6-0)^2 = 36 (Maximum score)
   *   <li><b>Inner Ring (Distance 1):</b> (6-1)^2 = 25
   *   <li><b>Outer Ring (Distance 5):</b> (6-5)^2 = 1 (Minimum score)
   * </ul>
   *
   * <p>This non-linear progression makes the inner squares significantly more valuable than the
   * outer ones.
   *
   * @param board The current board state.
   * @param index The index of the cell to evaluate.
   * @return The squared inverted distance to the center (ranging from 1 to 36).
   */
  @Override
  protected long getFactor(AgonBoard board, int index) {
    long dist = board.getCentrality(index);
    long inverted = 6 - dist;
    return inverted * inverted;
  }
}
