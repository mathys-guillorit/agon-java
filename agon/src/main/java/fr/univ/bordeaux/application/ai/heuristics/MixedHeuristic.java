package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;

/**
 * A composite heuristic strategy that combines **Centrality** and **Mobility** evaluations.
 * <p>
 * This class implements a <b>Linear Combination</b> of two distinct strategic goals:
 * <ul>
 * <li><b>Centrality:</b> The drive to control the center of the board.</li>
 * <li><b>Mobility:</b> The need to maintain freedom of movement and avoid blockades.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Why is this class necessary?</b><br>
 * The raw scores returned by {@link CentralityHeuristic} (e.g., ~5000) and {@link MobilityHeuristic} (e.g., ~50)
 * operate on vastly different scales. Simply adding them would make Mobility irrelevant.
 * This class allows the injection of <b>weights</b> to normalize these scores and tune the AI's personality
 * (e.g., Aggressive vs. Prudent).
 * </p>
 */
public class MixedHeuristic implements Heuristic {

    /**
     * The internal strategy for evaluating piece freedom.
     */
    private final MobilityHeuristic mobilityHeuristic;

    /**
     * The internal strategy for evaluating board control.
     */
    private final CentralityHeuristic centralityHeuristic;

    /**
     * The multiplier applied to the mobility score.
     * <p>
     * Since mobility raw scores are generally lower than centrality scores, this weight
     * is typically set higher (e.g., 5.0 or 10.0) to make mobility a significant factor
     * in the decision process.
     * </p>
     */
    private final double mobilityWeight;

    /**
     * The multiplier applied to the centrality score.
     * <p>
     * Usually set to 1.0 if the centrality is the reference metric.
     * </p>
     */
    private final double centralityWeight;

    /**
     * Constructs a Mixed Heuristic with specific weights to balance the strategies.
     * <p>
     * This constructor initializes new instances of {@link MobilityHeuristic} and {@link CentralityHeuristic}
     * with their default internal settings.
     * </p>
     *
     * @param mobilityWeight   The importance factor for the mobility score (e.g., 5.0).
     * @param centralityWeight The importance factor for the centrality score (e.g., 1.0).
     */
    MixedHeuristic(double mobilityWeight, double centralityWeight) {
        this.mobilityWeight = mobilityWeight;
        this.centralityWeight = centralityWeight;
        this.mobilityHeuristic = new MobilityHeuristic();
        this.centralityHeuristic = new CentralityHeuristic();
    }

    /**
     * Evaluates the board by calculating the weighted sum of both sub-heuristics.
     * <p>
     * The formula applied is:
     * <pre>
     * FinalScore = (MobilityRaw * MobilityWeight) + (CentralityRaw * CentralityWeight)
     * </pre>
     * The result is cast to a {@code long} to match the {@link Heuristic} interface.
     * </p>
     *
     * @param board   The current board state.
     * @param aiColor The color of the AI player.
     * @return The combined weighted score.
     */
    @Override
    public long evaluate(AgonBoard board, Color aiColor) {
        long mobilityScore = mobilityHeuristic.evaluate(board, aiColor);
        long centralityScore = centralityHeuristic.evaluate(board, aiColor);
        return (long)(mobilityScore*mobilityWeight + centralityScore*centralityWeight);
    }
}