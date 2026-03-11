package fr.univ.bordeaux.application.ai.strategy.minimax;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAI;
import fr.univ.bordeaux.application.ai.heuristics.Heuristic;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;
import fr.univ.bordeaux.agonCore.agonElements.Move;

import java.util.List;

/**
 * A concrete AI strategy implementing the <b>Minimax algorithm with Alpha-Beta Pruning</b>
 * and optional <b>Iterative Deepening</b>.
 * <p>
 * This strategy constructs a game tree to a specified depth and evaluates the terminal nodes
 * using the injected {@link Heuristic}.
 * </p>
 * <p>
 * If Iterative Deepening is enabled, the algorithm searches progressively deeper (depth 1, then 2, etc.)
 * until the maximum depth is reached or the time limit is exceeded. This ensures a valid move is always
 * ready even if the computation is interrupted.
 * </p>
 */
public class MinimaxStrategy extends AbstractAgonAI {

    private final int maxDepth;
    private final boolean iterativeDeepening;
    private final long timeLimitMillis;

    /** Flag to immediately halt the recursive search when time runs out. */
    private volatile boolean timeoutReached = false;

    /**
     * Constructs a Minimax strategy with Iterative Deepening and time management.
     *
     * @param heuristic          The evaluation function used for leaf nodes.
     * @param color              The color played by this AI.
     * @param maxDepth           The absolute maximum depth limit for the recursion.
     * @param iterativeDeepening {@code true} to enable Iterative Deepening, {@code false} for standard Minimax.
     * @param timeLimitSeconds   The maximum allowed calculation time per move in seconds.
     */
    public MinimaxStrategy(Heuristic heuristic, Color color, int maxDepth, boolean iterativeDeepening, int timeLimitSeconds) {
        super(heuristic, color);
        this.maxDepth = maxDepth;
        this.iterativeDeepening = iterativeDeepening;
        this.timeLimitMillis = timeLimitSeconds * 1000L;
    }

    @Override
    protected Move computeMove(AgonBoard board) {
        this.timeoutReached = false;
        long startTime = System.currentTimeMillis();

        List<Move> legalMoves = board.generateLegalMoves(this.color);
        if (legalMoves.isEmpty()) return null;

        Move absoluteBestMove = legalMoves.getFirst();

        if (!this.iterativeDeepening) {
            return runMinimaxForDepth(board, legalMoves, this.maxDepth, startTime);
        }

        for (int currentDepth = 1; currentDepth <= this.maxDepth; currentDepth++) {

            Move bestMoveForCurrentDepth = runMinimaxForDepth(board, legalMoves, currentDepth, startTime);

            if (!this.timeoutReached && bestMoveForCurrentDepth != null) {
                absoluteBestMove = bestMoveForCurrentDepth;
            } else {
                break;
            }
        }

        return absoluteBestMove;
    }

    /**
     * Executes the root layer of the Minimax algorithm for a specific target depth.
     */
    private Move runMinimaxForDepth(AgonBoard board, List<Move> legalMoves, int targetDepth, long startTime) {
        Move bestMove = legalMoves.getFirst();
        long maxScore = Long.MIN_VALUE;
        long alpha = Long.MIN_VALUE;
        long beta = Long.MAX_VALUE;

        for (Move move : legalMoves) {
            checkTimeLimit(startTime);
            if (this.timeoutReached) {
                break;
            }

            board.applyMove(move);
            this.nodeCount++;

            long score = minimax(board, targetDepth - 1, false, alpha, beta, startTime);

            board.undoMove();

            if (this.timeoutReached) {
                break;
            }

            if (score > maxScore) {
                maxScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, score);
        }
        return bestMove;
    }

    /**
     * Recursive Minimax function with Alpha-Beta pruning.
     */
    private long minimax(AgonBoard board, int depth, boolean isMaximizingPlayer, long alpha, long beta, long startTime) {

        checkTimeLimit(startTime);

        if (this.timeoutReached) {
            return 0;
        }

        Color opponentColor = (this.color == Color.WHITE) ? Color.BLACK : Color.WHITE;

        if (board.isGameWon(this.color)){
            return 1000000L + depth;
        }
        if (board.isGameWon(opponentColor)) {
            return -1000000L - depth;
        }
        if (depth == 0) {
            return this.heuristic.evaluate(board, this.color);
        }

        if (isMaximizingPlayer) {
            List<Move> moves = board.generateLegalMoves(this.color);
            if (moves.isEmpty()) {
                return this.heuristic.evaluate(board, this.color);
            }
            long maxEval = Long.MIN_VALUE;
            for (Move move : moves) {
                board.applyMove(move);
                this.nodeCount++;

                long eval = minimax(board, depth - 1, false, alpha, beta, startTime);

                board.undoMove();

                if (this.timeoutReached) return 0;

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            List<Move> moves = board.generateLegalMoves(opponentColor);
            if (moves.isEmpty()) {
                return this.heuristic.evaluate(board, this.color);
            }
            long minEval = Long.MAX_VALUE;
            for (Move move : moves) {
                board.applyMove(move);
                this.nodeCount++;

                long eval = minimax(board, depth - 1, true, alpha, beta, startTime);

                board.undoMove();

                if (this.timeoutReached) return 0;

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    /**
     * Checks if the allotted time has been exceeded and sets the flag if necessary.
     */
    private void checkTimeLimit(long startTime) {
        if (!this.timeoutReached && (System.currentTimeMillis() - startTime > this.timeLimitMillis)) {
            this.timeoutReached = true;
        }
    }
}