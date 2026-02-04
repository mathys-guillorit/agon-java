package fr.univ.bordeaux.application.network.protocol;

import java.util.HashMap;
import java.util.Map;

//  Classe responsable de l'analyse (parsing) des messages réseau.
public class CommandParser {

    // Analyse une ligne reçue depuis le réseau et la transforme en Command.
    public Command parse(String line) {
        if (line == null) {
            return new Command(CommandType.UNKNOWN, Map.of());
        }

        String raw = line.trim();
        if (raw.isEmpty()) {
            return new Command(CommandType.UNKNOWN, Map.of());
        }

        String[] parts = raw.split("\\s+");

        // Premier mot = type
        CommandType type = CommandType.convertCommandType(parts[0]);

        // Le reste = args
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
