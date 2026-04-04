package fr.univ.bordeaux.ui.cli;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsoleRendererTest {

  private AgonBoardImpl realBoard;

  @BeforeEach
  void setUp() {
    realBoard = new AgonBoardImpl();
  }

  @Test
  void testEmptyBoardStructure() {
    String view = ConsoleRenderer.getBoardRepresentation(realBoard);

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

    int indexWhitePawn = CoordinateMapper.toIndex('H', 4);
    realBoard.getWhitePawns().setBit(indexWhitePawn, 1L);

    String view = ConsoleRenderer.getBoardRepresentation(realBoard);

    System.out.println("--- Visual Test Output ---");
    System.out.println(view);

    assertTrue(view.contains("Q"), "Render must contain White Queen (Q)");
    assertTrue(view.contains("X"), "Render must contain Black Pawn (X)");
    assertTrue(view.contains("q"), "Render must contain Black Queen (q)");
    assertTrue(view.contains("O"), "Render must contain White Pawn (O)");

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
  void testThroneSymbol() {
    String emptyView = ConsoleRenderer.getBoardRepresentation(realBoard);
    assertTrue(emptyView.contains("+"), "An empty board must display the throne (+)");
    int indexThrone = CoordinateMapper.toIndex('F', 6);
    realBoard.getWhiteQueen().setBit(indexThrone, 1L);
    String filledView = ConsoleRenderer.getBoardRepresentation(realBoard);
    assertFalse(filledView.contains("+"), "The throne (+) must disappear when a piece is on it");
    assertTrue(filledView.contains("Q"), "The White Queen (Q) must replace the throne");
  }

  @Test
  void testCoordinatesValidity() {
    assertDoesNotThrow(() -> ConsoleRenderer.getBoardRepresentation(realBoard));
  }

  @Test
  void testNullBoardHandling() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> ConsoleRenderer.getBoardRepresentation(null),
            "Renderer should explicitly fail when given a null board");

    assertEquals("The game board cannot be null.", exception.getMessage());
  }
}
