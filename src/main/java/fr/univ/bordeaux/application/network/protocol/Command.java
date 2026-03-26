package fr.univ.bordeaux.application.network.protocol;

import java.util.Map;

// Represents a network command exchanged between a client and a server.
public class Command {

    private final CommandType type;
    private final Map<String, String> args;

    /**
     * Creates a new Command instance.
     *
     * @param type the type of the command (must not be {@code null})
     * @param args a map containing the command arguments, or an empty map
     *             if the command has no arguments
     */
    public Command(CommandType type, Map<String, String> args) {
        this.type = type;
        this.args = args;
    }

    /**
     * @return the type of this command.
     */
    public CommandType getType() {
        return type;
    }

    /**
     * Returns the value of a given argument.
     *
     * @param key the argument name
     * @return The value associated with the key
     */
    public String getArg(String key) {
        return args.get(key);
    }

    /**
     * Returns the parsed arguments.
     *
     * @return arguments map
     */
    public Map<String, String> getArgs() {
        return args;
    }
}