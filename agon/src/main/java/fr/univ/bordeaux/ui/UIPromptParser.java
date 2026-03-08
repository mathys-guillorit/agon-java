package fr.univ.bordeaux.ui;

import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;

import java.util.List;

/**
 * parse text into commands and options
 * example : "set debug=true"
 * cmdName : "set"
 * options: "debug" and "true"
 */
public class UIPromptParser {

  private String userPrompt;
  /** where user have written the message in cli*/
  private String userCmdName = "";
  private String[] userOptions;
  private Parser parser= new DefaultParser();

  public UIPromptParser(String userPrompt) {
    this.userPrompt = userPrompt;
    this.userOptions = new String[0]; // default empty
    this.userCmdName = "";
  }


  /**
   * @param line {@link String} input from terminal
   * @return boolean true if succeeded else false when an error occurred or input is empty.
   */
  public boolean parse(final String line){
    int startCursorIdx = 0;
    // in restricted mode we know option in advance
    // but not in game mode
    if (line.isEmpty()) {
      this.userCmdName = "";
      this.userOptions = new String[0];
      return false;
    }
    final ParsedLine parsed;
    try {
      parsed = parser.parse(line,0);
    } catch (Exception e) {
      return false;
    }
    final List<String> words = parsed.words();
    if (words.isEmpty()) return false;
    this.userCmdName = words.getFirst().toLowerCase();
    startCursorIdx++;
    this.userOptions = words.subList(startCursorIdx, words.size()).toArray(String[]::new);
    return true;
  }

  /**
   *
   * @return
   */
  public String[] getTxtOptions(){
    return null;
  }

  public String getUserPrompt() {
    return userPrompt;
  }

  /**
   * get the name of the command entered by the user
   * @return {@link String}
   */
  public String getUserCmdName() {
    return userCmdName;
  }

  /**
   * get all options sent by the user
   * @return {@link String}[]
   */
  public String[] getUserOptions() {
    return userOptions;
  }

  public Parser getParser() {
    return parser;
  }
}
