package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.ICmdCtx;
import fr.univ.bordeaux.ui.cli.ICmdShellDelegate;

public class CmdHelp extends Cmd {
  /**
   * load delegate(s) and information to allow
   * commands interact with the system (for the CLI or GUI)
   *
   * @param cmdCtx cmdCtx the responsibility to use resources in commands
   */
  public CmdHelp(ICmdCtx cmdCtx) {
    super(cmdCtx);
  }

  @Override
  public void execute() {
    this.cliWln("you written \"help\" command !");
  }

  @Override
  public void showHelp() {

  }
}
