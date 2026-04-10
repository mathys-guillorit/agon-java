package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.application.match.MatchManager;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

/** * Default interface for all game commands (CLI Shell, GUI, etc.).
 * Defines the contract for command execution, help display, and auto-completion.
 */
public interface CmdAction {

  /** * Execute actions provided by the specific command.
   * @param match The {@link MatchManager} to interact with the current game state.
   * @return {@code true} if the execution was successful, {@code false} otherwise.
   */
  boolean execute(MatchManager match);

  /**
   * Factory method to create an executable instance of the command with specific arguments.
   *
   * @param args User input arguments parsed from the CLI.
   * @return A new {@link CmdAction} instance configured with the provided arguments.
   */
  CmdAction createNew(String[] args);

  /** * Retrieves a brief summary of the command's purpose.
   * @return A {@link String} containing the short description.
   */
  String getDescription();

  /**
   * Provides a JLine completer to assist the user with tab-completion in the shell.
   *
   * @return A non-null {@link Completer} instance tailored for this command's options.
   */
  @Nonnull
  Completer getAutoCompleter();

  /**
   * Get the command's full trigger name (e.g., "new", "save").
   * Unlike options, there is no short or reduced form for the command name itself.
   *
   * @return The unique {@link String} identifier of the command.
   */
  String getName();

  /**
   * Retrieves the set of CLI options (flags) available for this specific command.
   *
   * @return An Apache Commons CLI {@link Options} object.
   */
  Options getOptions();

  /**
   * Provides detailed usage information and instructions for the command.
   *
   * @return A {@link String} representing the full help/usage manual for the command.
   */
  String getHelp();
}