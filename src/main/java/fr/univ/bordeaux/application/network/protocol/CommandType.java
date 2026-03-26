package fr.univ.bordeaux.application.network.protocol;

/**
 * Enumeration of supported network command types.
 */
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
    UNKNOWN;

    /**
     * Converts a raw string into a CommandType.
     *
     * @param text the raw command keyword extracted from the network message
     * @return the corresponding {@link CommandType}, or {@code UNKNOWN} if the
     *     text is null, empty, or does not match any existing command
     */
    public static CommandType convertCommandType(String text) {
        if (text == null) {
            return UNKNOWN;
        }

        String command = text.trim();
        if (command.isEmpty()) {
            return UNKNOWN;
        }

        try {
            return CommandType.valueOf(command.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}