package fr.univ.bordeaux.application.ai.heuristics;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agoncore.bitboard.BitBoard;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import org.junit.jupiter.api.Test;

class MobilityHeuristicTest {

  @Test
  void testFreeQueenShouldReturnPositiveScore() {
    BitBoard whiteQueen = new BitBoard();
    BitBoard blackQueen = new BitBoard();
    BitBoard whitePawns = new BitBoard();
    BitBoard blackPawns = new BitBoard();

    whiteQueen.setBit(CoordinateMapper.toIndex('D', 4), 1L);

    AgonBoardImpl board = new AgonBoardImpl(whiteQueen, blackQueen, whitePawns, blackPawns);

    MobilityHeuristic heuristic = new MobilityHeuristic();
    long score = heuristic.evaluate(board, Color.WHITE);

    assertTrue(score > 0, "A free queen should have a positive mobility score");

    assertEquals(0, score % 20, "Score should be a multiple of Queen's weight (20)");
  }

  @Test
  void testFreePawnShouldReturnLowerScoreThanQueen() {
    BitBoard whiteQueen = new BitBoard();
    BitBoard blackQueen = new BitBoard();
    BitBoard whitePawns = new BitBoard();
    BitBoard blackPawns = new BitBoard();

    whitePawns.setBit(CoordinateMapper.toIndex('D', 4), 1L);

    AgonBoardImpl board = new AgonBoardImpl(whiteQueen, blackQueen, whitePawns, blackPawns);

    MobilityHeuristic heuristic = new MobilityHeuristic();
    long score = heuristic.evaluate(board, Color.WHITE);

    assertTrue(score > 0, "A free pawn should have positive score");

    assertEquals(0, score % 5, "Score should be a multiple of Pawn's weight (5)");
  }

  @Test
  void testEnemyMobilityShouldReturnNegativeScore() {
    BitBoard whiteQueen = new BitBoard();
    BitBoard blackQueen = new BitBoard();
    BitBoard whitePawns = new BitBoard();
    BitBoard blackPawns = new BitBoard();

    blackQueen.setBit(CoordinateMapper.toIndex('G', 7), 1L);

    AgonBoardImpl board = new AgonBoardImpl(whiteQueen, blackQueen, whitePawns, blackPawns);

    MobilityHeuristic heuristic = new MobilityHeuristic();

    long score = heuristic.evaluate(board, Color.WHITE);

    assertTrue(score < 0, "Enemy mobility should result in a negative score for the AI");
    assertEquals(0, score % 20, "Should still be a multiple of 20");
  }

  @Test
  void testBlockedPieceShouldReturnZero() {

    BitBoard whiteQueen = new BitBoard();
    BitBoard blackQueen = new BitBoard();
    BitBoard whitePawns = new BitBoard();
    BitBoard blackPawns = new BitBoard();

    whitePawns.setBit(CoordinateMapper.toIndex('K', 10), 1L);
    whitePawns.setBit(CoordinateMapper.toIndex('J', 11), 1L);
    whitePawns.setBit(CoordinateMapper.toIndex('J', 10), 1L);

    AgonBoardImpl board = new AgonBoardImpl(whiteQueen, blackQueen, whitePawns, blackPawns);

    MobilityHeuristic heuristic = new MobilityHeuristic();

    long score = heuristic.evaluate(board, Color.WHITE);

    whitePawns.setBit(CoordinateMapper.toIndex('K', 11), 1L);

    AgonBoardImpl board1 = new AgonBoardImpl(whiteQueen, blackQueen, whitePawns, blackPawns);

    long score1 = heuristic.evaluate(board1, Color.WHITE);

    assertEquals(
        0,
        (score - 2 * heuristic.getPawnWeight()) - score1,
        "A stuck pawn should have 0 mobility score");
  }
}
