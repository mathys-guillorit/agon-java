package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.ui.cli.ICmdShellDelegate;

/**
 * represent the fixed code for all different Commands
 * @warning little changes require a lot refactor here
 */
public abstract class Cmd implements ICmd {

  private final ICmdCtx CmdCtx;
  // may require a GUI delegate here for later (example : IGUIDelegate)
  // mau require a delegate here for the NETwork for later (or
  // juste create one without args in the constructor) (example: INETDelegate)
  private static String prompt = null;

  /**
   * load delegate(s) and information to allow
   * commands interact with the system (for the CLI or GUI)
   * @param cmdCtx cmdCtx the responsibility to use resources in commands
   */
  public Cmd(ICmdCtx cmdCtx) {
    this.CmdCtx = cmdCtx;
    if (prompt == null) Cmd.prompt = cmdCtx.getPrompt();
  }

  /**
   * allow to change prompt shown to the user
   * (overwrite only)
   * @param prompt text t
   */
  public void setUserPrompt(String prompt) {
    this.CmdCtx.setPromptHeader(prompt);
  }

  /**
   * get the entire delegate that contains information
   * to proceed on with commands
   * @return shell delegate
   */
  public ICmdShellDelegate getICmdDelegate() {
    return CmdCtx.getICmdShellDelegate();
  }
  /** draw text with jump line at the end into terminal
   * stance for "CLI Write Line"
   */
  public void cliWln(String msg){
    this.CmdCtx.cliWln(msg);
  }
  /** draw text without jump line at the end into terminal
   * stance for "CLI Write" */
  public void cliW(String msg){
    this.CmdCtx.cliW(msg);
  }

  /**
   * get user message from CLI
   * shortened the access (redundant for each command)
   * @return the message written by the user
   */
  public String getCLIPrompt(){
    return this.CmdCtx.getLineReader().readLine(Cmd.prompt);
  }

}
