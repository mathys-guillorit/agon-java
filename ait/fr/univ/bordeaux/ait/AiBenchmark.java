package fr.univ.bordeaux.ait;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAi;
import fr.univ.bordeaux.application.ai.strategy.AiFactory;
import fr.univ.bordeaux.technical.io.config.GameConfig;

public class AiBenchmark {

    // --- GLOBAL SETTINGS ---
    private static final int NUM_MATCHES_PER_SCENARIO = 10;
    private static final int MAX_TURNS = 100;
    private static final int TIME_LIMIT_SECONDS = 1;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("    AGON AI BENCHMARK SUITE - AUTOMATED TESTS    ");
        System.out.println("=================================================\n");

        // --- CONFIGURATIONS PREPARATION ---

        GameConfig minimaxMixed = new GameConfig();
        minimaxMixed.setAiMode("minimax");
        minimaxMixed.setAiHeuristic("mixed");
        minimaxMixed.setAiDepth(3);
        minimaxMixed.setAiTimeLimit(TIME_LIMIT_SECONDS);

        GameConfig minimaxCentrality = new GameConfig();
        minimaxCentrality.setAiMode("minimax");
        minimaxCentrality.setAiHeuristic("centrality");
        minimaxCentrality.setAiDepth(3);
        minimaxCentrality.setAiTimeLimit(TIME_LIMIT_SECONDS);

        GameConfig minimaxMobility = new GameConfig();
        minimaxMobility.setAiMode("minimax");
        minimaxMobility.setAiHeuristic("mobility");
        minimaxMobility.setAiDepth(3);
        minimaxMobility.setAiTimeLimit(TIME_LIMIT_SECONDS);

        GameConfig mctsUct = new GameConfig();
        mctsUct.setAiMode("mcts");
        mctsUct.setAiHeuristic("uct");
        mctsUct.setAiTimeLimit(TIME_LIMIT_SECONDS);

        GameConfig mctsMl = new GameConfig();
        mctsMl.setAiMode("mcts");
        mctsMl.setAiHeuristic("ml");
        mctsMl.setAiTimeLimit(TIME_LIMIT_SECONDS);

        // --- RUNNING SCENARIOS ---

        runScenario("Scenario 1: The Baseline",
                minimaxMixed, "Minimax (Mixed)",
                mctsUct, "MCTS (UCT)");

        runScenario("Scenario 2: Probabilistic Clash",
                mctsMl, "MCTS (Machine Learning)",
                mctsUct, "MCTS (UCT)");

        runScenario("Scenario 3: The Ultimate Test",
                mctsMl, "MCTS (Machine Learning)",
                minimaxMixed, "Minimax (Mixed)");

        runScenario("Scenario 4: Heuristics Duel",
                minimaxCentrality, "Minimax (Centrality)",
                minimaxMobility, "Minimax (Mobility)");

        System.out.println(">>> ALL SCENARIOS COMPLETED <<<");
    }

    /**
     * Executes a full scenario between two AI configurations, automatically alternating
     * starting colors to ensure fairness, and prints a detailed performance summary.
     */
    private static void runScenario(String scenarioName,
                                    GameConfig configAi1, String nameAi1,
                                    GameConfig configAi2, String nameAi2) {

        System.out.println(">>> " + scenarioName + ": " + nameAi1 + " vs " + nameAi2 + " <<<");

        int ai1Wins = 0;
        int ai2Wins = 0;
        int draws = 0;

        long ai1TotalNodes = 0;
        long ai2TotalNodes = 0;
        long ai1TotalMoves = 0;
        long ai2TotalMoves = 0;

        for (int i = 0; i < NUM_MATCHES_PER_SCENARIO; i++) {
            System.out.print("  Match " + (i + 1) + "/" + NUM_MATCHES_PER_SCENARIO);

            // Alternate colors: Even matches AI1 is White, Odd matches AI1 is Black
            boolean ai1IsWhite = (i % 2 == 0);

            AbstractAgonAi whiteAi = AiFactory.createAi(ai1IsWhite ? configAi1 : configAi2, Color.WHITE);
            AbstractAgonAi blackAi = AiFactory.createAi(ai1IsWhite ? configAi2 : configAi1, Color.BLACK);

            AgonBoardImpl board = new AgonBoardImpl();
            board.initBaseConfiguration();

            Color currentPlayer = Color.WHITE;
            int turns = 0;

            while (!board.isGameWon(Color.WHITE) && !board.isGameWon(Color.BLACK) && turns < MAX_TURNS) {
                AbstractAgonAi currentAi = (currentPlayer == Color.WHITE) ? whiteAi : blackAi;

                Move move = currentAi.getBestMove(board);
                if (move == null) break;

                // Track nodes and moves
                if ((ai1IsWhite && currentPlayer == Color.WHITE) || (!ai1IsWhite && currentPlayer == Color.BLACK)) {
                    ai1TotalNodes += currentAi.getNodeCount();
                    ai1TotalMoves++;
                } else {
                    ai2TotalNodes += currentAi.getNodeCount();
                    ai2TotalMoves++;
                }

                board.applyMove(move);
                currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
                turns++;
            }

            // Determine winner
            if (board.isGameWon(Color.WHITE)) {
                if (ai1IsWhite) ai1Wins++; else ai2Wins++;
                System.out.println(" -> White win (" + (ai1IsWhite ? "AI1" : "AI2") + ") in " + turns + " turns.");
            } else if (board.isGameWon(Color.BLACK)) {
                if (!ai1IsWhite) ai1Wins++; else ai2Wins++;
                System.out.println(" -> Black win (" + (!ai1IsWhite ? "AI1" : "AI2") + ") in " + turns + " turns.");
            } else {
                draws++;
                System.out.println(" -> Draw after " + turns + " turns.");
            }
        }

        // Print Scenario Summary
        System.out.println("-------------------------------------------------");
        System.out.println("SCENARIO SUMMARY: " + nameAi1 + " vs " + nameAi2);
        System.out.println("AI 1 (" + nameAi1 + ") Wins : " + ai1Wins);
        System.out.println("AI 2 (" + nameAi2 + ") Wins : " + ai2Wins);
        System.out.println("Draws/Deadlocks        : " + draws);
        System.out.println("Avg visited nodes/move (AI 1) : " + (ai1TotalMoves > 0 ? ai1TotalNodes / ai1TotalMoves : 0));
        System.out.println("Avg visited nodes/move (AI 2) : " + (ai2TotalMoves > 0 ? ai2TotalNodes / ai2TotalMoves : 0));
        System.out.println("=================================================\n");
    }
}