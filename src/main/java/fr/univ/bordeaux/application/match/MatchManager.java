package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Move;

/**
 * Defines the control behaviors for an Agon match.
 * <p>This interface extends {@link ReadOnlyMatch} to provide write access and
 * game flow control, such as moving pieces, managing history (undo/redo),
 * and handling the match lifecycle (pause/quit).</p>
 */
public interface MatchManager extends ReadOnlyMatch {

  /**
   * Attempts to move a piece on the board according to the provided move data.
   *
   * @param move The {@link Move} containing the source and destination coordinates.
   * @return {@code true} if the move was valid and successfully applied, {@code false} otherwise.
   */
  boolean move(Move move);

  /**
   * Reverts the last turn played.
   * <p>This operation is only possible if there is enough move history (typically
   * requiring at least one full turn to have been completed).</p>
   *
   * @return {@code true} if the undo operation succeeded, {@code false} if no history is available.
   */
  boolean undo();

  /**
   * Replays a turn that was previously undone.
   * <p>This operation is only possible if an {@link #undo()} was performed
   * immediately prior and no new moves have been made since.</p>
   *
   * @return {@code true} if the redo operation succeeded, {@code false} otherwise.
   */
  boolean redo();

  /**
   * Suspends the current turn state, typically used for pausing timers in timed modes.
   *
   * @return {@code true} if the match was successfully paused, {@code false} if the
   * current match type does not support pausing.
   */
  boolean pause();

  /**
   * Updates the persistence status of the match.
   *
   * @param isSaved {@code true} if the current state has been synchronized with
   * a save file, {@code false} otherwise.
   */
  void setIsSaved(boolean isSaved);

  /**
   * Terminates the current match session and sets the status to finished.
   */
  void quit();

  /**
   * Initializes the beginning of a new turn.
   * <p>This method handles setup logic such as starting turn-based timers
   * or refreshing per-turn state variables.</p>
   */
  void startTurn();
}