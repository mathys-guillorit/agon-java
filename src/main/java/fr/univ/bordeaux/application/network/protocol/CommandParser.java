package fr.univ.bordeaux.application.network.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for parsing raw network messages into {@link Command} objects.
 */
public class CommandParser {

    /**
     * Parses a raw network line into a {@link Command}.
     *
     * <p>The first token is interpreted as the command type. Remaining tokens are
     * parsed as {@code key=value} arguments.
     *
     * @param line the raw line received from the network
     * @return a {@link Command} object containing the identified {@link CommandType}
     *     and a map of its arguments
     */
    public static Command parse(String line) {
        if (line == null) {
            return new Command(CommandType.UNKNOWN, Map.of());
        }

        String raw = line.trim();
        if (raw.isEmpty()) {
            return new Command(CommandType.UNKNOWN, Map.of());
        }

        String[] parts = raw.split("\\s+");

        CommandType type = CommandType.convertCommandType(parts[0]);

        Map<String, String> args = new HashMap<>();
        for (int i = 1; i < parts.length; i++) {
            String token = parts[i];
            int eq = token.indexOf('=');

            if (eq <= 0) {
                continue;
            }

            String key = token.substring(0, eq);
            String value = token.substring(eq + 1);

            if (!key.isEmpty()) {
                args.put(key, value);
            }
        }

        return new Command(type, args);
    }
}