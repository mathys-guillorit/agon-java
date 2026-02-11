package fr.univ.bordeaux.agonCore.bitboard;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgonBoardImplTest {

  private AgonBoardImpl board;
  private final int THRONE = 60;

  @BeforeEach
  void setUp() {
    // Initialize an empty board
    board = new AgonBoardImpl();
  }

  @Test
  @DisplayName("Geometry Test: Circle Centrality")
  void testCentrality() {
    assertEquals(0, board.getCentrality(THRONE), "Index 60 must be the Throne (Circle 0)");

    // Check a direct neighbor of the throne (Circle 1)
    assertTrue(board.getNeighbors(THRONE).isSet(72));
    assertEquals(1, board.getCentrality(72), "A direct neighbor of the throne must be in circle 1");

    // An index on the edges (Circle 5)
    assertEquals(5, board.getCentrality(0), "Index 0 must be on the outer edge (Circle 5)");
  }

  @Test
  @DisplayName("Capture Test: Sandwich Mechanism")
  void testPerformCaptures() {
    BitBoard whitePawns = new BitBoard();
    whitePawns.setBit(THRONE, 1L);
    whitePawns.setBit(82, 1L);

    BitBoard blackPawns = new BitBoard(71);

    AgonBoardImpl captureBoard =
        new AgonBoardImpl(new BitBoard(), new BitBoard(), whitePawns, blackPawns);

    captureBoard.performCaptures(Color.WHITE);

    assertNull(captureBoard.getPieceAt(71), "The black pawn at 71 should be captured");
    assertTrue(
        captureBoard.generateLegalMoves(Color.BLACK).stream().allMatch(m -> m.getFrom() == -1),
        "Black should have relocation moves (from = -1)");
  }

  @Test
  @DisplayName("Rule Test: Pawns cannot enter the throne")
  void testPawnCannotEnterThrone() {
    BitBoard whitePawns = new BitBoard(72);
    AgonBoardImpl boardWithPawn =
        new AgonBoardImpl(new BitBoard(), new BitBoard(), whitePawns, new BitBoard());

    List<Move> moves = boardWithPawn.generateLegalMoves(Color.WHITE);

    boolean canGoToThrone = moves.stream().anyMatch(m -> m.getTo() == THRONE);
    assertFalse(canGoToThrone, "A pawn should never be able to move to the throne (index 60)");
  }

  @Test
  @DisplayName("Rule Test: No retreating allowed")
  void testNoRetreating() {
    int startIdx = 36;
    assertEquals(2, board.getCentrality(startIdx));

    BitBoard whitePawns = new BitBoard(startIdx);
    AgonBoardImpl moveBoard =
        new AgonBoardImpl(new BitBoard(), new BitBoard(), whitePawns, new BitBoard());

    List<Move> moves = moveBoard.generateLegalMoves(Color.WHITE);
    for (Move m : moves) {
      int targetCircle = board.getCentrality(m.getTo());
      assertTrue(
          targetCircle <= 2, "A pawn cannot move to an outer circle (" + targetCircle + " > 2)");
    }
  }

  @Test
  @DisplayName("Victory Test: Queen + 6 pawns")
  void testVictoryCondition() {
    BitBoard wQueen = new BitBoard(THRONE);
    BitBoard wPawns = new BitBoard();
    int[] neighbors = {48, 49, 59, 61, 71, 72};
    for (int n : neighbors) {
      wPawns.setBit(n, 1L);
    }

    AgonBoardImpl winBoard = new AgonBoardImpl(wQueen, new BitBoard(), wPawns, new BitBoard());

    assertTrue(
        winBoard.isGameWon(Color.WHITE), "White should win: Queen in the center and surrounded");
  }

  @Test
  @DisplayName("Non-Victory Test: 6 pawns without Queen")
  void testNotVictoryCondition() {
    BitBoard wQueen = new BitBoard();
    BitBoard wPawns = new BitBoard();

    int[] neighbors = {48, 49, 59, 61, 71, 72};
    for (int n : neighbors) {
      wPawns.setBit(n, 1L);
    }

    AgonBoardImpl winBoard = new AgonBoardImpl(wQueen, new BitBoard(), wPawns, new BitBoard());

    assertFalse(winBoard.isGameWon(Color.WHITE));
  }
}
