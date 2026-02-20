package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

public class CmdSet extends Cmd {

  private Options opts;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdSet(AbstractGameUI uictx) {
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
  public void execute() {}

  @Override
  public void showHelp() {
    this.getCtx().showMessage("Usage: set PARAM=VALUE\n");
    this.getCtx()
        .showMessage(
            "Description: Changes the current game configuration dynamically during the session.\n");
  }
}
