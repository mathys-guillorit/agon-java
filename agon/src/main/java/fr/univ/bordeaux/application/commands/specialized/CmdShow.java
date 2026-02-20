package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

public class CmdShow extends Cmd {

  private Options opts;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdShow(AbstractGameUI uictx) {
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
    return "show";
  }

  @Override
  public Options getOptions() {
    return this.opts;
  }

  @Override
  public void execute() {}

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
