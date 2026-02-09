package fr.univ.bordeaux.application.ai.strategy;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.heuristics.Heuristic;

/**
 * Skeletal implementation of the {@link AgonAI} interface.
 * <p>
 * This abstract class handles the common "plumbing" required for any AI strategy,
 * allowing concrete implementations (like Minimax or MCTS) to focus solely on the
 * decision logic.
 * </p>
 * <p>
 * <b>Key features managed by this class:</b>
 * <ul>
 * <li><b>Time Management:</b> Automatically tracks start time and provides a helper method
 * to check if execution time is running out.</li>
 * <li><b>Heuristic Storage:</b> Holds the reference to the evaluation strategy.</li>
 * <li><b>Performance Metrics:</b> Tracks the number of nodes visited ({@code nodeCount}) for debugging.</li>
 * </ul>
 * </p>
 */
public abstract class AbstractAgonAI implements AgonAI {

    /**
     * The heuristic strategy used to evaluate board positions.
     * This allows for the dynamic injection of different evaluation logic (e.g., Centrality vs Mobility).
     */
    protected final Heuristic heuristic;

    /**
     * The color played by this AI agent (White or Black).
     */
    protected Color color;

    /**
     * The maximum time allowed for calculation in milliseconds. Default is 5000ms.
     */
    protected long timeLimit = 5000;

    /**
     * The timestamp (in milliseconds) when the current move calculation started.
     */
    protected long startTime;

    /**
     * A counter for the number of game states (nodes) analyzed during the current move calculation.
     * Useful for performance profiling.
     */
    protected long nodeCount = 0;

    /**
     * Constructs the AI with a specific heuristic.
     *
     * @param heuristic The evaluation function to use.
     */
    public AbstractAgonAI(Heuristic heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeLimit(long millis) {
        this.timeLimit = millis;
    }

    /**
     * Checks if there is still time allocated for calculation.
     * <p>
     * This method includes a <b>safety buffer of 50ms</b> to ensure the AI returns
     * before the strict timeout, avoiding disqualification.
     * It should be called frequently within the search loops (e.g., inside Minimax recursion).
     * </p>
     *
     * @return {@code true} if the elapsed time is within the safe limit, {@code false} otherwise.
     */
    protected boolean isTimeRemaining() {
        return (System.currentTimeMillis() - startTime) < (timeLimit - 50);
    }

    /**
     * Returns the total number of nodes visited during the last move calculation.
     *
     * @return The node count (performance metric).
     */
    public long getNodeCount() {
        return nodeCount;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Implementation Note:</b> This method is {@code final} to enforce the initialization
     * of the {@code startTime} before any calculation begins. It delegates the actual
     * logic to the abstract {@link #computeMove(AgonBoard)} method.
     * </p>
     */
    @Override
    public final Move getBestMove(AgonBoard board) {
        this.startTime = System.currentTimeMillis();
        return computeMove(board);
    }

    /**
     * Abstract method where the specific AI logic must be implemented.
     * <p>
     * Concrete subclasses (e.g., Minimax, Mcts) must override this method to
     * determine the best move. Implementations are expected to respect the
     * {@link #isTimeRemaining()} check.
     * </p>
     *
     * @param board The current board state.
     * @return The best move found by the specific algorithm.
     */
    protected abstract Move computeMove(AgonBoard board);
}