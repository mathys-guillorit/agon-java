package fr.univ.bordeaux.ui.gui.components.states;

/**
 * * The interaction state representing an active piece selection or an ongoing drag-and-drop
 * operation. Handles dropping a piece onto a target hexagon to trigger a move request.
 */
public class PieceSelectedState implements CanvasInteractionState {

  /** The starting logical coordinate of the selected piece. */
  private String originHex;

  /** Flag indicating whether the piece is currently being dragged across the canvas. */
  private boolean isDragging = false;

  /**
   * Initializes the state with the coordinate of the piece that was just selected.
   *
   * @param originHex The logical coordinate of the selected piece (e.g., "F6").
   */
  public PieceSelectedState(final String originHex) {
    this.originHex = originHex;
  }

  @Override
  public void handleMousePressed(
      final CanvasInterface canvas, final String agonCoordinate, final double x, final double y) {
    if (agonCoordinate == null) {
      canvas.setSelectedHex(null);
      canvas.setState(new IdleCanvasState());
    } else if (canvas.hasPieceAt(agonCoordinate)) {
      canvas.setSelectedHex(agonCoordinate);
      canvas.setDraggedPiece(canvas.getPieceAt(agonCoordinate));
      canvas.setMousePosition(x, y);
      this.originHex = agonCoordinate;
    } else {
      canvas.requestMove(originHex + "" + agonCoordinate);
      canvas.setSelectedHex(null);
      canvas.setState(new IdleCanvasState());
    }
    canvas.draw();
  }

  @Override
  public void handleMouseDragged(final CanvasInterface canvas, final double x, final double y) {
    isDragging = true;
    canvas.setMousePosition(x, y);
    canvas.draw();
  }

  @Override
  public void handleMouseReleased(
      final CanvasInterface canvas, final String targetHex, double x, double y) {
    if (isDragging) {
      if (targetHex != null && !targetHex.equals(originHex)) {
        canvas.requestMove(originHex + "" + targetHex);
        canvas.setSelectedHex(null);
        canvas.setState(new IdleCanvasState());
      } else {
        canvas.setSelectedHex(originHex);
      }
      isDragging = false;
    }
    canvas.setDraggedPiece(null);
    canvas.draw();
  }
}
