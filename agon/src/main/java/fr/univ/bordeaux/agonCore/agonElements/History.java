package fr.univ.bordeaux.agonCore.agonElements;

import java.util.Stack;

public class History {

  Stack<Move> undoStack = new Stack<>();
  Stack<Move> redoStack = new Stack<>();

  public History() {
  }

  public Move getHeadUndo() {
    return undoStack.peek();
  }

  public Move getHeadRedo() {
    return redoStack.peek();
  }

  public void add(Move move) {
    undoStack.push(move);
  }

  public Move undo() {
    if (undoStack.isEmpty()) {
      return null;
    }
    Move move = undoStack.pop();
    redoStack.push(move);
    return move;
  }

  public Move redo() {
    Move move = redoStack.pop();
    undoStack.push(move);
    return move;
  }

  public boolean isEmptyUndo() {
    return undoStack.isEmpty();
  }

  public boolean isEmptyRedo() {
    return redoStack.isEmpty();
  }

}