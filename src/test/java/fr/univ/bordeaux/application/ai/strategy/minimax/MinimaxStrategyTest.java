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

    private int THRONE = 60;

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

        AgonBoardImpl board = createCustomBoard(THRONE, -1, List.of(CoordinateMapper.toIndex('G',6),CoordinateMapper.toIndex('F',5),CoordinateMapper.toIndex('E',5),CoordinateMapper.toIndex('E',6),CoordinateMapper.toIndex('F',7),CoordinateMapper.toIndex('H',8)), List.of());

        Heuristic heuristic = new MixedHeuristic(10, 1);
        MinimaxStrategy ai = new MinimaxStrategy(heuristic, Color.WHITE, 1, false, 5);

        Move bestMove = ai.getBestMove(board);

        assertNotNull(bestMove, "AI must find a move");

        board.applyMove(bestMove);

        assertTrue(board.isGameWon(Color.WHITE), "Game should be won");
    }

    @Test
    void testDefenseOpponentWinByCapturing() {

        List<Integer> bPawns = List.of(CoordinateMapper.toIndex('G',6),
                CoordinateMapper.toIndex('F',5),CoordinateMapper.toIndex('E',5),
                CoordinateMapper.toIndex('E',6),CoordinateMapper.toIndex('F',7),
                CoordinateMapper.toIndex('H',8));

        List<Integer> wPawns = List.of(CoordinateMapper.toIndex('H', 7),
                CoordinateMapper.toIndex('I', 9), CoordinateMapper.toIndex('C', 3),
                CoordinateMapper.toIndex('D', 5), CoordinateMapper.toIndex('D', 6),
                CoordinateMapper.toIndex('K', 8));

        AgonBoardImpl board = createCustomBoard(-1, THRONE, wPawns, bPawns);

        Heuristic heuristic = new MixedHeuristic(10, 1);

        MinimaxStrategy ai = new MinimaxStrategy(heuristic, Color.WHITE, 4, false, 10);

        Move bestMove = ai.getBestMove(board);

        board.applyMove(bestMove);

        assertNotNull(bestMove);

        assertEquals(CoordinateMapper.toIndex('H', 9), bestMove.getTo(), "AI should have blocked G7 in order to block the opponent");
    }


    @Test
    void testIterativeDeepeningFindsOptimalMove() {
        AgonBoardImpl board = createCustomBoard(THRONE, -1,
                List.of(CoordinateMapper.toIndex('G',6), CoordinateMapper.toIndex('F',5), CoordinateMapper.toIndex('E',5), CoordinateMapper.toIndex('E',6), CoordinateMapper.toIndex('F',7), CoordinateMapper.toIndex('H',8)),
                List.of());

        Heuristic heuristic = new MixedHeuristic(10, 1);

        MinimaxStrategy ai = new MinimaxStrategy(heuristic, Color.WHITE, 4, true, 100);

        Move bestMove = ai.getBestMove(board);

        assertNotNull(bestMove, "ID should return a valid move");

        board.applyMove(bestMove);
        assertTrue(board.isGameWon(Color.WHITE), "ID should have found the winning move just like standard Minimax");
    }

    @Test
    void testIterativeDeepeningRespectsTimeout() {

        int wQ = CoordinateMapper.toIndex('A', 1);
        int bQ = CoordinateMapper.toIndex('K', 11);
        List<Integer> wP = List.of(CoordinateMapper.toIndex('A', 2), CoordinateMapper.toIndex('A', 3), CoordinateMapper.toIndex('A', 4), CoordinateMapper.toIndex('A', 5),  CoordinateMapper.toIndex('A', 6), CoordinateMapper.toIndex('B', 7));
        List<Integer> bP = List.of(CoordinateMapper.toIndex('K', 10), CoordinateMapper.toIndex('K', 9), CoordinateMapper.toIndex('K', 8), CoordinateMapper.toIndex('K', 7), CoordinateMapper.toIndex('K', 6), CoordinateMapper.toIndex('J', 5));

        AgonBoardImpl board = createCustomBoard(wQ, bQ, wP, bP);

        Heuristic heuristic = new CentralityHeuristic();

        MinimaxStrategy ai = new MinimaxStrategy(heuristic, Color.WHITE, 10, true, 1);

        long startTime = System.currentTimeMillis();
        Move bestMove = ai.getBestMove(board);
        long elapsed = System.currentTimeMillis() - startTime;

        assertNotNull(bestMove, "Even when interrupted, ID must return the best move found so far");

        assertTrue(elapsed >= 945, "The AI should have used its entire 1 second limit");
        assertTrue(elapsed < 1500, "The AI should have stopped cleanly and not exceeded the timeout drastically");
    }
}