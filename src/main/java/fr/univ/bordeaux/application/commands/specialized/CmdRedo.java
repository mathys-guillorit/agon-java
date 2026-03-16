package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.Options;

public final class CmdRedo extends Cmd {

  private Options options;
  private int redoNumber;
  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdRedo(GameUserInterface uictx) {
    super(uictx);
    this.setDesc(
        "Description: Replays the last canceled turn. If a number N is provided, it replays the last N canceled turns.\n");
    this.setName("redo");
    this.options = new Options();
    this.options.addOption("n", "number", true, "Number of turns to redo");
  }

  public CmdRedo(GameUserInterface uictx, int redoNumber) {
    this(uictx);
    this.redoNumber=redoNumber;
  }
  @Override
  public String getDescription() {
    return "Redo";
  }


  public boolean execute(MatchManager match) {
    if (match == null) {
      super.getCtx().showMessage("No active match found.\n");
      return false;
    }

    for (int i = 0; i < redoNumber; i++) {
      if (!match.undo()){
        super.getCtx().showMessage("You can't undo anymore please redo or play a move.\n");
        return true;
      };
    }
    return true;
  }

  public CmdAction createNew(String[] args) {
    return new CmdRedo(super.getCtx());
  }
}
