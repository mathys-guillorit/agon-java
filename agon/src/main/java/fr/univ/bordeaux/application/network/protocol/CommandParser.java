package fr.univ.bordeaux.application.network.protocol;

import java.util.HashMap;
import java.util.Map;

// Responsible for parsing raw network messages into Command objects
public class CommandParser {

    /**
     * Parses a raw network line into a Command.
     * The input line is expected to be composed of ASCII characters and
     * terminated by a newline character ('\n') on the wire.
     *
     * @param line the raw line received from the network
     */
    public Command parse(String line) {
        if (line == null) {
            return new Command(CommandType.UNKNOWN, Map.of());
        }

        String raw = line.trim();
        if (raw.isEmpty()) {
            return new Command(CommandType.UNKNOWN, Map.of());
        }

        String[] parts = raw.split("\\s+");

        // First token corresponds to the command type
        CommandType type = CommandType.convertCommandType(parts[0]);

        // Remaining tokens are parsed as key=value arguments
        Map<String, String> args = new HashMap<>();
        for (int i = 1; i < parts.length; i++) {
            String token = parts[i];
            int eq = token.indexOf('=');
            if (eq <= 0) continue;

            String key = token.substring(0, eq);
            String value = token.substring(eq + 1);
            if (!key.isEmpty()) {
                args.put(key, value);
            }
        }

        return new Command(type, args);
    }
}
