package fr.univ.bordeaux.ui.gui.components.states;

/**
 * Defines the contract for the HexagonCanvas interaction states. This allows implementing different
 * behaviors (like Click-to-Move or Drag-and-Drop) cleanly.
 */
public interface CanvasInteractionState {

  /**
   * Handles a mouse click on a specific hexagon.
   *
   * @param canvas The context (HexagonCanvas) to update its state or trigger redraws.
   * @param agonCoordinate The logical coordinate of the clicked hexagon (e.g., "F6").
   */
  void handleMousePressed(CanvasInterface canvas, String agonCoordinate, double x, double y);

  /**
   * Handles the mouse drag event for moving pieces.
   *
   * @param canvas The canvas context.
   * @param x The current X pixel coordinate.
   * @param y The current Y pixel coordinate.
   */
  void handleMouseDragged(CanvasInterface canvas, double x, double y);

  /**
   * Handles the mouse release event to finalize a move or drop a piece.
   *
   * @param canvas The canvas context.
   * @param agonCoordinate The target logical coordinate where the mouse was released.
   * @param x The release X pixel coordinate.
   * @param y The release Y pixel coordinate.
   */
  void handleMouseReleased(CanvasInterface canvas, String agonCoordinate, double x, double y);
}
