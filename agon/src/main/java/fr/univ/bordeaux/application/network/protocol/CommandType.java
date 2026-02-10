package fr.univ.bordeaux.application.network.protocol;

// Enumeration of supported network command types.
public enum CommandType {
    PING, PONG, QUIT, BYE, UNKNOWN;


    /**
     * Converts a raw string into a CommandType.
     *
     * @param text the raw command keyword extracted from the network message
     */
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
