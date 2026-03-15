package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.Options;

public final class CmdSave extends Cmd {

  private Options opts;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdSave(GameUserInterface uictx) {
    super(uictx);
    this.opts = this.getOptions();
    this.setDesc("Description: Saves the current game state and history to the specified file.");
    this.setName("save");
  }

  @Override
  public String getDescription() {
    return "Save";
  }


  public boolean execute(MatchManager match) {
    return true;
  }

  public CmdAction createNew(String[] args) {
    return new CmdSave(super.getCtx());
  }
}
