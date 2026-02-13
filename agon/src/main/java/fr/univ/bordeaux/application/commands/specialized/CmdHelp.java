package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.ICmdCtx;

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
  public void shellExecute() {
    this.cliWln("--- Available Commands ---");
    this.cliWln(" quit    : Quit the game");
    this.cliWln(" help    : Show this message");
    this.cliWln(" hint    : Ask for a hint");
    this.cliWln(" version : Show the version");
    this.cliWln(" new     : New game (if available)");
  }

  @Override
  public void shellShowHelp() {

  }
}
