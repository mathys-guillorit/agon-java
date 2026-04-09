package fr.univ.bordeaux.ui;

/**
 * Defines the contract for a match that can be monitored by a user interface.
 * * <p>This interface allows an {@link MatchObserver} to register itself to receive
 * real-time updates whenever the game state changes (e.g., after a move, undo, or redo).
 */
public interface ObservableMatch {

  /**
   * Registers a single observer to be notified of match updates.
   *
   * @param observer The {@link MatchObserver} (typically a CLI or GUI) to attach.
   */
  void setObserver(MatchObserver observer);

  /**
   * Triggers a notification to the attached observer.
   * * <p>This method should be called manually or automatically after any significant
   * state change to synchronize the display with the internal game logic.
   */
  void notifyUi();
}