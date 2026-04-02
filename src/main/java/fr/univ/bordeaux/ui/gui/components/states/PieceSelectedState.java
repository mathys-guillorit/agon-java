package fr.univ.bordeaux.ui.gui.components.states;

public class PieceSelectedState implements CanvasInteractionState {

  private String originHex;
  private boolean isDragging = false; // Notre fameux détective !

  public PieceSelectedState(String originHex) {
    this.originHex = originHex;
  }

  @Override
  public void handleMousePressed(
      CanvasInterface canvas, String agonCoordinate, double x, double y) {
    if (agonCoordinate == null) {
      canvas.setSelectedHex(null);
      canvas.setState(new IdleCanvasState());
    } else if (canvas.hasPieceAt(agonCoordinate)) {
      // Clic sur une autre pièce (ou la même) : On change l'origine
      canvas.setSelectedHex(agonCoordinate);
      canvas.setDraggedPiece(canvas.getPieceAt(agonCoordinate));
      canvas.setMousePosition(x, y);
      this.originHex = agonCoordinate;
    } else {
      // Clic sur une case vide : ON JOUE ! (Cas du Clic Simple)
      canvas.requestMove(originHex + "" + agonCoordinate);
      canvas.setSelectedHex(null);
      canvas.setState(new IdleCanvasState());
    }
    canvas.draw();
  }

  @Override
  public void handleMouseDragged(CanvasInterface canvas, double x, double y) {
    isDragging = true; // Le détective valide le Drag !
    canvas.setMousePosition(x, y);
    canvas.draw();
  }

  @Override
  public void handleMouseReleased(CanvasInterface canvas, String targetHex, double x, double y) {
    if (isDragging) {
      if (targetHex != null && !targetHex.equals(originHex)) {
        // DRAG & DROP VALIDE : ON JOUE !
        canvas.requestMove(originHex + "" + targetHex);
        canvas.setSelectedHex(null);
        canvas.setState(new IdleCanvasState());
      } else {
        // Relâché sur place
        canvas.setSelectedHex(originHex);
      }
      isDragging = false;
    }
    // On nettoie la pièce volante du curseur (que l'on ait bougé ou non)
    canvas.setDraggedPiece(null);
    canvas.draw();
  }
}
