package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

public class CmdHint extends Cmd {

  private Options opts;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdHint(AbstractGameUI uictx) {
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
    return "hint";
  }

  @Override
  public Options getOptions() {
    return this.opts;
  }

  @Override
  public void execute() {}

  @Override
  public void showHelp() {
    this.getCtx().showMessage("Usage: hint\n");
    this.getCtx()
        .showMessage(
            "Description: Asks the AI to suggest the best possible move for the current player.\n");
    this.getCtx()
        .showMessage("Note: This does not play the move for you, it only highlights it.\n");
  }
}
