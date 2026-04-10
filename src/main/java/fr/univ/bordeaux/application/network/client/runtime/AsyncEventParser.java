package fr.univ.bordeaux.application.network.client.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Parses asynchronous protocol events and their arguments. */
public class AsyncEventParser {

  /** Supported asynchronous event types. */
  private static final Set<String> ASYNC_EVENT_TYPES =
      Set.of(
          "GAME_STARTED",
          "NEW_OK",
          "MOVE_OK",
          "OPPONENT_MOVE",
          "GAME_OVER",
          "YOUR_TURN",
          "ERROR",
          "INVITATION_SENT",
          "INVITATION_RECEIVED",
          "INVITATION_ACCEPTED",
          "INVITATION_DECLINED",
          "INVITATION_CANCELED",
          "LOBBY_JOINED",
          "WAITING_MODE",
          "CHOOSE_MODE",
          "DECLINE_OK");

  /** Events only displayed as informational messages. */
  private static final Set<String> INFO_ONLY_EVENTS =
      Set.of(
          "INVITATION_SENT",
          "INVITATION_RECEIVED",
          "INVITATION_ACCEPTED",
          "INVITATION_DECLINED",
          "INVITATION_CANCELED",
          "LOBBY_JOINED",
          "CHOOSE_MODE",
          "DECLINE_OK");

  /** Events starting a new online game context. */
  private static final Set<String> GAME_START_EVENTS = Set.of("NEW_OK", "GAME_STARTED");

  /**
   * Returns true if the received line is an asynchronous event.
   *
   * @param line received protocol line
   * @return true if the line is asynchronous, false otherwise
   */
  public boolean isAsyncEvent(final String line) {
    boolean asyncEvent = false;

    if (line != null && !line.isBlank()) {
      final String eventType = extractEventType(line);
      asyncEvent = ASYNC_EVENT_TYPES.contains(eventType);
    }

    return asyncEvent;
  }

  /**
   * Returns true if the event starts a game.
   *
   * @param eventType event type
   * @return true if the event starts a game
   */
  public boolean isGameStartEvent(final String eventType) {
    return GAME_START_EVENTS.contains(eventType);
  }

  /**
   * Returns true if the event is informational only.
   *
   * @param eventType event type
   * @return true if the event is informational only
   */
  public boolean isInfoOnlyEvent(final String eventType) {
    return INFO_ONLY_EVENTS.contains(eventType);
  }

  /**
   * Extracts the event type from a protocol line.
   *
   * @param line received protocol line
   * @return extracted event type
   */
  public String extractEventType(final String line) {
    String eventType = line;

    if (line.startsWith("ERROR MESSAGE=")) {
      eventType = "ERROR";
    } else {
      final int firstSpaceIndex = line.indexOf(' ');

      if (firstSpaceIndex >= 0) {
        eventType = line.substring(0, firstSpaceIndex);
      }
    }

    return eventType;
  }

  /**
   * Parses a protocol line formatted as COMMAND KEY=VALUE KEY=VALUE.
   *
   * @param line protocol line
   * @return parsed key/value pairs
   */
  public Map<String, String> parseProtocolArgs(final String line) {
    final Map<String, String> parsedArguments = new HashMap<>();

    if (line != null && !line.isBlank()) {
      final String[] tokens = line.trim().split("\\s+");

      for (int index = 1; index < tokens.length; index++) {
        parseToken(tokens[index], parsedArguments);
      }
    }

    return parsedArguments;
  }

  /**
   * Parses one KEY=VALUE token and stores it if valid.
   *
   * @param token token to parse
   * @param parsedArguments destination map
   */
  private void parseToken(final String token, final Map<String, String> parsedArguments) {
    final int equalsIndex = token.indexOf('=');

    if (equalsIndex > 0) {
      final String key = token.substring(0, equalsIndex);
      final String value = token.substring(equalsIndex + 1);

      if (!key.isEmpty()) {
        parsedArguments.put(key, value);
      }
    }
  }
}
