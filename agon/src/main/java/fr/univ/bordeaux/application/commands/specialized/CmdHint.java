package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.ICmdCtx;

public class CmdHint extends Cmd {


  /**
   * load delegate(s) and information to allow
   * commands interact with the system (for the CLI or GUI)
   *
   * @param cmdCtx cmdCtx the responsibility to use resources in commands
   */
  public CmdHint(ICmdCtx cmdCtx) {
    super(cmdCtx);
  }

  @Override
  public void execute() {
    this.cliWln("--- Hint Request ---");
    String bestMove = callDomainLayerForHint();
    this.cliWln("AI suggests: " + bestMove);
  }

  /**
   * Simulates communication with the Domain layer (AgonCore / Engine).
   * To be replaced later by: return this.CmdCtx.getGameEngine().computeHint();
   */
  private String callDomainLayerForHint() {
      // TODO: Connect this to the actual game engine once it is merged
      return "";
  }

  @Override
  public void showHelp() {

  }

}
