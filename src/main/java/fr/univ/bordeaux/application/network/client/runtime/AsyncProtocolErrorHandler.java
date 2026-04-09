package fr.univ.bordeaux.application.network.client.runtime;

import fr.univ.bordeaux.application.network.OnlineGameStartListener;

/** Handles asynchronous protocol error events. */
public class AsyncProtocolErrorHandler {

  /**
   * Handles a protocol error message coming from the server.
   *
   * @param line received protocol line
   * @param listener listener to notify
   */
  public void handleProtocolError(final String line, final OnlineGameStartListener listener) {
    final String message = line.substring("ERROR MESSAGE=".length()).trim();

    logProtocolError(message);

    if (listener != null) {
      listener.onOnlineBoardRefreshRequested();
    }
  }

  /**
   * Logs the user-facing protocol error message.
   *
   * @param message protocol error message
   */
  private void logProtocolError(final String message) {
    switch (message) {
      case "INVALID_MOVE" -> AsyncEventLogger.logWarn("[SERVER] Illegal move.");
      case "NOT_YOUR_TURN" -> AsyncEventLogger.logWarn("[SERVER] Not your turn.");
      case "MISSING_MOVE" -> AsyncEventLogger.logWarn("[SERVER] Missing move.");
      case "NOT_IN_GAME" -> AsyncEventLogger.logWarn("[SERVER] You are not in a game.");
      case "GAME_NOT_FOUND" -> AsyncEventLogger.logWarn("[SERVER] Game not found.");
      default -> AsyncEventLogger.logWarn("[SERVER] " + message);
    }
  }
}
