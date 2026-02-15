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
}