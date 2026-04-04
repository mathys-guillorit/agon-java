package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;

/** Command responsible for pausing the game timers. */
public final class CmdPause extends Cmd {

  /**
   * Constructs the Pause command.
   *
   * @param uictx The user interface context for command interaction.
   */
  public CmdPause(GameUserInterface uictx) {
    super(uictx);
    this.setName("pause");
    String msg;
    msg = "Description: Pauses the passing time. ";
    msg += "This command is only available when playing in Blitz mode.";
    this.setDesc(msg);
  }

  /**
   * Returns the help description for the pause command.
   *
   * @return A formatted string describing the command's purpose.
   */
  @Override
  public String getDescription() {
    return "Usage: pause\n" + "Description: Pauses the game timers in Blitz mode.\n";
  }

  /**
   * Executes the pause logic.
   *
   * @param match The manager handling the current match state and timers.
   * @return true if the pause was successfully triggered.
   */
  @Override
  public boolean execute(MatchManager match) {
    if (match == null) {
      this.getCtx()
          .showInfo(
              "You must create a match before using this command. Type help for more informations");
      return false;
    }
    match.pause();
    return false;
  }

  /**
   * Factory method to create an executable instance of the pause command.
   *
   * @param args Arguments passed in the CLI (ignored for pause).
   * @return A new CmdPause instance.
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdPause(super.getCtx());
  }
}
