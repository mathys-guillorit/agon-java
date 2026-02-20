package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

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
    this.opts = new Options();
  }

  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return null;
  }

  @Override
  public String getName() {
    return "undo";
  }

  @Override
  public Options getOptions() {
    return this.opts;
  }

  @Override
  public void execute() {}

  @Override
  public void showHelp() {
    this.getCtx().showMessage("Usage: undo [N]\n");
    this.getCtx()
        .showMessage(
            "Description: Cancels the last played turn. If a number N is provided, it cancels the last N turns.\n");
  }
}
