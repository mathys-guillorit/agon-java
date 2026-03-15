package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.application.match.MatchManager;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

/**
 * default for all commands (UI, GUI, etc..)
 */
public interface CmdAction {

  /**
   * execute actions provided by the specific command
   */
  boolean execute(MatchManager match);

  CmdAction createNew(String[] args);

  /**
   * show help for the specific sub (inherited) command
   */
  String getDescription();

  /**
   * override in sub commands
   *
   * @return Completer for completing user writing with tab keycap
   */
  @Nonnull
  Completer getAutoCompleter();

  /**
   * get command full name (it's not like options (example: -h --help) there is no reduced form)
   *
   * @return String command name
   */
  public String getName();

  public Options getOptions();
}
