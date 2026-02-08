package fr.univ.bordeaux.application.ai.strategy.minimax;

import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAI;
import fr.univ.bordeaux.application.ai.heuristics.Heuristics;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;
import fr.univ.bordeaux.agonCore.agonElements.Move;

import java.util.List;

public class MinimaxStrategy extends AbstractAgonAI {

    private final int maxDepth;

    public MinimaxStrategy(Heuristics heuristic, int maxDepth) {
        super(heuristic);
        this.maxDepth = maxDepth;
    }

    @Override
    protected Move computeMove(AgonBoard board) {
        List<Move> legalMoves = board.generateLegalMoves(this.color);
        if (legalMoves.isEmpty()) return null;

        Move bestMove = legalMoves.getFirst();
        long maxScore = Long.MIN_VALUE;

        // Initialisation Alpha-Beta
        long alpha = Long.MIN_VALUE;
        long beta = Long.MAX_VALUE;

        // 2. Boucle Racine
        for (Move move : legalMoves) {

            // Joue un coup
            board.applyMove(move);
            this.nodeCount++;

            // Appelle minimax
            long score = minimax(board, this.maxDepth - 1, false, alpha, beta);

            // Backtrack
            board.undoMove();

            // Maximisation
            if (score > maxScore) {
                maxScore = score;
                bestMove = move;
                // System.out.println("Nouveau meilleur coup : " + move + " (Score: " + score + ")");
            }

            // Alpha-Beta Update
            alpha = Math.max(alpha, score);
        }

        System.out.println("Minimax fini. Nœuds visités: " + this.nodeCount);
        return bestMove;
    }

    private long minimax(AgonBoard board, int depth, boolean isMaximizingPlayer, long alpha, long beta) {

        // Condition d'arrêt
        if (depth == 0 || board.isGameWon(this.color)) {
            return heuristic.evaluate(board);
        }

        List<Move> moves = board.generateLegalMoves(this.color);

        if (isMaximizingPlayer) {
            // JOUEUR MAX
            long maxEval = Long.MIN_VALUE;

            for (Move move : moves) {
                board.applyMove(move);
                this.nodeCount++;

                long eval = minimax(board, depth - 1, false, alpha, beta);

                board.undoMove();

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);

                // Élagage
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;

        } else {
            // JOUEUR MIN
            long minEval = Long.MAX_VALUE;

            for (Move move : moves) {
                board.applyMove(move);
                this.nodeCount++;

                long eval = minimax(board, depth - 1, true, alpha, beta);

                board.undoMove();

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);

                // Élagage
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }
}