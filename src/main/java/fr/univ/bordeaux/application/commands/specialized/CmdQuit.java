package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Command responsible for safely exiting the Agon application.
 *
 * <p>This command triggers the shutdown sequence in both the current match and the user interface,
 * typically asking for a save confirmation before closing.
 */
public class CmdQuit extends Cmd {

  /**
   * Constructs a new Quit command. Initializes the command name to "quit" and its CLI options.
   *
   * @param uictx The user interface context to close upon execution.
   */
  public CmdQuit(GameUserInterface uictx) {
    super(uictx);
    this.setName("quit");
  }

  /**
   * Provides the usage and description for the quit command.
   *
   * @return A formatted string for the help menu.
   */
  @Override
  public String getDescription() {
    return "Usage: quit (or Ctrl+C)\n"
        + "Description: Exits the game. You will be prompted to save your current progress before leaving.\n";
  }

  /**
   * Executes the shutdown sequence.
   *
   * <p>Calls the quit method on the {@link MatchManager} (if active) and then signals the {@link
   * GameUserInterface} to terminate the session.
   *
   * @param match The manager for the current game session.
   * @return true always, as the command successfully initiates the exit.
   */
  @Override
  public boolean execute(MatchManager match) {
    if (match != null) {
      match.quit();
    }
    this.getCtx().quit();
    return true;
  }

  /**
   * Factory method to create an executable instance of the quit command.
   *
   * @param args Arguments passed in CLI (ignored for quit).
   * @return A new {@link CmdQuit} instance.
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdQuit(super.getCtx());
  }
}
