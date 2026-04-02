package fr.univ.bordeaux.ui.gui.components.states;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for {@link IdleCanvasState}.
 *
 * <p>Verifies the behavior of the canvas when no piece is currently selected. It ensures that
 * clicks on empty hexes are ignored and clicks on pieces transition the canvas into the appropriate
 * selected state.
 */
public class IdleCanvasStateTest {

  private IdleCanvasState state;
  private FakeCanvas dummyCanvas;

  /**
   * A lightweight mock of the CanvasInterface to track method calls and state changes without
   * initializing the full JavaFX environment.
   */
  class FakeCanvas implements CanvasInterface {
    boolean simulatePieceExists = false;
    boolean drawCalled = false;
    CanvasInteractionState newState = null;

    @Override
    public boolean hasPieceAt(String hex) {
      return simulatePieceExists;
    }

    @Override
    public void setSelectedHex(String hex) {}

    @Override
    public void setDraggedPiece(PieceType piece) {}

    @Override
    public PieceType getPieceAt(String hex) {
      return PieceType.WHITE_PAWN;
    }

    @Override
    public void setMousePosition(double x, double y) {}

    @Override
    public void setState(CanvasInteractionState state) {
      this.newState = state;
    }

    @Override
    public void draw() {
      drawCalled = true;
    }

    @Override
    public void requestMove(String moveCommand) {}
  }

  @BeforeEach
  void setUp() {
    state = new IdleCanvasState();
    dummyCanvas = new FakeCanvas();
  }

  /** Ensures that clicking outside the board boundaries does not trigger any state change. */
  @Test
  void testHandleMousePressed_NullCoordinate() {
    state.handleMousePressed(dummyCanvas, null, 0, 0);
    assertNull(dummyCanvas.newState, "The state should not change");
    assertFalse(dummyCanvas.drawCalled, "The board should not be redrawn");
  }

  /** Ensures that clicking on an empty hex while idle does nothing. */
  @Test
  void testHandleMousePressed_EmptyHex() {
    dummyCanvas.simulatePieceExists = false;
    state.handleMousePressed(dummyCanvas, "G7", 100, 100);
    assertNull(dummyCanvas.newState, "The state should not change on an empty hex");
    assertFalse(dummyCanvas.drawCalled, "The board should not be redrawn");
  }

  /**
   * Ensures that clicking on a valid piece correctly grabs it and transitions the canvas to the
   * PieceSelectedState.
   */
  @Test
  void testHandleMousePressed_WithPiece() {
    dummyCanvas.simulatePieceExists = true;
    state.handleMousePressed(dummyCanvas, "F6", 100, 100);
    assertTrue(
        dummyCanvas.newState instanceof PieceSelectedState,
        "The state should transition to PieceSelectedState");
    assertTrue(dummyCanvas.drawCalled, "The board should be redrawn");
  }

  /** Verifies that dragging or releasing the mouse while idle throws no exceptions. */
  @Test
  void testEmptyMethods() {
    assertDoesNotThrow(
        () -> {
          state.handleMouseDragged(dummyCanvas, 50, 50);
          state.handleMouseReleased(dummyCanvas, "F6", 50, 50);
        });
  }
}
