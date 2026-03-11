package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.AbstractGameUI;
import org.apache.commons.cli.Options;

public class CmdUndo extends Cmd {

  private Options opts;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdUndo(AbstractGameUI uictx) {
    super(uictx);
    this.setDesc(
        "Description: Cancels the last played turn. If a number N is provided, it cancels the last N turns.");
    this.setName("undo");
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
}
