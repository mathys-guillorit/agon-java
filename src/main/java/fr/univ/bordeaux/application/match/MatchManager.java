package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Move;

/** Represent Match Behavior. */
public interface MatchManager extends ReadOnlyMatch {

  /**
   * Move a piece.
   *
   * @param move {@link Move}
   * @return true | false
   */
  boolean move(Move move);

  /**
   * Undo a turn (if enough history for it, at least 1 turn must be done).
   *
   * @return true undo succeeded else false
   */
  boolean undo();

  /**
   * redo a tun if enough history (at least 1 undo have been done before).
   *
   * @return true undo succeeded else false
   */
  boolean redo();

  /**
   * Stop turn state.
   *
   * @return true if it's possible to pause else false.
   */
  boolean pause();

  void setIsSaved(boolean isSaved);

  /** Quit the game. */
  void quit();

  void startTurn();
}
