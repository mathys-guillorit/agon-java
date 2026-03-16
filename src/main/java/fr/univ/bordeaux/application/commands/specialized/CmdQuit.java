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
    return "Usage: quit (or Ctrl+C)\n"+"Description: Exits the game. You will be prompted to save your current progress before leaving.\n";
  }

  public boolean execute(MatchManager match) {
    System.out.println("j'ai bien appeler quit");
    if (match!=null){
      match.quit();
    }
    this.getCtx().quit();
    System.out.println("j ai demandé au shell de s'arreter");
    return true;
  }

  public CmdAction createNew(String[] args) {
    return new CmdQuit(super.getCtx());
  }

 /* public void getDescription() {
    this.getCtx().showMessage("Usage: quit (or Ctrl+C)\n");
    this.getCtx()
        .showMessage(
            "Description: Exits the game. You will be prompted to save your current progress before leaving.\n");
  }*/
}
