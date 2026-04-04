package fr.univ.bordeaux.application.network.protocol;

import java.util.Map;

/** Represents a network command exchanged between a client and a server. */
public class Command {

  private final CommandType type;
  private final Map<String, String> args;
  private final String rawArg;

  /**
   * Creates a new Command instance.
   *
   * @param type the type of the command (must not be {@code null})
   * @param args a map containing the command arguments, or an empty map if the command has no
   *     arguments
   */
  public Command(CommandType type, Map<String, String> args) {
    this.type = type;
    this.args = args;
    this.rawArg = null;
  }

  /**
   * Creates a new command.
   *
   * @param type the parsed command type
   * @param args parsed key=value arguments
   * @param rawArgument optional raw argument
   */
  public Command(CommandType type, Map<String, String> args, String rawArgument) {
    this.type = type;
    this.args = args;
    this.rawArg = rawArgument;
  }

  /**
   * Retrieves the specific category or classification of this command.
   *
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

  /**
   * Returns the optional raw argument.
   *
   * <p>This is mainly used for move-related messages such as "MOVE e2e4".
   *
   * @return the raw argument, or null if none exists
   */
  public String getRawArgument() {
    return rawArg;
  }
}
