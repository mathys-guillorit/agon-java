package fr.univ.bordeaux.application.network;

/** Listener notified when online game-related events occur on the client side. */
public interface OnlineGameStartListener {

  /**
   * Called when an online game has started.
   *
   * @param info the parsed online game information
   */
  void onOnlineGameStarted(OnlineGameInfo info);

  /**
   * Called when the server confirms a move played by the local client.
   *
   * @param rawMove the compact move text (e.g. "e2e4")
   */
  void onLocalMoveConfirmed(String rawMove);

  /**
   * Called when a move played by the opponent is received.
   *
   * @param rawMove the compact move text (e.g. "e7e5")
   */
  void onOpponentMoveReceived(String rawMove);

  /**
   * Called when the server notifies that the current online game has ended.
   *
   * @param line the raw protocol message describing the game result
   */
  void onGameOver(String line);

  /**
   * Called when the client should refresh the online board display.
   *
   * <p>This is typically triggered after an error or state update requiring a visual
   * synchronization with the server.
   */
  void onOnlineBoardRefreshRequested();
}
