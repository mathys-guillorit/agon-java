package fr.univ.bordeaux.ui.cli;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.UIPromptParser;
import org.jline.consoleui.prompt.builder.PromptBuilder;
import org.jline.keymap.KeyMap;
import org.jline.reader.Candidate;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/** shell manager */
public class AgonShell extends AbstractGameUI implements GameUserInterface {

  private AtomicBoolean running; // copy reference for usage in commands
  private Terminal terminal;
  private LineReader reader;
  /** get the line entered in the terminal by the user */
  private String line;

  /** Message Header of the cli for the entire App */
  private final String msgHA;

  /** Message Body Information on to of app (2 levels max) */
  private final String msgBI;

  /** warn message */
  private final String msgBW;

  /** Body msg for Errors */
  private final String msgBE;

  /** load the menu once display many times */
  private String mainMenuASCII = "No default Menu set";

  /** represent all options available from the menu */
  private String userPrompt = "> ";

  private AgonRegister<CmdAction> cmds;

  /** allow only default minimal terminal (agon mode) */
  private boolean verbose;

  private AtomicBoolean debug;

  private void init() {
    this.verbose = false;
    this.debug = new AtomicBoolean(false);
    this.cmds = new AgonRegister<>();
    this.running = new AtomicBoolean(true);
    this.userPrompt = this.msgHA + "> ";
  }

  public AgonShell(Terminal term, LineReader reader) {
    super(); // require the work of others
    this.terminal = term;
    // default reader
    this.reader = reader;
    // message headers for the app
    this.msgHA = this.cliLayer();
    this.msgBI = this.cliInfo();
    this.msgBW = this.cliWarn();
    this.msgBE = this.cliError();
    this.init();
  }

  public AgonShell(Terminal term) {
    super(); // require the work of others
    this.terminal = term;
    // default reader
    this.reader =
        LineReaderBuilder.builder().terminal(terminal).completer(this::globalCompleter).build();
    // message headers for the app
    this.msgHA = this.cliLayer();
    this.msgBI = this.cliInfo();
    this.msgBW = this.cliWarn();
    this.msgBE = this.cliError();
    this.init();
    // shortcut for "ctrl+r" show history
    reader.getWidgets().put("show-full-history", this::showFullHistory);
    reader
        .getKeyMaps()
        .get(LineReader.MAIN)
        .bind(new Reference("show-full-history"), KeyMap.ctrl('R'));
  }

  /**
   * load ASCII main menu to shell
   *
   * @param mainMenu file find from the root at main/resources/+<strong>filepath</strong> passed as
   *     argument
   */
  public void loadMainMenu(String mainMenu) {
    this.mainMenuASCII = mainMenu;
  }

  /** leave the shell */
  public void leave() {
    this.running.set(false);
    this.safeCloseTerminal();
  }

  /**
   * loop the program once
   */
  public void loopOnce(){
    if(this.mainMenuASCII.equals("No default Menu set")){
      this.showWarn("Main menu didn't change, you may specify a menu before using associated commands ?");
    }
    if (this.cmds.isEmpty()) {
      // error in commands, no commands registered (dev side problems)
      this.showError(
              String.format(
                      "%s%s",
                      "No Command \"Cmd\" registered, pleas fill \"AgonShell.cmds\"",
                      " first in constructor: \"this.cmds.add(new CmdExample(this))\""));
      this.running.set(false);
    }
    this.readLine();
    this.cliWln("\nBye !\n");
    this.safeCloseTerminal();
  }

  /** run the program to interact with the user */
  public void loop() {
    // this.showMainMenu();
    if(this.mainMenuASCII.equals("No default Menu set")){
      this.showWarn("Main menu didn't change, you may specify a menu before using associated commands ?");
    }
    this.cliWln(this.mainMenuASCII);
    if (this.cmds.isEmpty()) {
      // error in commands, no commands registered (dev side problems)
      this.showError(
          String.format(
              "%s%s",
              "No Command \"Cmd\" registered, pleas fill \"AgonShell.cmds\"",
              " first in constructor: \"this.cmds.add(new CmdExample(this))\""));
      this.running.set(false);
    }
    while (this.running.get()) {
      this.readLine();
    }
    this.cliWln("\nBye !\n");
    this.safeCloseTerminal();
  }

  public String showBE(){
    return this.msgBE;
  }

