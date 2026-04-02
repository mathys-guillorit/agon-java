package fr.univ.bordeaux.ui.gui.components.states;

import fr.univ.bordeaux.ui.gui.components.HexagonCanvas;

/**
 * Defines the contract for the HexagonCanvas interaction states.
 * This allows implementing different behaviors (like Click-to-Move or Drag-and-Drop) cleanly.
 */
public interface CanvasInteractionState {

    /**
     * Handles a mouse click on a specific hexagon.
     *
     * @param canvas       The context (HexagonCanvas) to update its state or trigger redraws.
     * @param agonCoordinate The logical coordinate of the clicked hexagon (e.g., "F6").
     */
    void handleMousePressed(CanvasInterface canvas, String agonCoordinate, double x, double y);
    void handleMouseDragged(CanvasInterface canvas, double x, double y);
    void handleMouseReleased(CanvasInterface canvas, String agonCoordinate, double x, double y);
}
