package fr.univ.bordeaux.ui.cli;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.util.ArrayList;

public class CmdShellDelegate implements ICmdShellDelegate {

  private Terminal terminal;
  private LineReader lineReader;
  private boolean isRunning;
  private static ArrayList<String> commandHistory;
  private static String usrPrompt = null;

  public CmdShellDelegate(Terminal terminal, LineReader lineReader, boolean isRunning, ArrayList<String> commandHistory) {
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
  public void setPromptHeader(String prompt){
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
  public String getPrompt() {
    return this.lineReader.readLine(CmdShellDelegate.usrPrompt);
  }

  public LineReader getLineReader() {
    return this.lineReader;
  }

  public boolean isRunning() {
    return this.isRunning;
  }

  public ArrayList<String> commandHistory() {
    return CmdShellDelegate.commandHistory;
  }

  public void setRunning(boolean running) {
    this.isRunning = running;
  }
}
