package fr.univ.bordeaux.ui.cli;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agonCore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agonCore.bitboard.CoordinateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsoleRendererTest {

  private AgonBoardImpl realBoard;
  private ConsoleRenderer renderer;

  @BeforeEach
  void setUp() {
    realBoard = new AgonBoardImpl();
    renderer = new ConsoleRenderer(realBoard);
  }

  @Test
  void testEmptyBoardStructure() {
    String view = renderer.getBoardRepresentation();

    assertNotNull(view, "Render output must not be null");
    assertFalse(view.isEmpty(), "Render output must not be empty");
    assertTrue(view.contains("F |"), "Must contain central line F");
    assertTrue(view.contains("A \\"), "Must contain bottom line A");
    assertTrue(view.contains("K /"), "Must contain top line K");
    assertTrue(view.contains("1 2 3 4 5 6"), "Must contain column numbers");
    assertFalse(view.contains("Q"), "An empty board must not display a Queen (Q)");
    assertFalse(view.contains("X"), "An empty board must not display a Black Pawn (X)");
  }

  @Test
  void testRenderWithPieces() {
    int indexWhiteQueen = CoordinateMapper.toIndex('F', 6);
    realBoard.getWhiteQueen().setBit(indexWhiteQueen, 1L);

    int indexBlackPawn = CoordinateMapper.toIndex('C', 3);
    realBoard.getBlackPawns().setBit(indexBlackPawn, 1L);

    int indexBlackQueen = CoordinateMapper.toIndex('K', 6);
    realBoard.getBlackQueen().setBit(indexBlackQueen, 1L);

    String view = renderer.getBoardRepresentation();

    System.out.println("--- Visual Test Output ---");
    System.out.println(view);

    assertTrue(view.contains("Q"), "Render must contain White Queen (Q)");
    assertTrue(view.contains("X"), "Render must contain Black Pawn (X)");
    assertTrue(view.contains("q"), "Render must contain Black Queen (q)");

    String[] lines = view.split("\n");
    boolean foundQueenOnF = false;

    for (String line : lines) {
      if (line.contains("F |")) {
        if (line.contains("Q")) {
          foundQueenOnF = true;
        }
      }
    }
    assertTrue(foundQueenOnF, "White Queen (Q) should be visible on line F");
  }

  @Test
  void testCoordinatesValidity() {
    assertDoesNotThrow(() -> renderer.getBoardRepresentation());
  }
}
