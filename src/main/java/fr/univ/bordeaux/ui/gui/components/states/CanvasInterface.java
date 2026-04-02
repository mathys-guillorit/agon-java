package fr.univ.bordeaux.ui.gui.components.states;

import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.ui.gui.components.states.CanvasInteractionState;

public interface CanvasInterface {
    boolean hasPieceAt(String hex);
    PieceType getPieceAt(String hex);
    void setSelectedHex(String hex);
    void setDraggedPiece(PieceType piece);
    void setMousePosition(double x, double y);
    void setState(CanvasInteractionState state);
    void requestMove(String moveCommand);
    void draw();
}
