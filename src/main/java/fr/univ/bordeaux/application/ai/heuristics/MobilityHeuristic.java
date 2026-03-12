package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;

/**
 * Heuristic strategy that evaluates the board based on the <b>Mobility</b> of pieces.
 * <p>
 * This heuristic counts the number of legal moves available for each piece.
 * Its primary purpose is to avoid <b>self-blocking situations</b> where the AI might trap its own Queen
 * with its Pawns while trying to reach the center.
 * </p>
 * <p>
 * Strategically, Mobility acts as a <i>tie-breaker</i> for the {@link CentralityHeuristic}:
 * if two moves result in the same distance to the center, the AI will prefer the one that offers
 * more future possibilities (freedom of movement).
 * </p>
 */
public class MobilityHeuristic extends AbstractHeuristic {

    /**
     * Constructs the Mobility heuristic with moderate weights.
     * <p>
     * The weights are intentionally lower than those used in Centrality to ensure that
     * moving towards the goal (the Center) remains the priority.
     * </p>
     * <ul>
     * <li><b>Pawns (Weight: 5):</b> Encourages keeping pawns active, but their blockage is acceptable.</li>
     * <li><b>Queen (Weight: 20):</b> Significant enough to prevent the Queen from entering a trap,
     * but low enough not to discourage her from entering tight spaces near the Throne.</li>
     * </ul>
     */
    public MobilityHeuristic() {
        super(5, 20);
    }

    /**
     * Calculates the mobility factor by querying the board for available moves.
     * <p>
     * This method delegates the calculation to the board's move generator.
     * A higher return value indicates a piece with many options (open space), while a value of 0
     * indicates a completely blocked piece.
     * </p>
     *
     * @param board The current board state.
     * @param index The index of the cell to evaluate.
     * @return The number of legal moves available for the piece at this index.
     */
    @Override
    protected long getFactor(AgonBoard board, int index) {
        return board.getMobility(index);
    }
}