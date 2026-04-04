package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.Options;

/** Command responsible for replaying previously undone moves. */
public final class CmdRedo extends Cmd {

  private int redoNumber = 1;

  /**
   * Constructs the base Redo command for registration.
   *
   * @param uictx The user interface context for interaction.
   */
  public CmdRedo(GameUserInterface uictx) {
    super(uictx);
    this.setName("redo");
    String msg = "Description: Replays the last canceled turn.";
    msg += "If a number N is provided, it replays the last N canceled turns.";
    this.setDesc(msg);
    Options options = super.getOptions();
    options.addOption("n", "number", true, "Number of turns to redo");
  }

  /**
   * Internal constructor used to create an executable instance with a specific count.
   *
   * @param uictx The user interface context.
   * @param redoNumber The specific number of moves to restore.
   */
  private CmdRedo(GameUserInterface uictx, int redoNumber) {
    this(uictx);
    this.redoNumber = redoNumber;
  }

  /**
   * Returns the help description for the redo command.
   *
   * @return A formatted string showing usage and examples.
   */
  @Override
  public String getDescription() {
    return "Usage: redo [N]\n"
        + "Description: Replays the last N canceled turns.\n"
        + "Example: redo 2\n";
  }

  /**
   * Executes the redo logic.
   *
   * @param match The manager handling the game history and state.
   * @return true if the execution completed (even if partially).
   */
  @Override
  public boolean execute(MatchManager match) {
    if (match == null) {
      this.getCtx().showError("No active match found.\n");
      return false;
    }
    for (int i = 0; i < redoNumber; i++) {
      if (!match.redo()) {
        this.getCtx().showWarn("No more moves to redo.\n");
        break;
      }
    }
    return false;
  }

  /**
   * Factory method to create an executable instance of the redo command.
   *
   * @param args CLI arguments (e.g., ["3"] to redo three times).
   * @return A new CmdRedo instance with the specified count.
   */
  @Override
  public CmdAction createNew(String[] args) {
    int count = 1;
    if (args.length > 0) {
      String arg0 = args[0];
      try {
        count = Integer.parseInt(arg0);
      } catch (NumberFormatException e) {
        this.getCtx().showError("Invalid number format for redo: " + arg0 + ". Defaulting to 1.\n");
      }
    }
    return new CmdRedo(this.getCtx(), count);
  }
}
