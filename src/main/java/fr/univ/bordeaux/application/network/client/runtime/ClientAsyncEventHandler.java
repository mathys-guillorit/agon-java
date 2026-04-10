package fr.univ.bordeaux.application.network.client.runtime;

import fr.univ.bordeaux.application.network.OnlineGameStartListener;

/** Dispatches asynchronous protocol events received from the server. */
public class ClientAsyncEventHandler {

  /** Listener notified when online game events occur. */
  private volatile OnlineGameStartListener gameStartListener;

  /** Parser used for protocol event detection and argument extraction. */
  private final AsyncEventParser eventParser = new AsyncEventParser();

  /** Handler dedicated to game-related asynchronous events. */
  private final AsyncGameEventHandler gameEventHandler = new AsyncGameEventHandler();

  /** Handler dedicated to protocol error events. */
  private final AsyncProtocolErrorHandler protocolErrorHandler = new AsyncProtocolErrorHandler();

  /**
   * Registers the listener notified when online game events occur.
   *
   * @param listener listener to register
   */
  public void setGameStartListener(final OnlineGameStartListener listener) {
    this.gameStartListener = listener;
  }

  /**
   * Returns true if the received line is an asynchronous event.
   *
   * @param line received protocol line
   * @return true if the line is asynchronous, false otherwise
   */
  public boolean isAsyncEvent(final String line) {
    return eventParser.isAsyncEvent(line);
  }

  /**
   * Handles one asynchronous protocol event.
   *
   * @param line received asynchronous protocol line
   */
  public void handleAsyncEvent(final String line) {
    if (line != null && !line.isBlank()) {
      final String eventType = eventParser.extractEventType(line);
      dispatchEvent(eventType, line);
    }
  }

  /**
   * Dispatches the asynchronous event to the appropriate handler.
   *
   * @param eventType extracted event type
   * @param line full protocol line
   */
  private void dispatchEvent(final String eventType, final String line) {
    final OnlineGameStartListener listener = gameStartListener;

    if (eventParser.isGameStartEvent(eventType)) {
      gameEventHandler.handleGameStartMessage(line, listener, eventParser);
    } else if (eventParser.isInfoOnlyEvent(eventType)) {
      AsyncEventLogger.logInfo("[ONLINE] " + line);
    } else if ("WAITING_MODE".equals(eventType)) {
      AsyncEventLogger.logInfo("[ONLINE] Waiting for host to choose mode.");
    } else if ("MOVE_OK".equals(eventType)) {
      gameEventHandler.handleMoveConfirmed(line, listener);
    } else if ("OPPONENT_MOVE".equals(eventType)) {
      gameEventHandler.handleOpponentMove(line, listener);
    } else if ("GAME_OVER".equals(eventType)) {
      gameEventHandler.handleGameOver(line, listener);
    } else if ("ERROR".equals(eventType)) {
      protocolErrorHandler.handleProtocolError(line, listener);
    } else {
      AsyncEventLogger.logInfo("[SERVER] " + line);
    }
  }
}
