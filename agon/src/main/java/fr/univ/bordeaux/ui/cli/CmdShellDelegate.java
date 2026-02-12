package fr.univ.bordeaux.ui.cli;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class CmdShellDelegate implements ICmdShellDelegate {

  private Terminal terminal;
  private LineReader lineReader;
  private AtomicBoolean isRunning;
  private static ArrayList<String> commandHistory;
  private static String usrPrompt = null;

  public CmdShellDelegate(Terminal terminal, LineReader lineReader, AtomicBoolean isRunning, ArrayList<String> commandHistory) {
    this.terminal = terminal;
    this.lineReader = lineReader;
    this.isRunning = isRunning;
    CmdShellDelegate.commandHistory = commandHistory;
    if (usrPrompt == null) CmdShellDelegate.usrPrompt = ">> "; // default user prompt

  }

  /**
   * change ">> example_user_message"
   * to anything example : "$ hey i changed prompt"
   * @param prompt text before user's message
   */
  public void cliSetPromptHeader(String prompt){
    CmdShellDelegate.usrPrompt = prompt;
  }

  @Override
  public void cliWln(String msg) {
    this.terminal.writer().println(msg);
    this.terminal.flush();
  }

  @Override
  public void cliW(String msg) {
    this.terminal.writer().print(msg);
    this.terminal.flush();
  }

  @Override
  public String cliGetPrompt() {
    return this.lineReader.readLine(CmdShellDelegate.usrPrompt);
  }

  public boolean cliIsRunning() {
    return this.isRunning.get();
  }

  public ArrayList<String> cliCommandHistory() {
    return CmdShellDelegate.commandHistory;
  }

  public void cliSetRunning(boolean running) {
    this.isRunning.set(running);
  }
}
