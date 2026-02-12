package fr.univ.bordeaux.application.ai.strategy.minimax;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agonCore.bitboard.BitBoard;
import fr.univ.bordeaux.agonCore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.ai.heuristics.CentralityHeuristic;
import fr.univ.bordeaux.application.ai.heuristics.Heuristic;
import fr.univ.bordeaux.application.ai.heuristics.MixedHeuristic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MinimaxStrategyTest {

    private AgonBoardImpl createCustomBoard(int wQueenIdx, int bQueenIdx, List<Integer> wPawnsIdx, List<Integer> bPawnsIdx) {
        BitBoard wQ = new BitBoard();
        BitBoard bQ = new BitBoard();
        BitBoard wP = new BitBoard();
        BitBoard bP = new BitBoard();

        if (wQueenIdx != -1) wQ.setBit(wQueenIdx, 1L);
        if (bQueenIdx != -1) bQ.setBit(bQueenIdx, 1L);

        for (Integer idx : wPawnsIdx) wP.setBit(idx, 1L);
        for (Integer idx : bPawnsIdx) bP.setBit(idx, 1L);

        return new AgonBoardImpl(wQ, bQ, wP, bP);
    }

    @Test
    void testPuzzleWinInOneMove() {
        System.out.println("=== PUZZLE TEST : WIN IN ONE MOVE ===");

        AgonBoardImpl board = createCustomBoard(60, -1, List.of(CoordinateMapper.toIndex('G',6),CoordinateMapper.toIndex('F',5),CoordinateMapper.toIndex('E',5),CoordinateMapper.toIndex('E',6),CoordinateMapper.toIndex('F',7),CoordinateMapper.toIndex('H',8)), List.of());

        Heuristic heuristic = new CentralityHeuristic();
        MinimaxStrategy ai = new MinimaxStrategy(heuristic, Color.WHITE, 1);

        System.out.println("Initial state :");
        board.printBoard();

        Move bestMove = ai.getBestMove(board);

        assertNotNull(bestMove, "AI must find a move");

        board.applyMove(bestMove);
        System.out.println("Final state :");
        board.printBoard();

        assertTrue(board.isGameWon(Color.WHITE), "Game should be won");
    }

    @Test
    void simulateGameAiVsRandomBot() {
        System.out.println("\n=== GAME SIMULATION : AI (WHITE) vs RANDOM (BLACK) ===");

        int wQ = CoordinateMapper.toIndex('A', 1);
        int bQ = CoordinateMapper.toIndex('K', 11);
        List<Integer> wP = List.of(CoordinateMapper.toIndex('A', 2), CoordinateMapper.toIndex('A', 3), CoordinateMapper.toIndex('A', 4), CoordinateMapper.toIndex('A', 5),  CoordinateMapper.toIndex('A', 6), CoordinateMapper.toIndex('B', 7));
        List<Integer> bP = List.of(CoordinateMapper.toIndex('K', 10), CoordinateMapper.toIndex('K', 9), CoordinateMapper.toIndex('K', 8), CoordinateMapper.toIndex('K', 7), CoordinateMapper.toIndex('K', 6), CoordinateMapper.toIndex('J', 5));

        AgonBoardImpl board = createCustomBoard(wQ, bQ, wP, bP);

        Heuristic heuristic = new CentralityHeuristic();
        MinimaxStrategy ai = new MinimaxStrategy(heuristic, Color.WHITE, 5);

        int maxTurns = 40;
        for (int i = 1; i <= maxTurns; i++) {
            System.out.println("\n---------------- TURN " + i + " ----------------");

            System.out.println("AI playing :");
            Move aiMove = ai.getBestMove(board);

            if (aiMove == null) {
                System.out.println("AI cannot find a move");
                break;
            }

            board.applyMove(aiMove);
            board.printBoard();

            if (board.isGameWon(Color.WHITE)) {
                System.out.println("AI won");
                break;
            }

            System.out.println("Random playing :");
            List<Move> blackMoves = board.generateLegalMoves(Color.BLACK);

            if (blackMoves.isEmpty()) {
                System.out.println("Random cannot find a move");
                break;
            }

            Move randomMove = blackMoves.get((int)(Math.random() * blackMoves.size()));

            board.applyMove(randomMove);
            board.printBoard();

            if (board.isGameWon(Color.BLACK)) {
                System.out.println("Random won");
                break;
            }
        }
        System.out.println("\n=== END ===");
    }

    @Test
    void simulateGameAiVsAi() {
        System.out.println("\n=== GAME SIMULATION : AI (WHITE) vs AI (BLACK) ===");

        int wQ = CoordinateMapper.toIndex('A', 1);
        int bQ = CoordinateMapper.toIndex('K', 11);
        List<Integer> wP = List.of(CoordinateMapper.toIndex('A', 2), CoordinateMapper.toIndex('A', 3), CoordinateMapper.toIndex('A', 4), CoordinateMapper.toIndex('A', 5),  CoordinateMapper.toIndex('A', 6), CoordinateMapper.toIndex('B', 7));
        List<Integer> bP = List.of(CoordinateMapper.toIndex('K', 10), CoordinateMapper.toIndex('K', 9), CoordinateMapper.toIndex('K', 8), CoordinateMapper.toIndex('K', 7), CoordinateMapper.toIndex('K', 6), CoordinateMapper.toIndex('J', 5));

        AgonBoardImpl board = createCustomBoard(wQ, bQ, wP, bP);

        Heuristic heuristic = new MixedHeuristic(10, 1);
        MinimaxStrategy wAi = new MinimaxStrategy(heuristic, Color.WHITE, 5);
        MinimaxStrategy bAi = new MinimaxStrategy(heuristic, Color.BLACK, 5);

        int maxTurns = 40;
        for (int i = 1; i <= maxTurns; i++) {
            System.out.println("\n---------------- TURN " + i + " ----------------");

            System.out.println("WHITE AI playing :");
            Move wAiMove = wAi.getBestMove(new AgonBoardImpl(
                    board.getWhiteQueen().copy2(),
                    board.getBlackQueen().copy2(),
                    board.getWhitePawns().copy2(),
                    board.getBlackPawns().copy2()
            ));

            if (wAiMove == null) {
                System.out.println("WHITE AI cannot find a move");
                break;
            }

            board.applyMove(wAiMove);
            board.printBoard();

            if (board.isGameWon(Color.WHITE)) {
                System.out.println("WHITE AI won");
                break;
            }

            System.out.println("BLACK AI playing :");
            Move bAiMove = bAi.getBestMove(new AgonBoardImpl(
                    board.getWhiteQueen().copy2(),
                    board.getBlackQueen().copy2(),
                    board.getWhitePawns().copy2(),
                    board.getBlackPawns().copy2()
            ));


            if (bAiMove == null) {
                System.out.println("BLACK AI cannot find a move");
                break;
            }

            board.applyMove(bAiMove);
            board.printBoard();

            if (board.isGameWon(Color.BLACK)) {
                System.out.println("BLACK AI won");
                break;
            }
        }
        System.out.println("\n=== END ===");
    }
}