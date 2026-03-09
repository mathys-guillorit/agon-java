package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.ui.AbstractGameUI;
import org.apache.commons.cli.Options;

public class CmdRedo extends Cmd {
  private Options opts;

  /**
   * load delegate(s) and information to allow commands interact with the system (for the CLI or
   * GUI)
   *
   * @param uictx
   */
  public CmdRedo(AbstractGameUI uictx) {
    super(uictx);
    this.opts = this.getOptions();
    this.setDesc(
        "Description: Replays the last canceled turn. If a number N is provided, it replays the last N canceled turns.\n");
    this.setName("redo");
  }

  @Override
  public void execute() {}
}
