package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.Options;

public final class CmdPause extends Cmd {

  private Options opts;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdPause(GameUserInterface uictx) {
    super(uictx);
    this.opts = this.getOptions();
    this.setName("pause");
    this.setDesc(
        "Description: Pauses the passing time. This command is only available when playing in Blitz mode.");
  }

  @Override
  public String getDescription() {
    return "Pause\n";
  }

  public boolean execute(MatchManager match) {
    return true;
  }

  public CmdAction createNew(String[] args) {
    return null;
  }
}
