package fr.univ.bordeaux.agoncore.bitboard;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import java.util.ArrayList;
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
  @DisplayName("Test applyMove")
  void testApplyMoveBlackAndWhite() {
    BitBoard wQueen = new BitBoard();
    BitBoard bQueen = new BitBoard();
    BitBoard wPawns = new BitBoard();
    BitBoard bPawns = new BitBoard();
    bQueen.setBit(59, 1L);
    wPawns.setBit(63, 1L);
    board = new AgonBoardImpl(wQueen, bQueen, wPawns, bPawns);
    assertTrue(
        board.applyMove(new Move(59, 60, Color.BLACK)), "La reine noire devrait pouvoir bouger");
    assertEquals(PieceType.BLACK_QUEEN, board.getPieceAt(60));
    assertTrue(board.applyMove(new Move(63, 62, Color.WHITE)));
  }

  @Test
  @DisplayName("Geometry Test: Circle Centrality")
  void testCentrality() {
    assertEquals(0, board.getCentrality(THRONE), "Index 60 must be the Throne (Circle 0)");

    // Check a direct neighbor of the throne (Circle 1)
    assertTrue(board.getNeighbors(THRONE).isSet(72));
    assertEquals(1, board.getCentrality(72), "A direct neighbor of the throne must be in circle 1");

    // An index on the edges (Circle 5)
    assertEquals(5, board.getCentrality(1), "Index 0 must be on the outer edge (Circle 5)");
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

    List<Move> moves = new ArrayList<>();
    captureBoard.performCaptures(Color.WHITE, moves);

    assertNull(captureBoard.getPieceAt(71), "The black pawn at 71 should be captured");
    assertTrue(
        captureBoard.generateLegalMoves(Color.BLACK).stream().allMatch(m -> m.getFrom() == -1),
        "Black should have relocation moves (from = -1)");
    BitBoard blackQueen = new BitBoard(62);
    whitePawns.setBit(61, 1L);
    whitePawns.setBit(63, 1L);
    moves = new ArrayList<>();
    captureBoard.performCaptures(Color.WHITE, moves);
    assertNull(captureBoard.getPieceAt(62), "The black queen at 62 should be captured");
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

    boolean canGoToThrone = moves.stream().anyMatch(m -> m.getDestination() == THRONE);
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
      int targetCircle = board.getCentrality(m.getDestination());
      assertTrue(
          targetCircle <= 2, "A pawn cannot move to an outer circle (" + targetCircle + " > 2)");
    }
  }

  @Test
  @DisplayName("Test de relocation : Priorité absolue")
  void testRelocationPriority() {
    BitBoard wPawns = new BitBoard(59);
    BitBoard bQueen = new BitBoard(60);
    wPawns.setBit(61, 1L);

    AgonBoardImpl boardReloc = new AgonBoardImpl(new BitBoard(), bQueen, wPawns, new BitBoard());
    List<Move> history = new ArrayList<>();
    boardReloc.performCaptures(Color.WHITE, history);
    List<Move> moves = boardReloc.generateLegalMoves(Color.BLACK);
    assertFalse(moves.isEmpty());
    for (Move m : moves) {
      assertEquals(-1, m.getFrom(), "While queen is captured only relocation move could be able");
    }
  }

  @Test
  @DisplayName("Test de relocation : Priorité absolue")
  void testRelocation() {
    BitBoard wPawns = new BitBoard(61);
    wPawns.setBit(64, 1L);
    BitBoard bPawns = new BitBoard(62);
    AgonBoardImpl boardReloc = new AgonBoardImpl(new BitBoard(), new BitBoard(), wPawns, bPawns);
    boardReloc.applyMove(new Move(64, 63, Color.WHITE, PieceType.WHITE_PAWN));
    assertFalse(
        boardReloc.applyMove(
            new Move(-1, CoordinateMapper.toIndex('G', 1), Color.BLACK, PieceType.BLACK_PAWN)));
    assertFalse(
        boardReloc.applyMove(
            new Move(-1, CoordinateMapper.toIndex('A', 0), Color.BLACK, PieceType.BLACK_PAWN)));
    assertFalse(boardReloc.applyMove(new Move(-1, -1, Color.BLACK, PieceType.BLACK_PAWN)));
    assertFalse(boardReloc.applyMove(new Move(-1, 60, Color.BLACK, PieceType.BLACK_PAWN)));
    assertFalse(
        boardReloc.applyMove(
            new Move(-1, CoordinateMapper.toIndex('F', 3), Color.BLACK, PieceType.BLACK_PAWN)));
    assertFalse(
        boardReloc.applyMove(
            new Move(-1, CoordinateMapper.toIndex('A', 11), Color.BLACK, PieceType.BLACK_PAWN)));
    assertTrue(
        boardReloc.applyMove(
            new Move(-1, CoordinateMapper.toIndex('G', 2), Color.BLACK, PieceType.BLACK_PAWN)));
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

  @Test
  @DisplayName("Test undoMove : back to the last state")
  void testUndoMove() {
    BitBoard wPawns = new BitBoard(0);
    AgonBoardImpl undoBoard =
        new AgonBoardImpl(new BitBoard(), new BitBoard(), wPawns, new BitBoard());

    Move move = new Move(0, 1, Color.WHITE);
    undoBoard.applyMove(move);

    assertEquals(PieceType.WHITE_PAWN, undoBoard.getPieceAt(1));
    assertNull(undoBoard.getPieceAt(0));

    undoBoard.undoMove();

    assertEquals(PieceType.WHITE_PAWN, undoBoard.getPieceAt(0), "Pawn should be back to index 0");
    assertNull(undoBoard.getPieceAt(1), "index 1 should be empty after undo");
    assertFalse(undoBoard.undoMove());
  }

  @Test
  @DisplayName("Test Undo après capture de la Reine Noire")
  void testUndoCaptureQueen() {
    // 1. Setup avec captureBoard
    BitBoard whitePawns = new BitBoard();
    whitePawns.setBit(62, 1L);
    whitePawns.setBit(65, 1L);
    BitBoard blackQueen = new BitBoard(63);
    AgonBoardImpl captureBoard =
        new AgonBoardImpl(new BitBoard(), blackQueen, whitePawns, new BitBoard());
    captureBoard.applyMove(new Move(65, 64, Color.WHITE));
    assertNull(captureBoard.getPieceAt(63), "La reine devrait être capturée avant l'undo");
    assertTrue(captureBoard.undoMove(), "L'undo devrait réussir car un coup a été joué");
    assertEquals(PieceType.BLACK_QUEEN, captureBoard.getPieceAt(63), "La reine doit être revenue");
    assertEquals(
        PieceType.WHITE_PAWN, captureBoard.getPieceAt(65), "Le pion blanc doit être revenu à 65");
    assertNull(captureBoard.getPieceAt(64), "La case 64 doit être vide");
  }

  @Test
  @DisplayName("Test Undo Relocalisation Reine : déclenche setQueenRelocating(true)")
  void testUndoQueenRelocation() {
    BitBoard wQueen = new BitBoard(63);
    BitBoard bPawns = new BitBoard();
    bPawns.setBit(62, 1L);
    bPawns.setBit(65, 1L);
    AgonBoardImpl boardReloc2 = new AgonBoardImpl(wQueen, new BitBoard(), new BitBoard(), bPawns);
    boardReloc2.applyMove(new Move(65, 64, Color.BLACK));
    assertNull(boardReloc2.getPieceAt(63), "La reine blanche devrait être capturée");
    Move relocationMove2 = new Move(-1, 61, Color.WHITE);
    assertTrue(boardReloc2.applyMove(relocationMove2));
    assertEquals(PieceType.WHITE_QUEEN, boardReloc2.getPieceAt(61));
    boolean undoResult2 = boardReloc2.undoMove();
    assertTrue(undoResult2, "L'undo doit réussir");
    assertNull(boardReloc2.getPieceAt(20), "La reine ne doit plus être sur le plateau");
    List<Move> nextMoves2 = boardReloc2.generateLegalMoves(Color.WHITE);
    assertFalse(nextMoves2.isEmpty());
    boolean isRelocating2 = nextMoves2.stream().allMatch(m -> m.getFrom() == -1);
    assertTrue(isRelocating2, "La reine blanche doit à nouveau être en attente de relocalisation");
  }

  @Test
  @DisplayName("Test de la configuration initiale du plateau")
  void testInitBaseConfiguration() {

    AgonBoardImpl baseBoard = new AgonBoardImpl();
    baseBoard.initBaseConfiguration();

    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('K', 10)) == PieceType.WHITE_PAWN);
    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('J', 5)) == PieceType.WHITE_PAWN);
    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('H', 11)) == PieceType.WHITE_PAWN);
    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('D', 9)) == PieceType.WHITE_PAWN);
    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('A', 5)) == PieceType.WHITE_PAWN);
    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('B', 1)) == PieceType.WHITE_PAWN);
    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('F', 1)) == PieceType.WHITE_QUEEN);
    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('K', 7)) == PieceType.BLACK_PAWN);
    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('J', 11)) == PieceType.BLACK_PAWN);
    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('H', 3)) == PieceType.BLACK_PAWN);
    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('D', 1)) == PieceType.BLACK_PAWN);
    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('A', 2)) == PieceType.BLACK_PAWN);
    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('B', 7)) == PieceType.BLACK_PAWN);
    assertTrue(baseBoard.getPieceAt(CoordinateMapper.toIndex('F', 11)) == PieceType.BLACK_QUEEN);
  }

  @Test
  @DisplayName("Test generateLegalMovesBitboard : Cas de relocalisation")
  void testGenerateLegalMovesWithRelocation() {
    BitBoard wQueen = new BitBoard(63);
    BitBoard bPawns = new BitBoard();
    bPawns.setBit(62, 1L);
    bPawns.setBit(65, 1L);
    AgonBoardImpl boardReloc = new AgonBoardImpl(wQueen, new BitBoard(), new BitBoard(), bPawns);
    boardReloc.applyMove(new Move(65, 64, Color.BLACK));
    BitBoard legalDestinations = boardReloc.generateLegalMovesBitboard(Color.WHITE);
    assertNotNull(legalDestinations, "Le BitBoard de destinations ne doit pas être nul");
    assertFalse(
        legalDestinations.isEmpty(), "Il doit y avoir des cases de relocalisation possibles");
    assertFalse(legalDestinations.isSet(60), "Une reine ne peut pas être relocalisée sur le trône");
  }

  @Test
  @DisplayName("Test redoMove : back to the last state")
  void testRedoMove() {
    BitBoard wPawns = new BitBoard();
    BitBoard bQueen = new BitBoard();
    wPawns.setBit(62, 1L);
    bQueen.setBit(53, 1L);
    board = new AgonBoardImpl(wPawns, bQueen, new BitBoard(), new BitBoard());
    board.applyMove(new Move(62, 61, Color.WHITE));
    board.applyMove(new Move(53, 52, Color.BLACK));
    board.undoMove();
    board.undoMove();
    board.undoMove();
    board.redoMove();
    assertTrue(wPawns.isSet(61));
    board.applyMove(new Move(53, 52, Color.BLACK));
    assertFalse(board.redoMove());
  }

  @Test
  @DisplayName("Test getMobility")
  void testGetMobility() {
    BitBoard wPawns = new BitBoard();
    BitBoard bQueen = new BitBoard();
    BitBoard bPawns = new BitBoard();
    BitBoard wQueen = new BitBoard();
    bQueen.setBit(60, 1L);
    board = new AgonBoardImpl(wQueen, bQueen, wPawns, bPawns);
    assertEquals(board.getMobility(61), -1);
    assertEquals(board.getMobility(60), 0);
  }

  @Test
  @DisplayName("Test toTextList: ASCII conversion")
  void testToTextList() {
    BitBoard wQueen = new BitBoard(THRONE);
    BitBoard bPawns = new BitBoard(61);
    BitBoard wPawns = new BitBoard(59);
    AgonBoardImpl textBoard = new AgonBoardImpl(wQueen, new BitBoard(), wPawns, bPawns);

    List<String> lines = textBoard.toTextList();

    assertNotNull(lines, "The list of lines should not be null");
    assertFalse(lines.isEmpty(), "The list of lines should not be empty");

    String fullText = String.join("\n", lines);
    assertTrue(fullText.contains("Q"), "The text must contain the White Queen (Q)");
    assertTrue(fullText.contains("X"), "The text must contain the Black Pawn (X)");
    assertTrue(fullText.contains("O"), "The text must contain the White Pawn (o)");
    assertTrue(fullText.contains("."), "The text must contain empty tiles (.)");
  }

  @Test
  @DisplayName("Test Constructor from Text: Board restoration")
  void testConstructorFromTextList() {
    BitBoard wQueen = new BitBoard(THRONE);
    BitBoard bPawns = new BitBoard(72);
    BitBoard wPawns = new BitBoard(59);
    AgonBoardImpl originalBoard = new AgonBoardImpl(wQueen, new BitBoard(), wPawns, bPawns);

    List<String> textRepresentation = originalBoard.toTextList();

    AgonBoardImpl reconstructedBoard = new AgonBoardImpl(textRepresentation,null);

    assertEquals(
        PieceType.WHITE_QUEEN,
        reconstructedBoard.getPieceAt(60),
        "The White Queen should be at index 60");
    assertEquals(
        PieceType.BLACK_PAWN,
        reconstructedBoard.getPieceAt(72),
        "The Black Pawn should be at index 72");
    assertEquals(
        PieceType.WHITE_PAWN,
        reconstructedBoard.getPieceAt(59),
        "The White Pawn should be at index 59");
    assertNull(reconstructedBoard.getPieceAt(0), "An initially empty tile must remain empty");
  }
}
