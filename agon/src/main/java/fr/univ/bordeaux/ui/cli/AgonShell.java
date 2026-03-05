package fr.univ.bordeaux.ui.cli;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.ui.AbstractGameUI;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
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
  private String userCmdName;
  private String[] userOptions; // alias "args"
  private Terminal terminal;
  private LineReader reader;
  private PromptBuilder promptBuilder;

  /** Message Header of the cli for the entire App */
  private final String msgHA;

  /** Message Body Information on to of app (2 levels max) */
  private final String msgBI;

  /** warn message */
  private final String msgBW;

  /** Body msg for Errors */
  private final String msgBE;

  /** load the menu once display many times */
  private String mainMenuASCII;

  /** represent all options available from the menu */
  private String userPrompt = "> ";

  private AgonRegister<CmdAction> cmds;

  /** allow only default minimal terminal (agon mode) */
  private boolean verbose;

  private AtomicBoolean debug;

  private boolean isReader;

  private void init() {
    this.verbose = false;
    this.debug = new AtomicBoolean(false);
    this.cmds = new AgonRegister<>();
    this.running = new AtomicBoolean(true);
    this.setGameEngine(this);
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
    this.mainMenuASCII = "No default Menu set";
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
    this.mainMenuASCII = "No default Menu set";
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
  }

  /** run the program to interact with the user */
  public void loop() {
    // this.showMainMenu();
    this.cliWln(this.mainMenuASCII);
    if (this.cmds.isEmpty()) {
      // error in commands, no commands registered (dev side problems)
      this.cliErr(
          String.format(
              "%s%s",
              "No Command \"Cmd\" registered, pleas fill \"AgonShell.cmds\"",
              " first in constructor: \"this.cmds.add(new CmdExample(this))\""));
      this.running.set(false);
    }
    while (this.running.get()) {
      if (this.readLine()) continue;
      this.doCommand();
    }
    this.cliWln("\nBye !\n");
    this.safeCloseTerminal();
  }

  /**
   * get and format user input from cli
   *
   * @return continues or not (skip doing the command) or inform that the command input is empty
   */
  public boolean readLine() {
    String line;
    List<String> words;
    boolean batchMode = true;
    int startCursorIdx = 0;
    // in restricted mode we know option in advance
    // but not in game mode
    try {
      line = this.reader.readLine(this.userPrompt).trim();
    } catch (UserInterruptException e) {
      // if user use "ctrl+c"
      this.quitGame();
      return true;
    }
    if (line.isEmpty()) return true;
    this.reader.getHistory().add(line);
    // tokenized by JLine into words
    ParsedLine parsed = reader.getParser().parse(line, startCursorIdx);
    words = parsed.words();
    this.userCmdName = words.getFirst(); // == word.get(0);
    startCursorIdx++;
    this.userOptions = words.subList(startCursorIdx, words.size()).toArray(new String[0]);
    return false;
  }

  /** apply command action associated with the command name split command logic */
  private void doCommand() {
    // Locked Here need work from the others
    // exit case
    if (this.cmds == null) {
      this.cliErr("no command added into cli");
      return;
    }
    Optional<CmdAction> optional = this.cmds.get(this.userCmdName.toLowerCase());
    if (optional.isEmpty()) {
      this.cliErr("Unknown command: '" + this.userCmdName + "'");
      this.showHelp();
      return;
    }
    CmdAction cmd = optional.get();
    cmd.execute();
  }

  private void safeCloseTerminal() {
    try {
      this.terminal.close();
    } catch (IOException e) {
      this.cliErr("error closing terminal");
      this.cliErr(e.getMessage());
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
      System.out.printf("%3d  %s%n", i++, entry.line());
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
      this.cliErr(e.getMessage());
    }
  }

  /** fill options for the cli (done twice internally) */
  public void initCmds(AgonRegister<CmdAction> cmds) {
    this.cmds = cmds;
  }

  public void test() {
    var a = new ConsoleRenderer(null);
    a.render();
  }

  /**
   * format the output error to see where is the problem (following maven style) (must be used once
   * in the constructor to set attr)
   */
  private String cliError() {
    final String tag = "ERROR";
    AttributedStringBuilder asb = new AttributedStringBuilder();
    asb.append(this.msgHA).append("[");
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
  private void cliErr(String msg) {
    this.cliW(this.msgHA);
    this.cliW(this.msgBE);
    this.cliWln(" " + msg);
  }

  /**
   * show message information inline in console (with cli formatting)
   *
   * @param msg message to display
   */
  private void cliIWln(String msg) {
    this.cliW(this.msgHA);
    this.cliW(this.msgBI);
    this.cliWln(" " + msg);
  }

  /**
   * warn user for important decisions or anything more special
   *
   * @param msg message to show in cli
   */
  private void cliIWWln(String msg) {
    this.cliW(this.msgHA);
    this.cliW(this.msgBW);
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
   * when in game mode undo Reader turn
   *
   * @return ???
   */
  @Override
  public void undo() {
    //
  }

  /** when in game mode redo Reader turn if history is not empty */
  public void redo() {}

  @Override
  public void saveGame(String f) {
    ///  TODO: saving file
    System.out.println("save : " + f);
  }

  /** clit quit the game */
  @Override
  public void quitGame() {
    this.cliIWWln("Save the game before quitting ? [y/n]");
    this.readLine();
    try {
      if (this.userCmdName.equalsIgnoreCase("y")) {
        ///  TODO: saving here the real file name
        cliIWln("saving...");
        // code here
        cliIWln("saved !");
      }
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
   * action command n°1 role : initialize Reader new game
   *
   * @param args useless arguments defined in the main interface
   */
  @Override
  public void startNewGame(String[] args) {
    /// TODO: start Reader new game here
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

  @Override
  public void showError(String error) {
    this.cliErr(error);
  }

  ///  ////////////////// GETTERS & SETTERS //////////////////

  public void setVerbose(boolean state) {
    if (this.verbose == state) {
      this.cliIWln("verbose already set to: " + state);
      return;
    }
    this.cliIWln("last verbose mode : " + !state);
    this.verbose = state;
    this.cliIWln("current verbose set to : " + state);
  }

  /**
   * get more help about Reader command (reduce if verbose is disabled)
   *
   * @param cmd command to get more help on
   */
  public void getHelp(CmdAction cmd) {
    cmd.showHelp();
  }

  public boolean getVerbose() {
    return this.verbose;
  }

  /**
   * get user options
   *
   * @return user options with arguments
   */
  public String[] getTxtOptions() {
    return this.userOptions;
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

  public String getUserCmdName() {
    return userCmdName;
  }
}
