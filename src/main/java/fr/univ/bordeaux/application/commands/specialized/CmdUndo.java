package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** Command responsible for canceling previously played moves. */
public final class CmdUndo extends Cmd {

  private int undoNumber = 1;

  /**
   * Constructs the base Undo command for registration.
   *
   * @param uictx The user interface context for interaction.
   */
  public CmdUndo(GameUserInterface uictx) {
    super(uictx);
    this.setName("undo");
    String msg = "Description: Cancels the last played turn.";
    msg += " If a number N is provided, it cancels the last N turns.";
    this.setDesc(msg);
    Options options = super.getOptions();
    options.addOption("n", "number", true, "Number of turns to undo");
  }

  /**
   * Internal constructor used to create an executable instance with
   * a specific count.
   *
   * @param uictx The user interface context.
   * @param undoNumber The specific number of moves to revert.
   */
  private CmdUndo(GameUserInterface uictx, int undoNumber) {
    this(uictx);
    this.undoNumber = undoNumber;
  }

  /**
   * Returns the help description and usage examples for the undo command.
   *
   * @return A formatted string for the help menu.
   */
  @Override
  public String getDescription() {
    return "Usage: undo [N] or undo -n [N]\n"
        + "Description: Cancels the last N played turns.\n"
        + "Example: undo 2\n";
  }

  /**
   * Executes the undo logic.
   *
   * @param match The manager handling the game history.
   * @return true if the execution completed, false if no match was found.
   */
  @Override
  public boolean execute(MatchManager match) {
    if (match == null) {
      this.getCtx().showError("No active match found.\n");
      return false;
    }

    for (int i = 0; i < undoNumber; i++) {
      if (!match.undo()) {
        String msg = "You can't undo anymore. Please play a move or use redo.";
        this.getCtx().showWarn(msg + "\n");
        break;
      }
    }
    return true;
  }

  /**
   * Factory method to create an executable CmdUndo instance.
   *
   * @param args CLI arguments.
   * @return A new specialized CmdUndo instance.
   */
  @Override
  public CmdAction createNew(String[] args) {
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine line = parser.parse(super.getOptions(), args);

      int n = 1;
      if (line.hasOption("n")) {
        n = Integer.parseInt(line.getOptionValue("n"));
      } else if (args.length > 0) {
        n = Integer.parseInt(args[0]);
      }

      return new CmdUndo(this.getCtx(), n);

    } catch (ParseException | NumberFormatException e) {
      final String msg;
      msg = "Invalid format. Usage: undo [-n <number>] or undo <number>";
      this.getCtx().showError(msg + "\n");
      return null;
    }
  }
}
