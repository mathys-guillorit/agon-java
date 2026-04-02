package fr.univ.bordeaux.ui.gui.components.states;

/** The default state where the canvas is waiting for the user to select a piece. */
public class IdleCanvasState implements CanvasInteractionState {
  @Override
  public void handleMousePressed(
      CanvasInterface canvas, String agonCoordinate, double x, double y) {
    // Si on clique sur une case contenant une pièce
    if (agonCoordinate != null && canvas.hasPieceAt(agonCoordinate)) {
      canvas.setSelectedHex(agonCoordinate);
      canvas.setDraggedPiece(canvas.getPieceAt(agonCoordinate));
      canvas.setMousePosition(x, y);

      // On passe à l'état "Pièce Sélectionnée" !
      canvas.setState(new PieceSelectedState(agonCoordinate));
      canvas.draw();
    }
  }

  @Override
  public void handleMouseDragged(CanvasInterface canvas, double x, double y) {}

  @Override
  public void handleMouseReleased(
      CanvasInterface canvas, String agonCoordinate, double x, double y) {}
}
