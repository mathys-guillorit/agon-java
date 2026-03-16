package fr.univ.bordeaux.agoncore.history;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Manages the history of turns to support undo and redo operations.
 *
 * <p>This class maintains two stacks of {@link HistoryInformations}: * *
 *
 * <ul>
 *   <li><b>Undo Stack:</b> Stores the history of moves already played.
 *   <li><b>Redo Stack:</b> Stores moves that were undone and are available to be re-applied.
 * </ul>
 * <p>
 * *
 *
 * <p>Following standard command patterns, the redo stack is cleared whenever a new, original move
 * is added to the history to prevent branching timelines.
 */
public class History {

  /**
   * Stack containing turns that can be reverted.
   */
  private final Stack<HistoryInformations> undoStack = new Stack<>();

  /**
   * Stack containing turns that were reverted and can be re-applied.
   */
  private final Stack<HistoryInformations> redoStack = new Stack<>();

  /**
   * Initializes an empty game history.
   */
  public History() {
  }

  /**
   * Retrieves the most recent turn from the undo stack without removing it.
   *
   * @return The latest {@link HistoryInformations} on the undo stack.
   * @throws java.util.EmptyStackException if the undo stack is empty.
   */
  public HistoryInformations getHeadUndo() {
    return undoStack.peek();
  }

  /**
   * Retrieves the most recent turn from the redo stack without removing it.
   *
   * @return The latest {@link HistoryInformations} on the redo stack.
   * @throws java.util.EmptyStackException if the redo stack is empty.
   */
  public HistoryInformations getHeadRedo() {
    return redoStack.peek();
  }

  /**
   * Records a new turn in the history.
   *
   * <p>The turn is pushed onto the undo stack, and the redo stack is immediately cleared to ensure
   * history consistency.
   *
   * @param informations The {@link HistoryInformations} containing the move sequence to record.
   */
  public void add(HistoryInformations informations) {
    undoStack.push(informations);
    redoStack.clear();
  }

  /**
   * Reverts the last recorded turn.
   *
   * <p>Pops the latest turn from the undo stack and transfers it to the redo stack.
   *
   * @return The {@link HistoryInformations} that was undone, or {@code null} if no moves are
   * available to revert.
   */
  public HistoryInformations undo() {
    if (undoStack.isEmpty()) {
      return null;
    }
    HistoryInformations informations = undoStack.pop();
    redoStack.push(informations);
    return informations;
  }

  /**
   * Re-applies the most recently undone turn. *
   *
   * <p>Pops the turn from the redo stack and transfers it back to the undo stack.
   *
   * @return The {@link HistoryInformations} that was re-applied.
   * @throws java.util.EmptyStackException if the redo stack is empty.
   */
  public HistoryInformations redo() {
    HistoryInformations informations = redoStack.pop();
    undoStack.push(informations);
    return informations;
  }

  /**
   * Checks if there are any turns available to undo.
   *
   * @return {@code true} if the undo stack is empty, {@code false} otherwise.
   */
  public boolean isEmptyUndo() {
    return undoStack.isEmpty();
  }

  /**
   * Checks if there are any turns available to redo.
   *
   * @return {@code true} if the redo stack is empty, {@code false} otherwise.
   */
  public boolean isEmptyRedo() {
    return redoStack.isEmpty();
  }

  /**
   * Returns a list representation of the move history. This method converts the internal undo stack
   * into a list, reversing the order so that the moves are presented in chronological order (from
   * the first move to the most recent one).
   *
   * @return a {@link List} of {@link HistoryInformations} containing all performed moves in
   * chronological order.
   */
  public List<HistoryInformations> toList() {
    return new ArrayList<>(undoStack).reversed();
  }
}
