package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.ui.cli.ICmdShellDelegate;
import org.jline.reader.LineReader;

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
  public String getPrompt() {
    return this.iCmdShellDelegate.getPrompt();
  }
  public void setPromptHeader(String prompt) {
    this.iCmdShellDelegate.setPromptHeader(prompt);
  }
  public LineReader getLineReader() {
    return this.iCmdShellDelegate.getLineReader();
  }
  public boolean isRunning() {
    return this.iCmdShellDelegate.isRunning();
  }
  public ArrayList<String> commandHistory() {
    return this.iCmdShellDelegate.commandHistory();
  }
  public void setRunning(boolean running) {
    this.iCmdShellDelegate.setRunning(running);
  }
}
