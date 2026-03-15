package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public final class CmdUndo extends Cmd {

  private final Options options;
  private int undoNumber;
  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdUndo(GameUserInterface uictx) {
    super(uictx);
    this.setDesc(
        "Description: Cancels the last played turn. If a number N is provided, it cancels the last N turns.");
    this.setName("undo");
    this.options = new Options();
    this.options.addOption("n", "number", true, "Number of turns to undo");
  }


  public CmdUndo(GameUserInterface uictx, int undoNumber) {
    this(uictx);
    this.undoNumber=undoNumber;
  }
  @Override
  public String getDescription() {
    return "Undo";
  }

  public boolean execute(MatchManager match) {
    if (match == null) {
      super.getCtx().showMessage("No active match found.\n");
      return false;
    }
    for (int i = 0; i < undoNumber; i++) {
      if (!match.undo()){
        super.getCtx().showMessage("You can't undo anymore please redo or play a move.\n");
        return true;
      };
    }
    return true;
  }

  public CmdAction createNew(String[] args) {
    CommandLineParser parser = new DefaultParser();
    try {
      // Le parser compare les 'args' avec la définition 'this.options'
      CommandLine line = parser.parse(this.options, args);

      int n = 1;
      if (line.hasOption("n")) {
        n = Integer.parseInt(line.getOptionValue("n"));
      } else if (args.length > 0) {
        // Optionnel : permet de supporter "undo 3" sans le "-n"
        n = Integer.parseInt(args[0]);
      }

      return new CmdUndo(super.getCtx(), n);

    } catch (ParseException | NumberFormatException e) {
      super.getCtx().showMessage("Usage: undo [-n <number>] or undo <number>");
      return null;
    }
  }
}
