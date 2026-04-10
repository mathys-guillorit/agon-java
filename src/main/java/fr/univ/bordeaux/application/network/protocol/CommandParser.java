package fr.univ.bordeaux.application.network.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Responsible for parsing raw network messages into {@link Command} objects. */
public final class CommandParser {

  /** Utility class. */
  private CommandParser() {}

  /**
   * Parses a raw network line into a {@link Command}.
   *
   * <p>The first token is interpreted as the command type. Remaining tokens are parsed as {@code
   * key=value} arguments.
   *
   * @param line the raw line received from the network
   * @return a {@link Command} object containing the identified {@link CommandType} and a map of its
   *     arguments
   */
  public static Command parse(final String line) {
    Command result;

    if (line == null) {
      result = new Command(CommandType.UNKNOWN, Map.of());
    } else {
      final String rawLine = line.trim();

      if (rawLine.isEmpty()) {
        result = new Command(CommandType.UNKNOWN, Map.of());
      } else {
        final String[] parts = rawLine.split("\\s+");
        final CommandType type = CommandType.convertCommandType(parts[0]);

        final Map<String, String> arguments = new ConcurrentHashMap<>();
        String rawArg = null;

        for (int index = 1; index < parts.length; index++) {
          final String token = parts[index];
          final int equalIndex = token.indexOf('=');

          if (equalIndex > 0) {
            final String key = token.substring(0, equalIndex);
            final String value = token.substring(equalIndex + 1);

            if (!key.isEmpty()) {
              arguments.put(key, value);
            }
          } else if (rawArg == null) {
            rawArg = token;
          }
        }

        result = new Command(type, arguments, rawArg);
      }
    }

    return result;
  }
}
