package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.application.match.MatchManager;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

/** Default for all commands (UI, GUI, etc..). */
public interface CmdAction {

  /** Execute actions provided by the specific command. */
  boolean execute(MatchManager match);

  /**
   * Create a new Game.
   *
   * @param args user input into the programm from CLI.
   * @return {@link CmdAction}
   */
  CmdAction createNew(String[] args);

  /** Show help for the specific sub (inherited) command. */
  String getDescription();

  /**
   * Override in sub commands.
   *
   * @return {@link Completer} for completing user writing with tab keycap
   */
  @Nonnull
  Completer getAutoCompleter();

  /**
   * Get command full name (it's not like options (example: -h --help) there is no reduced form).
   *
   * @return String command name
   */
  public String getName();

  /**
   * Get options of the command.
   *
   * @return {@link Options}
   */
  public Options getOptions();
}
