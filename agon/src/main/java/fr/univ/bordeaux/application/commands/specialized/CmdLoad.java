package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.ui.AbstractGameUI;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

public class CmdLoad extends Cmd {

  private Options opts;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdLoad(AbstractGameUI uictx) {
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
    return "load";
  }

  @Override
  public Options getOptions() {
    return this.opts;
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

  @Override
  public void showHelp() {
    this.getCtx().showMessage("Usage: load [filename]\n");
    this.getCtx()
        .showMessage("Description: Loads a previously saved game from the specified file.\n");
    this.getCtx().showMessage("Example: load my_save.txt\n");
  }
}
