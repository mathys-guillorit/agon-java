package fr.univ.bordeaux.ui.gui.components.states;

/**
 * * The default interaction state where the canvas is waiting for the user to select a piece.
 * Clicking a valid piece transitions the canvas to the {@link PieceSelectedState}.
 */
public class IdleCanvasState implements CanvasInteractionState {

  @Override
  public void handleMousePressed(
      final CanvasInterface canvas, final String agonCoordinate, final double x, final double y) {

    if (agonCoordinate != null) {
      if (canvas.hasPieceAt(agonCoordinate)) {
        canvas.setSelectedHex(agonCoordinate);
        canvas.setDraggedPiece(canvas.getPieceAt(agonCoordinate));
        canvas.setMousePosition(x, y);

        canvas.setState(new PieceSelectedState(agonCoordinate));
        canvas.draw();
      } else {
        canvas.requestMove(agonCoordinate);
      }
    }
  }

  @Override
  public void handleMouseDragged(CanvasInterface canvas, double x, double y) {}

  @Override
  public void handleMouseReleased(
      CanvasInterface canvas, String agonCoordinate, double x, double y) {}
}
