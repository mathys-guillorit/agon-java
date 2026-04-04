package fr.univ.bordeaux.application.commands;

import java.util.HashMap;
import java.util.Optional;

/**
 * Register command to access it without command limits only one {@link CmdRegistry} can be created
 * at the same time (modular).
 */
public class CmdRegistry {

  private static CmdRegistry instance;
  private final HashMap<String, Icmd> commands;

  /** Initialize variables. */
  private CmdRegistry() {
    this.commands = new HashMap<>();
  }

  /**
   * Get the register.
   *
   * @return {@link CmdRegistry} that contains {@link String} associated {@link Icmd}
   */
  public static CmdRegistry getInstance() {
    if (instance == null) {
      instance = new CmdRegistry();
    }
    return instance;
  }

  /**
   * Register a {@link Icmd} to be used from an identifier for later access by {@link String} the
   * String is lowercased by the function.
   *
   * @param name command name
   * @param cmd class associated with the name (many names for same command is possible)
   */
  public void register(String name, Icmd cmd) {
    this.commands.put(name, cmd);
  }

  /**
   * Can return corresponding command if found else return null ({@link Optional} show that the
   * result can be null explicitly).
   *
   * @param cmdName command name
   * @return the concerned {@link Icmd} or null
   */
  public Optional<Icmd> get(String cmdName) {
    return Optional.ofNullable(this.commands.get(cmdName.toLowerCase()));
  }
}