  /**
   * get a line from terminal
   */
  public void readLine() {
    final String line;
    try {
      line = this.reader.readLine(this.userPrompt).trim();
    } catch (UserInterruptException e) {
      // if user use "ctrl+c"
      this.quitGame();
      return;
    }
    if (line.isEmpty()) return;
    this.reader.getHistory().add(line);
  }

  ///  TODO: remove here when code is duplicate into MatchManager
//  /** apply command action associated with the command name split command logic */
//  private void doCommand() {
//    // Locked Here need work from the others
//    // exit case
//    if (this.cmds == null) {
//      this.showError("no command added into cli");
//      return;
//    }
//    Optional<CmdAction> optional = this.cmds.get(this.userCmdName.toLowerCase());
//    if (optional.isEmpty()) {
//      this.showError("Unknown command: '" + this.userCmdName + "'");
//      this.showHelp();
//      return;
//    }
//    CmdAction cmd = optional.get();
//    cmd.execute();
//  }

  public void safeCloseTerminal() {
    try {
      this.terminal.close();
    } catch (IOException e) {
      this.showError("error closing terminal");
      this.showError(e.getMessage());
    }
  }

  /**
   * display all history
   *
   * @return entry for JLine
   */
  private boolean showFullHistory() {
    History history = reader.getHistory();
    reader.getBuffer().clear();
    reader.callWidget(LineReader.REDRAW_LINE);
    reader.callWidget(LineReader.REDISPLAY);
    this.cliWln("");
    int i = 1;
    for (History.Entry entry : history) {
      System.out.printf("%3d  %s%n", i++, entry.line());// format is : "LineNumber lastLine"
    }
    // redraw prompt
    reader.callWidget(LineReader.REDRAW_LINE);
    reader.callWidget(LineReader.REDISPLAY);
    return true;
  }

  /**
   * completer used by JLine to complete commands and option for all commands. Commands must manage
   * their own options and change here dynamically for autocomplete
   *
   * @param reader filled by Commons Cli
   * @param line filled by Commons Cli
   * @param candidates filled by Commons Cli
   */
  private void globalCompleter(LineReader reader, ParsedLine line, List<Candidate> candidates) {
    List<String> words = line.words();
    // partial word
    Cmd cmd;
    if (words.isEmpty()) {
      for (String cmdKey : this.cmds.getKeys()) {
        if (this.cmds.get(cmdKey).isPresent()) candidates.add(new Candidate(cmdKey));
      }
      return;
    }
    String firstWord = words.getFirst();
    Optional<CmdAction> cmdName = this.cmds.get(firstWord.toLowerCase());
    if (cmdName.isEmpty()) {
      // nothing typed -> all possible commands allowed
      for (String cmdKey : this.cmds.getKeys()) {
        if (cmdKey.startsWith(firstWord)) {
          candidates.add(new Candidate(cmdKey));
        }
      }
      return;
    }
    cmd = (Cmd) this.cmds.get(firstWord).get();
    // delegation for known commands :
    try {
      cmd.getAutoCompleter().complete(reader, line, candidates);
    } catch (NullPointerException e) {
      /// TODO: remove this catch when all commands implements getAutoCompleter() well
      // of not all commands implements autoCompleter
      this.showError(e.getMessage());
    }
  }

  /** fill options for the cli (done twice internally) */
  public void initCmds(AgonRegister<CmdAction> cmds) {
    this.cmds = cmds;
  }

  /**
   * format the output error to see where is the problem (following maven style) (must be used once
   * in the constructor to set attr)
   */
  private String cliError() {
    final String tag = "ERROR";
    AttributedStringBuilder asb = new AttributedStringBuilder();
    asb.append("[");
    asb.style(AttributedStyle.BOLD.foreground(AttributedStyle.RED)).append(tag);
    asb.style(AttributedStyle.DEFAULT).append("]");
    return asb.toAnsi();
  }

  /**
   * create the first bloc to know that we are in agon game to make Reader difference with maven
   * messages (must be used once in the constructor to set attr)
   */
  private String cliLayer() {
    final String tag = "AGON";
    AttributedStringBuilder asb = new AttributedStringBuilder();
    asb.style(AttributedStyle.DEFAULT).append("[");
    asb.style(AttributedStyle.BOLD.foreground(AttributedStyle.MAGENTA)).append(tag);
    asb.style(AttributedStyle.DEFAULT).append("]");
    return asb.toAnsi();
  }

