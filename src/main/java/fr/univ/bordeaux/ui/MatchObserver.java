package fr.univ.bordeaux.ui;

import fr.univ.bordeaux.application.match.ReadOnlyMatch;

/**
 * Defines the contract for objects that need to be notified of changes in the match state.
 *
 * <p>This interface is a key part of the Observer design pattern, allowing UI components
 * (like CLI or GUI) to synchronize their display whenever the game state evolves.
 *
 */
public interface MatchObserver {

  /**
   * Invoked when the state of the match has changed.
   *
   * @param match A {@link ReadOnlyMatch} representing the new state of the game.
   */
  void onMatchUpdate(ReadOnlyMatch match);
}