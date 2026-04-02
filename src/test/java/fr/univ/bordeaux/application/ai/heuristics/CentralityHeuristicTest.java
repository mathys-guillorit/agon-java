package fr.univ.bordeaux.application.ai.heuristics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agoncore.bitboard.BitBoard;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import org.junit.jupiter.api.Test;

// On suppose que tu as JUnit 5
class CentralityHeuristicTest {

  @Test
  void testQueenOnThroneShouldReturnMaxScore() {
    BitBoard whiteQueen = new BitBoard();
    BitBoard blackQueen = new BitBoard();
    BitBoard whitePawns = new BitBoard();
    BitBoard blackPawns = new BitBoard();

    whiteQueen.setBit(60, 1L);

    AgonBoardImpl board = new AgonBoardImpl(whiteQueen, blackQueen, whitePawns, blackPawns);

    CentralityHeuristic heuristic = new CentralityHeuristic();
    long score = heuristic.evaluate(board, Color.WHITE);

    assertEquals(7200, score, "A queen on the throne should be 7200 points");
  }

  @Test
  void testPawnAtEdgeShouldReturnMinScore() {
    BitBoard whiteQueen = new BitBoard();
    BitBoard blackQueen = new BitBoard();
    BitBoard whitePawns = new BitBoard();
    BitBoard blackPawns = new BitBoard();

    whitePawns.setBit(CoordinateMapper.toIndex('K', 11), 1L);

    AgonBoardImpl board = new AgonBoardImpl(whiteQueen, blackQueen, whitePawns, blackPawns);

    CentralityHeuristic heuristic = new CentralityHeuristic();
    long score = heuristic.evaluate(board, Color.WHITE);

    assertEquals(5, score, "A pawn on an edge should be 5 points");
  }

  @Test
  void enemyColorShouldReturnNegativeScore() {
    BitBoard whiteQueen = new BitBoard();
    BitBoard blackQueen = new BitBoard();
    BitBoard whitePawns = new BitBoard();
    BitBoard blackPawns = new BitBoard();

    blackQueen.setBit(60, 1L);

    AgonBoardImpl board = new AgonBoardImpl(whiteQueen, blackQueen, whitePawns, blackPawns);

    CentralityHeuristic heuristic = new CentralityHeuristic();
    long score = heuristic.evaluate(board, Color.WHITE);

    assertEquals(-7200, score, "An enemy queen on the throne should be -7200 points");
  }
}
