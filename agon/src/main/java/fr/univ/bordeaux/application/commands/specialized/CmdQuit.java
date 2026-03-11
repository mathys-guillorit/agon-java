package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.Options;

public class CmdQuit extends Cmd {
  private Options opts;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx display context
   */
  public CmdQuit(GameUserInterface uictx) {
    super(uictx);
    this.opts = new Options();
  }

  @Override
  public String getName() {
    return "quit";
  }

  @Override
  public Options getOptions() {
    return this.opts;
  }

  @Override
  public String getDescription() {
    return "";
  }

  /** to be run with higher levels in code */
  public void execute() {
    this.getCtx().quitGame();
  }

  public void execute(MatchManager match) {

  }

  public CmdAction createNew(String[] args) {
    return null;
  }

  public void showHelp() {
    this.getCtx().showMessage("Usage: quit (or Ctrl+C)\n");
    this.getCtx()
        .showMessage(
            "Description: Exits the game. You will be prompted to save your current progress before leaving.\n");
  }
}
