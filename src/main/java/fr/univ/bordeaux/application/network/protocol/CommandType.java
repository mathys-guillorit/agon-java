package fr.univ.bordeaux.application.network.protocol;

import java.util.Locale;

/** Enumeration of supported network command types. */
public enum CommandType {
  PING,
  PONG,
  QUIT,
  BYE,
  STATUS,
  STATUS_OK,
  LOGIN,
  WELCOME,
  PLAYERS,
  SCOREBOARD,
  NEW,
  MOVE,
  MOVE_OK,
  OPPONENT_MOVE,
  RESIGN,
  AWAY,
  BACK,
  ACCEPT,
  DECLINE,
  CANCEL,
  MODE,
  INVITATION_SENT,
  INVITATION_RECEIVED,
  INVITATION_ACCEPTED,
  INVITATION_DECLINED,
  INVITATION_CANCELED,
  LOBBY_JOINED,
  WAITING_MODE,
  CHOOSE_MODE,
  GAME_STARTED,
  GAME_OVER,
  UNKNOWN;

  /**
   * Converts a raw string into a CommandType.
   *
   * @param text the raw command keyword extracted from the network message
   * @return the corresponding {@link CommandType}, or {@code UNKNOWN} if the text is null, empty,
   *     or does not match any existing command
   */
  public static CommandType convertCommandType(final String text) {
    CommandType result = UNKNOWN;

    if (text != null) {
      final String command = text.trim();

      if (!command.isEmpty()) {
        try {
          result = CommandType.valueOf(command.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
          result = UNKNOWN;
        }
      }
    }

    return result;
  }
}
