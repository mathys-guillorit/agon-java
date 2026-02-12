package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.ICmdCtx;

public class CmdUndo extends Cmd {
  /**
   * load delegate(s) and information to allow
   * commands interact with the system (for the CLI or GUI)
   *
   * @param cmdCtx cmdCtx the responsibility to use resources in commands
   */
  public CmdUndo(ICmdCtx cmdCtx) {
    super(cmdCtx);
  }

  @Override
  public void shellExecute() {

  }

  @Override
  public void shellShowHelp() {

  }
}
