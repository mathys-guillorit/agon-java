package fr.univ.bordeaux.ui;

import fr.univ.bordeaux.application.match.ReadOnlyMatch;

/**
 * Defines a listener interested in state changes within an Agon match.
 *
 * <p>This interface follows the Observer design pattern. Any class implementing this interface
 * (such as a GUI or a Console logger) will be notified whenever the match state evolves (e.g.,
 * after a move, a capture, or a turn change).
 */
public interface MatchObserver {

  /**
   * Invoked when the state of the observed match has been updated.
   *
   * @param match A {@link ReadOnlyMatch} instance representing the current state. The match is
   *     provided in read-only mode to prevent observers from inadvertently modifying the game state
   *     during notification.
   */
  void onMatchUpdate(ReadOnlyMatch match);
}
