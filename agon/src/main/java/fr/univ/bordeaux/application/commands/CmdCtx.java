package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.ui.cli.ICmdShellDelegate;
import fr.univ.bordeaux.ui.cli.ShellMode;

import java.util.ArrayList;


/// TODO: ////////// MUST BE REMOVE WHEN IMPLEMENTED //////////////////
// temporary
/**
 * contains method to simplify access self objects (into interface)
 * contains objects to allow commands to use it (for concrete impl)
 */
interface INETDelegate{}

/**
 * contains method to simplify access self objects (into interface)
 * contains objects to allow commands to use it (for concrete impl)
 */
interface IGUIDelegate{}
/// TODO: ////////////////// END WARNING /////////////////////////////


/**
 * <h1>Command Context</h1>
 * context that contains information provided to commands and allow modular code evolution
 * <br>
 * - in composition with {@link Cmd}
 */
public class CmdCtx implements ICmdCtx{

  private ICmdShellDelegate iCmdShellDelegate;
  private INETDelegate inetDelegate;
  private IGUIDelegate guiDelegate;

  public CmdCtx(ICmdShellDelegate iCmdShellDelegate, INETDelegate inetDelegate, IGUIDelegate guiDelegate) {
    this.iCmdShellDelegate = iCmdShellDelegate;
    this.inetDelegate = inetDelegate;
    this.guiDelegate = guiDelegate;
  }

  public ICmdShellDelegate getICmdShellDelegate() {
    return iCmdShellDelegate;
  }
  public INETDelegate getINETDelegate() {
    return inetDelegate;
  }
  public IGUIDelegate getGUIDelegate() {
    return guiDelegate;
  }

  public void cliWln(String msg) {
    this.iCmdShellDelegate.cliWln(msg);
  }
  public void cliW(String msg) {
    this.iCmdShellDelegate.cliW(msg);
  }
  public String cliGetPrompt() {
    return this.iCmdShellDelegate.cliGetPrompt();
  }
  public void cliSetPromptHeader(String prompt) {
    this.iCmdShellDelegate.cliSetPromptHeader(prompt);
  }
  public boolean cliIsRunning() {
    return this.iCmdShellDelegate.cliIsRunning();
  }
  public ArrayList<String> cliCommandHistory() {
    return this.iCmdShellDelegate.cliCommandHistory();
  }
  public void cliSetRunning(boolean running) {
    this.iCmdShellDelegate.cliSetRunning(running);
  }

  @Override
  public ShellMode cliGetShellMode() {
    return this.iCmdShellDelegate.cliGetShellMode();
  }


  @Override
  public void cliGetShellMode(ShellMode mode) {
    this.iCmdShellDelegate.cliGetShellMode(mode);
  }
}
