package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.Options;

/**
 * Command responsible for pausing the game timers.
 *
 * <p>This command is specifically designed for Blitz mode, allowing players to temporarily stop the
 * countdown.
 */
public final class CmdPause extends Cmd {

  /** CLI options for the pause command (currently empty). */
  private Options opts;

  /**
   * Constructs the Pause command. Initializes the command name to "pause" and sets its default
   * description.
   *
   * @param uictx The user interface context for command interaction.
   */
  public CmdPause(GameUserInterface uictx) {
    super(uictx);
    this.opts = new Options();
    this.setName("pause");
    this.setDesc(
        "Description: Pauses the passing time. This command is only available when playing in Blitz mode.");
  }

  /**
   * Returns the help description for the pause command. * @return A formatted string describing the
   * command's purpose.
   */
  @Override
  public String getDescription() {
    return "Usage: pause\n" + "Description: Pauses the game timers in Blitz mode.\n";
  }

  /**
   * Executes the pause logic.
   *
   * <p>This method should interface with the match's timer system to suspend the current countdown.
   *
   * @param match The manager handling the current match state and timers.
   * @return true if the pause was successfully triggered.
   */
  @Override
  public boolean execute(MatchManager match) {
    // TODO: Implement timer suspension logic in MatchManager
    this.getCtx().showInfo("command reconnu mais pas impl");
    return false;
  }

  /**
   * Factory method to create an executable instance of the pause command.
   *
   * @param args Arguments passed in the CLI (ignored for pause).
   * @return A new {@link CmdPause} instance.
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdPause(super.getCtx());
  }

  /** Returns the CLI options for this command. * @return An empty {@link Options} object. */
  @Override
  public Options getOptions() {
    return this.opts;
  }
}
