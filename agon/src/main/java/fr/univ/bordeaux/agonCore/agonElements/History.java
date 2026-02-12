package fr.univ.bordeaux.agonCore.agonElements;

import java.util.Stack;

public class History {

  Stack<Move> undoStack = new Stack<>();
  Stack<Move> redoStack = new Stack<>();

  public History() {
  }
  /**
   * Retrieves the most recent move from the undo stack without removing it.
   * * @return The latest {@link Move} on the undo stack.
   * @throws java.util.EmptyStackException if the undo stack is empty.
   */
  public Move getHeadUndo() {
    return undoStack.peek();
  }

  /**
   * Retrieves the most recent move from the redo stack without removing it.
   * * @return The latest {@link Move} on the redo stack.
   * @throws java.util.EmptyStackException if the redo stack is empty.
   */
  public Move getHeadRedo() {
    return redoStack.peek();
  }

  /**
   * Adds a new move to the history.
   * Usually called when a move is played on the board.
   * * @param move The {@link Move} to be added to the undo history.
   */
  public void add(Move move) {
    undoStack.push(move);
    redoStack.clear();
  }

  /**
   * Reverts the last move.
   * Pops the move from the undo stack and pushes it onto the redo stack.
   * * @return The {@link Move} that was undone, or {@code null} if no moves are available.
   */
  public Move undo() {
    if (undoStack.isEmpty()) {
      return null;
    }
    Move move = undoStack.pop();
    redoStack.push(move);
    return move;
  }

  /**
   * Re-applies the last undone move.
   * Pops the move from the redo stack and pushes it back onto the undo stack.
   * * @return The {@link Move} that was re-applied.
   * @throws java.util.EmptyStackException if the redo stack is empty.
   */
  public Move redo() {
    Move move = redoStack.pop();
    undoStack.push(move);
    return move;
  }

  /**
   * Checks if there are any moves available to undo.
   * * @return {@code true} if the undo stack is empty, {@code false} otherwise.
   */
  public boolean isEmptyUndo() {
    return undoStack.isEmpty();
  }

  /**
   * Checks if there are any moves available to redo.
   * * @return {@code true} if the redo stack is empty, {@code false} otherwise.
   */
  public boolean isEmptyRedo() {
    return redoStack.isEmpty();
  }
}