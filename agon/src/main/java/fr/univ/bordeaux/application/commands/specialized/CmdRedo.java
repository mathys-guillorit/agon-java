package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;

import javax.annotation.Nonnull;

public class CmdRedo extends Cmd {
  private Options opts;


  /**
   * load delegate(s) and information to allow
   * commands interact with the system (for the CLI or GUI)
   *
   * @param uictx
   */
  public CmdRedo(AbstractGameUI uictx) {
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
    return "redo";
  }

  @Override
  public Options getOptions() {
    return this.opts;
  }

  @Override
  public void execute() {

  }

  @Override
  public void showHelp() {

  }
}
