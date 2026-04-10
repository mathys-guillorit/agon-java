package fr.univ.bordeaux.ui.gui.components.states;

import fr.univ.bordeaux.agoncore.agonelements.PieceType;

/**
 * Represents the contract for the Hexagon Canvas UI component. Exposes methods required by
 * interaction states to manipulate the canvas visual state.
 */
public interface CanvasInterface {

  /**
   * Checks if a piece exists at the given hexagonal coordinate.
   *
   * @param hex The coordinate to check.
   * @return True if a piece is present, false otherwise.
   */
  boolean hasPieceAt(String hex);

  /**
   * Retrieves the piece type at the specified coordinate.
   *
   * @param hex The target coordinate.
   * @return The PieceType, or null if empty.
   */
  PieceType getPieceAt(String hex);

  /**
   * Sets the currently selected hexagon for highlighting.
   *
   * @param hex The coordinate to select.
   */
  void setSelectedHex(String hex);

  /**
   * Sets the piece currently being dragged by the user.
   *
   * @param piece The piece being dragged.
   */
  void setDraggedPiece(PieceType piece);

  /**
   * Updates the current mouse coordinates for rendering dragged pieces.
   *
   * @param x The X pixel coordinate.
   * @param y The Y pixel coordinate.
   */
  void setMousePosition(double x, double y);

  /**
   * Changes the active interaction state of the canvas.
   *
   * @param state The new state to apply.
   */
  void setState(CanvasInteractionState state);

  /**
   * Dispatches a move command to the game engine.
   *
   * @param moveCommand The move string (e.g., "F6G7").
   */
  void requestMove(String moveCommand);

  /** Triggers a full redraw of the canvas graphics. */
  void draw();
}
