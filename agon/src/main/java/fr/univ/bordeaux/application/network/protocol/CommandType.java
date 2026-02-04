package fr.univ.bordeaux.application.network.protocol;

// Enumération des types de commandes réseau supportées.
public enum CommandType {
    PING, PONG, QUIT, BYE, UNKNOWN;

    // Convertit une chaîne de caractères en CommandType.
    public static CommandType convertCommandType(String text) {
        if (text == null) return UNKNOWN;
        String command = text.trim();
        if (command.isEmpty()) return UNKNOWN;

        try {
            return CommandType.valueOf(command);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
