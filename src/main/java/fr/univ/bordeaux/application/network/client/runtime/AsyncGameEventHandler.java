package fr.univ.bordeaux.application.network.client.runtime;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.network.OnlineGameInfo;
import fr.univ.bordeaux.application.network.OnlineGameStartListener;
import java.util.Locale;
import java.util.Map;

/** Handles asynchronous events related to online games. */
public class AsyncGameEventHandler {

  /**
   * Handles a MOVE_OK protocol event.
   *
   * @param line received protocol line
   * @param listener listener to notify
   */
  public void handleMoveConfirmed(final String line, final OnlineGameStartListener listener) {
    final String moveText = line.substring("MOVE_OK".length()).trim();
    AsyncEventLogger.logInfo("[ONLINE] Move accepted: " + moveText);

    if (listener != null && !moveText.isBlank()) {
      listener.onLocalMoveConfirmed(moveText);
    }
  }

  /**
   * Handles an OPPONENT_MOVE protocol event.
   *
   * @param line received protocol line
   * @param listener listener to notify
   */
  public void handleOpponentMove(final String line, final OnlineGameStartListener listener) {
    final String moveText = line.substring("OPPONENT_MOVE".length()).trim();
    AsyncEventLogger.logInfo("[ONLINE] Opponent played: " + moveText);

    if (listener != null && !moveText.isBlank()) {
      listener.onOpponentMoveReceived(moveText);
    }
  }

  /**
   * Handles a GAME_OVER protocol event.
   *
   * @param line received protocol line
   * @param listener listener to notify
   */
  public void handleGameOver(final String line, final OnlineGameStartListener listener) {
    logGameOverMessage(line);

    if (listener != null) {
      listener.onGameOver(line);
    }
  }

  /**
   * Handles a GAME_STARTED or NEW_OK protocol event.
   *
   * @param line received protocol line
   * @param listener listener to notify
   * @param eventParser parser used to extract protocol arguments
   */
  public void handleGameStartMessage(
      final String line,
      final OnlineGameStartListener listener,
      final AsyncEventParser eventParser) {
    final Map<String, String> parsedArguments = eventParser.parseProtocolArgs(line);

    try {
      final OnlineGameInfo onlineGameInfo = buildOnlineGameInfo(parsedArguments, line);

      if (onlineGameInfo != null && listener != null) {
        AsyncEventLogger.logInfo("[ONLINE] " + line);
        listener.onOnlineGameStarted(onlineGameInfo);
      }
    } catch (IllegalArgumentException exception) {
      AsyncEventLogger.logError("[CLIENT] Failed to parse game start message: " + line);
    }
  }

  /**
   * Builds the online game information from parsed protocol arguments.
   *
   * @param parsedArguments parsed protocol arguments
   * @param line original protocol line
   * @return built online game information, or null if invalid
   */
  private OnlineGameInfo buildOnlineGameInfo(
      final Map<String, String> parsedArguments, final String line) {
    OnlineGameInfo onlineGameInfo = null;

    final String gameIdValue = parsedArguments.get("GAME_ID");
    final String colorValue = parsedArguments.get("COLOR");
    final String whiteName = parsedArguments.get("WHITE");
    final String blackName = parsedArguments.get("BLACK");
    final String modeValue = parsedArguments.get("MODE");

    if (hasMissingGameStartField(gameIdValue, colorValue, whiteName, blackName, modeValue)) {
      AsyncEventLogger.logError("[CLIENT] Invalid game start message: " + line);
    } else {
      final int gameId = Integer.parseInt(gameIdValue);
      final Color localColor = Color.valueOf(colorValue.toUpperCase(Locale.ROOT));
      final boolean myTurn = localColor == Color.WHITE;
      final boolean blitzMode = "BLITZ".equalsIgnoreCase(modeValue);

      onlineGameInfo =
          new OnlineGameInfo(gameId, localColor, whiteName, blackName, myTurn, blitzMode);
    }

    return onlineGameInfo;
  }

  /**
   * Returns true if one mandatory field is missing from the game start message.
   *
   * @param gameIdValue game identifier
   * @param colorValue local player color
   * @param whiteName white player name
   * @param blackName black player name
   * @param modeValue game mode
   * @return true if one mandatory field is missing
   */
  private boolean hasMissingGameStartField(
      final String gameIdValue,
      final String colorValue,
      final String whiteName,
      final String blackName,
      final String modeValue) {
    return gameIdValue == null
        || colorValue == null
        || whiteName == null
        || blackName == null
        || modeValue == null;
  }

  /**
   * Logs the user-facing game-over message.
   *
   * @param line received protocol line
   */
  private void logGameOverMessage(final String line) {
    if (line.contains("RESULT=WIN") && line.contains("REASON=OPPONENT_LEFT")) {
      AsyncEventLogger.logInfo("[ONLINE] Opponent left the game. You win by forfeit.");
    } else if (line.contains("RESULT=LOSS") && line.contains("REASON=OPPONENT_LEFT")) {
      AsyncEventLogger.logInfo("[ONLINE] You resigned. You lose the game.");
    } else if (line.contains("RESULT=WIN")) {
      AsyncEventLogger.logInfo("[ONLINE] You win.");
    } else if (line.contains("RESULT=LOSS")) {
      AsyncEventLogger.logInfo("[ONLINE] You lose.");
    } else {
      AsyncEventLogger.logInfo("[ONLINE] " + line);
    }
  }
}
