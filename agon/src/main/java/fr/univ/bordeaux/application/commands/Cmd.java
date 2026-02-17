package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.ShellMode;

/**
 * represent the fixed code for all different Commands
 * @warning little changes require a lot refactor here
 */
public abstract class Cmd {

  // may require a GUI delegate here for later (example : IGUIDelegate)
  // mau require a delegate here for the NETwork for later (or
  // juste create one without args in the constructor) (example: INETDelegate)
  private static String prompt = null;

  /**
   * load delegate(s) and information to allow
   * commands interact with the system (for the CLI or GUI)
   */
  public Cmd() {
    
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
