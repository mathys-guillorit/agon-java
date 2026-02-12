package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.ICmdCtx;

/** Create a new game. Command representation in cli : "new [ARGS]" */
public class CmdCreate extends Cmd {


  /**
   * load delegate(s) and information to allow
   * commands interact with the system (for the CLI or GUI)
   *
   * @param cmdCtx cmdCtx the responsibility to use resources in commands
   */
  public CmdCreate(ICmdCtx cmdCtx) {
    super(cmdCtx);
  }

  @Override
  public void execute() {

  }

  @Override
  public void showHelp() {

  }

}