  /**
   * create information ASCII style for more readability (must be used once in the constructor to
   * set attr)
   */
  private String cliInfo() {
    final String tag = "INFO";
    AttributedStringBuilder asb = new AttributedStringBuilder();
    asb.style(AttributedStyle.DEFAULT).append("[");
    asb.style(AttributedStyle.BOLD.foreground(AttributedStyle.BLUE)).append(tag);
    asb.style(AttributedStyle.DEFAULT).append("]");
    return asb.toAnsi();
  }

  private String cliWarn() {
    final String tag = "WARNING";
    AttributedStringBuilder asb = new AttributedStringBuilder();
    asb.style(AttributedStyle.DEFAULT).append("[");
    asb.style(AttributedStyle.BOLD.foreground(AttributedStyle.YELLOW)).append(tag);
    asb.style(AttributedStyle.DEFAULT).append("]");
    return asb.toAnsi();
  }

  /**
   * called everytime system needs to show error from the application system
   *
   * @param msg add Reader message to the error
   */
  @Override
  public void showError(String msg) {
    this.cliW(this.msgHA);
    this.cliW(this.msgBE);
    this.cliWln(" " + msg);
  }

  /**
   * show message information inline in console (with cli formatting)
   *
   * @param msg message to display
   */
  @Override
  public void showInfo(String msg) {
    this.cliW(this.msgHA);
    this.cliW(this.msgBI);
    this.cliWln(" " + msg);
  }



  /**
   * show Reader message in terminal using JLine shortened the code verbose (because used many times
   * and must be changed once for all) (without header)
   *
   * @param msg message to send in terminal
   */
  private void cliWln(String msg) {
    this.terminal.writer().println(msg);
    this.terminal.flush();
  }

  /**
   * show Reader message in terminal using JLine display inline without jump line ("\n") (without
   * header)
   *
   * @param msg message to send in terminal
   */
  private void cliW(String msg) {
    PrintWriter pw = this.terminal.writer();
    pw.print(msg);
    pw.flush();
    this.terminal.flush();
  }

  public void showHelp() {
    this.cliWln(this.mainMenuASCII);
  }

  /**
   * warn user for important decisions or anything more special
   *
   * @param msg message to show in cli
   */
  @Override
  public void showWarn(String msg) {
    this.cliW(this.msgHA);
    this.cliW(this.msgBW);
    this.cliWln(" " + msg);
  }


  /** clit quit the game */
  @Override
  public void quitGame() {
    this.showWarn("Save the game before quitting ? [y/n]");
    this.readLine();
    String filePathSaved;
    try {
      var tmp = new UIPromptParser(this.userPrompt);
      if (!(tmp.parse(this.line) && tmp.getUserCmdName().equalsIgnoreCase("y"))) {
        this.running.set(false);
        return;
      }
      this.saveGame(); // default is locally
    } catch (NullPointerException e) {
      // user use enter keycap instead of writing "n" (or anything different of "y")
      // nothing happens here in this case
    } // may add catch for other specific Exceptions
    this.running.set(false);
  }

  @Override
  public void start() {
    this.loop();
  }


  /**
   * update board display to cli
   *
   * @param boardRepresentation the object used to show characters into terminal
   */
  @Override
  public void updateBoard(ConsoleRenderer boardRepresentation) {
    boardRepresentation.render();
  }

  @Override
  public void showMessage(String message) {
    this.cliW(message);
  }


  ///  ////////////////// GETTERS & SETTERS //////////////////

  public void setVerbose(boolean state) {
    if (this.verbose == state) {
      this.showInfo("verbose already set to: " + state);
      return;
    }
    this.showInfo("last verbose mode : " + !state);
    this.verbose = state;
    this.showInfo("current verbose set to : " + state);
  }


  public boolean getVerbose() {
    return this.verbose;
  }


  /**
   * atomicBoolean, no set required if the object is modified somewhere it automatically updated
   * everywhere the reference is (reference passing)
   *
   * @return {@link AtomicBoolean}
   */
  @Override
  public AtomicBoolean getDebugMode() {
    return this.debug;
  }

  public AgonRegister<CmdAction> getCmds() {
    return this.cmds;
  }

  /**
   * get user prompte (default is "[AGON]> ")
   * @return {@link String}
   */
  @Override
  public String getUserPrompt() {
    return userPrompt;
  }

  public AtomicBoolean getRunning() {
    return running;
  }

  public String getLine(){
    return this.line;
  }

}
