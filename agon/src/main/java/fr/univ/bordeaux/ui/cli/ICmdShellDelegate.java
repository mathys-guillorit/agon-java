package fr.univ.bordeaux.ui.cli;

/// TODO: fix {@link CmdDelegate}


import org.jline.reader.LineReader;

import java.util.ArrayList;

/**
 * Delegate information for later treatment
 * (different treatment for each command)
 */
public interface ICmdShellDelegate {

  /**
   * show a message in terminal with jump line
   * @param msg message to be shown
   */
  public void cliWln(String msg);
  /**
   * show a message in terminal
   * @param msg message to be shown
   */
  public void cliW(String msg);

  /**
   * message written by the user
   * @return text message prompt
   */
  public String cliGetPrompt();

  /**
   * change ">> example_user_message"
   * to anything example : "$ hey i changed prompt"
   * (write/overwrite only)
   * @param prompt text before user's message
   */
  public void cliSetPromptHeader(String prompt);

  public boolean cliIsRunning();
  public ArrayList<String> cliCommandHistory();

  public void cliSetRunning(boolean running);

}
