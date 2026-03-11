package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.AbstractGameUI;

public class CmdShow extends Cmd {

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdShow(AbstractGameUI uictx) {
    super(uictx);
    this.setDesc("Description: Displays specific information about the current game state.");
    this.setName("Show");
  }

  @Override
  public String getDescription() {
    return "";
  }

  public void execute(MatchManager match) {

  }

  public CmdAction createNew(String[] args) {
    return null;
  }

  @Override
  public void showHelp() {
    this.getCtx().showMessage("Usage: show [target]\n");
    this.getCtx()
        .showMessage("Description: Displays specific information about the current game state.\n");
    this.getCtx().showMessage("Available targets:\n");
    this.getCtx().showMessage("  - board         : Shows the current hexagonal board state.\n");
    this.getCtx().showMessage("  - history       : Shows the history of all played turns.\n");
    this.getCtx().showMessage("  - time          : Shows the remaining time for each player.\n");
    this.getCtx().showMessage("  - configuration : Shows the current game settings.\n");
  }
}
