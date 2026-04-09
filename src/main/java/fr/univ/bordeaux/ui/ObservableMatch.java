package fr.univ.bordeaux.ui;

/**
 * Defines the contract for a match that can be observed by a user interface.
 *
 * <p>This interface allows an external observer to register itself to receive updates. It ensures a
 * decoupled relationship between the game logic and the display logic, following the Observer
 * design pattern.
 */
public interface ObservableMatch {

  /**
   * Registers an observer to be notified of any changes in the match state.
   *
   * @param observer The {@link MatchObserver} that will listen for updates.
   */
  void setObserver(MatchObserver observer);

  /**
   * Manually triggers a notification to the registered observer. *
   *
   * <p>This is typically called after a significant state change (like a move or a game reset) to
   * ensure the user interface reflects the most current board configuration.
   */
  void notifyUi();
}
