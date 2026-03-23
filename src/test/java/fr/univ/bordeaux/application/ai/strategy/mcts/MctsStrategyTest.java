package fr.univ.bordeaux.application.ai.strategy.mcts;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agoncore.bitboard.BitBoard;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MctsStrategyTest {

  private final int THRONE = 60;

  private AgonBoardImpl createCustomBoard(
      int wQueenIdx, int bQueenIdx, List<Integer> wPawnsIdx, List<Integer> bPawnsIdx) {
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
  @DisplayName("MCTS should find the winning move in a Mate-in-1 scenario")
  void testMctsFindsWinningMove() {
    AgonBoardImpl board =
        createCustomBoard(
            THRONE,
            -1,
            List.of(
                CoordinateMapper.toIndex('G', 6),
                CoordinateMapper.toIndex('F', 5),
                CoordinateMapper.toIndex('E', 5),
                CoordinateMapper.toIndex('E', 6),
                CoordinateMapper.toIndex('F', 7),
                CoordinateMapper.toIndex('H', 8)),
            List.of());
    MctsStrategy mcts = new MctsStrategy(null, Color.WHITE);

    mcts.setTimeLimit(500);

    Move bestMove = mcts.getBestMove(board);

    assertNotNull(bestMove, "MCTS must find a move");

    board.applyMove(bestMove);

    assertTrue(board.isGameWon(Color.WHITE), "Game should be won : MCTS must play H8 to G7");
    assertTrue(mcts.getNodeCount() > 0, "MCTS should have populated its tree");
  }

  @Test
  @DisplayName("MCTS respects the time limit gracefully")
  void testMctsRespectsTimeout() {
    int wQ = CoordinateMapper.toIndex('A', 1);
    int bQ = CoordinateMapper.toIndex('K', 11);
    List<Integer> wP =
        List.of(
            CoordinateMapper.toIndex('A', 2),
            CoordinateMapper.toIndex('A', 3),
            CoordinateMapper.toIndex('A', 4),
            CoordinateMapper.toIndex('A', 5),
            CoordinateMapper.toIndex('A', 6),
            CoordinateMapper.toIndex('B', 7));
    List<Integer> bP =
        List.of(
            CoordinateMapper.toIndex('K', 10),
            CoordinateMapper.toIndex('K', 9),
            CoordinateMapper.toIndex('K', 8),
            CoordinateMapper.toIndex('K', 7),
            CoordinateMapper.toIndex('K', 6),
            CoordinateMapper.toIndex('J', 5));

    AgonBoardImpl board = createCustomBoard(wQ, bQ, wP, bP);

    MctsStrategy mcts = new MctsStrategy(null, Color.WHITE);

    long timeLimit = 1000;
    mcts.setTimeLimit(timeLimit);

    long startTime = System.currentTimeMillis();
    Move bestMove = mcts.getBestMove(board);
    long elapsed = System.currentTimeMillis() - startTime;

    assertNotNull(bestMove, "Even when interrupted, MCTS must return the best move found so far");

    assertTrue(
        elapsed >= timeLimit - 100,
        "The AI should have used its allocated time (" + elapsed + "ms)");
    assertTrue(
        elapsed < timeLimit + 100,
        "The AI should have stopped cleanly without drastically exceeding the timeout ("
            + elapsed
            + "ms)");

    System.out.println("MCTS visited " + mcts.getNodeCount() + " nodes in " + elapsed + "ms.");
  }

  @Test
  @DisplayName("MCTS should return null immediately if no legal moves exist")
  void testMctsNoLegalMoves() {
    AgonBoardImpl emptyBoard = createCustomBoard(-1, -1, List.of(), List.of());
    MctsStrategy mcts = new MctsStrategy(null, Color.WHITE);

    Move bestMove = mcts.getBestMove(emptyBoard);

    assertNull(bestMove, "Should return null when no moves are possible");
    assertEquals(
        0, mcts.getNodeCount(), "Should not build any tree because it returns immediately");
  }
}
