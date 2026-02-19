package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.cli.AgonShell;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

import javax.annotation.Nonnull;

public class CmdQuit extends Cmd {
  private Options opts;

  /**
   * load delegate(s) and information to allow
   * commands interact with the system (for the CLI or GUI)
   *
   * @param uictx display context
   */
  public CmdQuit(AbstractGameUI uictx) {
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
    return "quit";
  }

  @Override
  public Options getOptions() {
    return this.opts;
  }

  /**
   * to be run with higher levels in code
   */
  public void execute() {
    this.getCtx().quitGame();
  }

  public void showHelp() {

  }


}
