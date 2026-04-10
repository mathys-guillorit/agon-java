package fr.univ.bordeaux.ait;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.ai.heuristics.Heuristic;
import fr.univ.bordeaux.application.ai.heuristics.MixedHeuristic;
import fr.univ.bordeaux.application.ai.heuristics.MlHeuristic;
import fr.univ.bordeaux.application.ai.heuristics.UctHeuristic;
import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAi;
import fr.univ.bordeaux.application.ai.strategy.mcts.MctsStrategy;
import fr.univ.bordeaux.application.ai.strategy.minimax.MinimaxStrategy;

/**
 * Standalone testing script to simulate a match between two AI agents via the console.
 * Facilitates the evaluation of the TensorFlow Machine Learning model against a baseline UCT MCTS agent
 * without requiring the graphical user interface.
 */
public class AiDuel {

    /**
     * Entry point for the AI match simulation.
     * Initializes the game board, sets up the agents (White: ML, Black: UCT), and manages the game loop.
     *
     * @param args Command-line arguments (unused).
     */
    public static void main(String[] args) {
        System.out.println("=== AI Duel: ML vs Minimax ===");

        AgonBoardImpl board = new AgonBoardImpl();
        board.initBaseConfiguration();

        System.out.println("[Initialization] White Player: ML-powered MCTS");
        MlHeuristic mlHeuristic = new MlHeuristic(Math.sqrt(2));
        AbstractAgonAi whiteAi = new MctsStrategy(Color.WHITE, mlHeuristic, 3);

        System.out.println("[Initialization] Black Player: Mixed minimax");
        MixedHeuristic heuristic = new MixedHeuristic(10, 1);
        AbstractAgonAi blackAi = new MinimaxStrategy(heuristic, Color.BLACK, 1, false, 3);

        Color currentPlayer = Color.WHITE;
        int turnCount = 1;

        System.out.println("\n[Match Start]\n");

        while (!board.isGameWon(Color.WHITE) && !board.isGameWon(Color.BLACK)) {
            System.out.print("Turn " + turnCount + " - " + currentPlayer + " thinking... ");

            AbstractAgonAi currentAi = (currentPlayer == Color.WHITE) ? whiteAi : blackAi;

            long startTime = System.currentTimeMillis();
            Move bestMove = currentAi.getBestMove(board);
            long elapsed = System.currentTimeMillis() - startTime;

            if (bestMove == null) {
                System.out.println("\n[Error] Null move returned. " + currentPlayer + " has no legal moves.");
                break;
            }

            board.applyMove(bestMove);
            System.out.println("Move: " + bestMove.getFrom() + " -> " + bestMove.getDestination() +
                    " (" + elapsed + " ms, " + currentAi.getNodeCount() + " nodes visited)");

            for (String line : board.toTextList()) {
                System.out.println(line);
            }

            currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
            if (currentPlayer == Color.WHITE) {
                turnCount++;
            }

            if (turnCount > 150) {
                System.out.println("\n[Match Terminated] Turn limit (150) reached. Draw.");
                break;
            }
        }

        System.out.println("\n=== Match Ended ===");
        if (board.isGameWon(Color.WHITE)) {
            System.out.println("Result: WHITE (ML) Wins");
        } else if (board.isGameWon(Color.BLACK)) {
            System.out.println("Result: BLACK (UCT) Wins");
        }
    }
}