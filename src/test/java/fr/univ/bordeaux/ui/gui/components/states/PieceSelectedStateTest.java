package fr.univ.bordeaux.ui.gui.components.states;

import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link PieceSelectedState}.
 * <p>
 * Verifies the behavior of the canvas when a piece is currently selected and/or dragged.
 * Ensures that moves are correctly requested, selections are cleared, and out-of-bounds
 * drops cancel the action properly.
 */
public class PieceSelectedStateTest {

    private PieceSelectedState state;
    private FakeCanvas dummyCanvas;
    private final String ORIGIN = "F6";

    /**
     * A lightweight mock of the CanvasInterface to intercept interactions
     * such as move requests and UI visual updates.
     */
    class FakeCanvas implements CanvasInterface {
        public boolean simulatePieceExists = false;
        public String selectedHex = null;
        public PieceType draggedPiece = null;
        public CanvasInteractionState newState = null;
        public String lastMoveRequested = null;
        public double mouseX = 0;
        public double mouseY = 0;

        @Override public boolean hasPieceAt(String hex) { return simulatePieceExists; }
        @Override public PieceType getPieceAt(String hex) { return PieceType.BLACK_PAWN; }
        @Override public void setSelectedHex(String hex) { this.selectedHex = hex; }
        @Override public void setDraggedPiece(PieceType piece) { this.draggedPiece = piece; }
        @Override public void setMousePosition(double x, double y) { this.mouseX = x; this.mouseY = y; }
        @Override public void setState(CanvasInteractionState state) { this.newState = state; }
        @Override public void requestMove(String moveCommand) { this.lastMoveRequested = moveCommand; }
        @Override public void draw() {}
    }

    @BeforeEach
    void setUp() {
        state = new PieceSelectedState(ORIGIN);
        dummyCanvas = new FakeCanvas();
    }

    /**
     * Clicking outside the board while a piece is selected should cancel the selection.
     */
    @Test
    void testHandleMousePressed_NullCoordinate() {
        state.handleMousePressed(dummyCanvas, null, 0, 0);
        assertNull(dummyCanvas.selectedHex, "The selected hex should be cleared.");
        assertTrue(dummyCanvas.newState instanceof IdleCanvasState, "The state should revert to IdleCanvasState.");
    }

    /**
     * Clicking on another piece while one is already selected should switch the selection.
     */
    @Test
    void testHandleMousePressed_OnAnotherPiece() {
        dummyCanvas.simulatePieceExists = true;
        state.handleMousePressed(dummyCanvas, "G7", 100, 150);
        assertEquals("G7", dummyCanvas.selectedHex, "The new hex should be selected.");
        assertEquals(PieceType.BLACK_PAWN, dummyCanvas.draggedPiece, "The piece should be grabbed");
        assertEquals(100, dummyCanvas.mouseX);
        assertEquals(150, dummyCanvas.mouseY);
        assertNull(dummyCanvas.newState, "The state does not change (remains in PieceSelectedState)");
    }

    /**
     * Releasing the piece on a different hex after dragging should request a move.
     */
    @Test
    void testHandleMouseReleased_AfterDragging_ToNewHex_PlaysMove() {
        state.handleMouseDragged(dummyCanvas, 110, 110);
        state.handleMouseReleased(dummyCanvas, "G7", 150, 150);
        assertEquals("F6G7", dummyCanvas.lastMoveRequested, "The move should be requested");
        assertNull(dummyCanvas.selectedHex, "The selection should be cleared after the move");
        assertTrue(dummyCanvas.newState instanceof IdleCanvasState, "The state should revert to IdleCanvasState");
        assertNull(dummyCanvas.draggedPiece, "The dragged piece should be cleared");
    }

    /**
     * Releasing the piece back onto its starting hex should snap it back without moving.
     */
    @Test
    void testHandleMouseReleased_AfterDragging_ToSameHex() {
        state.handleMouseDragged(dummyCanvas, 110, 110);
        state.handleMouseReleased(dummyCanvas, ORIGIN, 100, 100);
        assertEquals(ORIGIN, dummyCanvas.selectedHex, "La sélection doit revenir sur l'origine");
        assertNull(dummyCanvas.lastMoveRequested, "Aucun coup ne doit être joué");
    }

    /**
     * Dropping a piece completely outside the board should cancel the drag.
     */
    @Test
    void testHandleMouseReleased_AfterDragging_OutOfBounds() {
        state.handleMouseDragged(dummyCanvas, 110, 110);
        state.handleMouseReleased(dummyCanvas, null, 1000, 1000);
        assertEquals(ORIGIN, dummyCanvas.selectedHex, "The selection should revert to the origin");
        assertNull(dummyCanvas.lastMoveRequested, "No move should be played");
    }

    /**
     * Releasing the mouse without dragging (just clicking) should drop the dragged piece state.
     */
    @Test
    void testHandleMouseReleased_WithoutDragging() {
        state.handleMouseReleased(dummyCanvas, "G7", 100, 100);
        assertNull(dummyCanvas.draggedPiece, "The dragged piece should be cleared");
        assertNull(dummyCanvas.lastMoveRequested, "No move should be sent here");
    }

    /**
     * Clicking an empty hex while a piece is selected (without drag-and-drop) should request a move.
     */
    @Test
    void testHandleMousePressed_OnEmptyHex_PlaysMove() {
        dummyCanvas.simulatePieceExists = false;
        state.handleMousePressed(dummyCanvas, "G7", 100, 150);
        assertEquals("F6G7", dummyCanvas.lastMoveRequested, "The move should be sent upon clicking");
        assertNull(dummyCanvas.selectedHex, "The selection should be cleared after the move");
        assertTrue(dummyCanvas.newState instanceof IdleCanvasState, "The state should revert to IdleCanvasState");
    }

}