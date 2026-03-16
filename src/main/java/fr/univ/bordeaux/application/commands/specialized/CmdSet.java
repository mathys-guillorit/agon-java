package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.GameUserInterface;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

public final class CmdSet extends Cmd {

  private Options opts;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdSet(GameUserInterface uictx) {
    super(uictx);
    this.opts = this.getOptions();
  }

  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return null;
  }

  @Override
  public String getName() {
    return "set";
  }

  @Override
  public Options getOptions() {
    return this.opts;
  }

  @Override
  public String getDescription() {
    return "Usage: set PARAM=VALUE\n"+"Description: Changes the current game configuration dynamically during the session.\n";
  }

  public boolean execute(MatchManager match) {
    return true;
  }

  public CmdAction createNew(String[] args) {
    return new CmdSave(super.getCtx());
  }

  /*public void getDescription() {
    this.getCtx().showMessage("Usage: set PARAM=VALUE\n");
    this.getCtx()
        .showMessage(
            "Description: Changes the current game configuration dynamically during the session.\n");
  }*/
}
