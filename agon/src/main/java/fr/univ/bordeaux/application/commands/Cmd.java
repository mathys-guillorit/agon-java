package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.ICmdShellDelegate;
import fr.univ.bordeaux.ui.cli.ShellMode;

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
    if (prompt == null) Cmd.prompt = cmdCtx.cliGetPrompt();
  }

  /**
   * allow to change prompt shown to the user
   * (overwrite only)
   * @param prompt text t
   */
  public void setUserPrompt(String prompt) {
    this.CmdCtx.cliSetPromptHeader(prompt);
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

  public void handleConfirmation(String input){
    if (input.equalsIgnoreCase("y")) {
      cliWln("saving...");
      cliWln("saved !");
    }
    this.setRunning(false);
  }

  public void setRunning(boolean running){
    this.CmdCtx.cliSetRunning(running);
  }

  /**
   * ask to {@link AgonShell} if the command require the user input
   * @implNote change const false to a variable that is toggleable
   * @return boolean false (by default)
   */
  public boolean requiresInput(){
    return false;
  }

  /**
   * get inputs from {@link AgonShell} (single entry point for
   * user inputs)
   * @implNote default empty must be change in sub commands
   * @param input text from the user in cli
   */
  public void handleInput(String input){
    // nothing
  }

  /**
   * commands to be executed for GUI later
   */
  public void guiExecute(){

  }

  /**
   * commands to be executed for GUI later
   */
  public void guiShowHelp(){

  }

  public void setShellMode(ShellMode mode){
    this.CmdCtx.cliGetShellMode(mode);
  }

  public ShellMode getShellMode(){
    return this.CmdCtx.cliGetShellMode();
  }

}
