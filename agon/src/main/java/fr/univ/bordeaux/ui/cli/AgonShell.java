package fr.univ.bordeaux.ui.cli;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.*;
import fr.univ.bordeaux.ui.AbstractGameUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jline.consoleui.prompt.ConsolePrompt;
import org.jline.consoleui.prompt.PromptResultItemIF;
import org.jline.consoleui.prompt.builder.PromptBuilder;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * shell manager
 */
public class AgonShell extends AbstractGameUI implements GameUserInterface{

  private AtomicBoolean running; // copy reference for usage in commands
  private String userCmdName;
  private String[] userOptions; // alias "args"
  private Terminal terminal;
  private LineReader reader;
  private PromptBuilder promptBuilder;

  /** Message Header of the cli for the entire App */
  private final String msgHA;
  /** load the menu once display many times */
  private final String mainMenuASCII;
  private ArrayList<String> cmdHistory;
  /** represent all options available from the menu */
  private Options options = null;
  private String userPrompt = "#> ";
  private Cmd activeCmd = null;
  private AgonRegister<CmdAction> cmds;
  private int undoCounts;
  /** allow only default minimal terminal (agon mode) */
  private ShellMode mode;


  /** ASCII engine renderer Used to communicate through the terminal with the user*/
  public AgonShell() {
    super(); // require the work of others
    this.undoCounts = 0;
    this.cmds = new AgonRegister<>();
    cmds.register("agon", new CmdAgon(this));
    this.running = new AtomicBoolean(true);
    this.msgHA = this.cliLayer();
    this.mainMenuASCII = this.loadMainMenu(
      "cmdsInformations/agonRestrictedMenu.txt"
    );
    this.cmdHistory = new ArrayList<>();
    this.mode = ShellMode.RESTRICTED;
    try {
      this.terminal = TerminalBuilder.builder().system(true).build(); // IOException
      this.reader = LineReaderBuilder.builder().terminal(terminal).build();
    } catch (IOException e) {
      this.cliErr("terminal initialization failed");
      this.cliErr(e.getMessage());
    }
    this.initCmdsAndOptions();
    this.setGameEngine(this);
    final String upJLineEvent = "up-custom";
    final String downJLineEvent = "down-custom";
    this.reader.getWidgets().put(upJLineEvent, this::cmdUndo);
    this.reader.getWidgets().put(downJLineEvent, this::cmdRedo);
  }


  /** run the program to interact with the user */
  public void loop() {
    // this.showMainMenu();
    this.cliWln(this.mainMenuASCII);
    String line;
    List<String> words;
    boolean batchMode = true;
    int startCursorIdx = 0;
    if (this.cmds.isEmpty()) {
      this.cliErr(String.format("%s%s",
        "No Command \"Cmd\" registered, pleas fill \"CmdRegister\"",
        " first with \"CmdRegister.getInstance()\""
      ));
      this.running.set(false);
    }
    while (this.running.get()) {
      if(this.readLine()) continue;
      this.doCommand();
    }
    this.cliWln("\nBye !\n");
    this.safeCloseTerminal();
  }

  /**
   * get and format user input from cli
   * @return continues or not (skip doing the command)
   * or inform that the command input is empty
   */
  private boolean readLine(){
    String line;
    List<String> words;
    boolean batchMode = true;
    int startCursorIdx = 0;
    line = this.reader.readLine(this.userPrompt).trim();
    this.cmdHistory.add(line);
    this.cliWln("message: '"+line+"'");
    if (line.isEmpty()) return true;
    // tokenized by JLine into words
    ParsedLine parsed = reader.getParser().parse(line, startCursorIdx);
    words = parsed.words();
    this.userCmdName = words.getFirst(); // == word.get(0);
    startCursorIdx++;
    this.userOptions = words.subList(startCursorIdx, words.size()).toArray(new String[0]);
    return false;
  }

  public String[] getTxtOptions(){
    return this.userOptions;
  }

  /**
   * apply command action associated with the command name
   * split command logic
   */
  private void doCommand() {
    // Locked Here need work from the others
    // exit case
    Optional<CmdAction> optional = this.cmds.get(this.userCmdName.toLowerCase());
    if (this.mode == ShellMode.RESTRICTED) {
      CmdAction cmdName = optional.get();
//      if (userCmdName.equalsIgnoreCase("quit")) this.running.set(false);
      if (!userCmdName.equalsIgnoreCase("agon")) {
        this.cliErr("Unknown command ("+this.userCmdName+")");
        this.running.set(false);
      }
      String option =  this.userOptions[0];
      if (option.equalsIgnoreCase("help")
       || option.equalsIgnoreCase("h")) {
        this.cliWln(this.mainMenuASCII);
      }
      if (option.equalsIgnoreCase("quit")
       || option.equalsIgnoreCase("q")) {
        this.cliWln(this.mainMenuASCII);
      }
      // 4 possible options
      return;
    }
    if(optional.isEmpty()) {
      this.cliErr("Unknown command: '"+this.userCmdName+"'");
      this.showHelp();
      return;
    }
    Cmd cmd = (Cmd)optional.get();
    this.cliWln(Arrays.toString(this.userOptions)); // something ab > opt is ab
    cmd.execute();
    if (cmd.requiresInput()) this.activeCmd = cmd;

  }

  private void safeCloseTerminal() {
    try {
      this.terminal.close();
    } catch (IOException e) {
      this.cliErr("error closing terminal");
      this.cliErr(e.getMessage());
    }
  }

  private void addRestrictedOptions(){
    // Commons Cli add already "--" no need to put them
    Option help = Option.builder("h")
            .longOpt("help")
            .argName("cmd")
            .desc("Show help (optionally for a specific command")
            .get();
    Option version = Option.builder("V")
            .longOpt("version")
            .desc("show program version")
            .get();
    Option verbose = Option.builder("v")
            .longOpt("verbose")
            .desc("add more text information")
            .get();
    Option debug = Option.builder("d")
            .longOpt("debug")
            .desc("show debug messages")
            .get();
    this.options.addOption(version);
    this.options.addOption(verbose);
    this.options.addOption(debug);
    this.options.addOption(help);
  }

  private void addGameOptions(){
    Option create = Option.builder("new")
            .optionalArg(false)
            .hasArg(true)
            .argName("ARGS")
            .desc("run a new game")
            .get();
    Option history = Option.builder("history")
            .desc("show game turns history")
            .get();
    Option load = Option.builder("load")
            .optionalArg(false)
            .hasArg(true)
            .argName("FILE")
            .desc("load a game from a file")
            .get();
    Option quit = Option.builder("quit")
            .desc("quit the game")
            .get();
    // if there is pause there is "resume" ?
    Option pause = Option.builder("pause")
            .desc("stop the passing time in blitz mode")
            .get();
    Option hint = Option.builder("hint")
            .desc("show an advice to play a turn")
            .get();
    Option undo = Option.builder("undo")
            .optionalArg(true)
            .hasArg(true)
            .argName("N")
            .desc("cancel the last turn (or the N lasts)")
            .get();
    Option redo = Option.builder("undo")
            .optionalArg(true)
            .hasArg(true)
            .argName("N")
            .desc("replay the last canceled turn (or the N lasts)")
            .get();
    Option showBoard = Option.builder("board")
            .desc("show the current board state")
            .get();
    Option showtime = Option.builder("time")
            .desc("show time left for each players")
            .get();
    Option showConf = Option.builder("configuration")
            .desc("show game configuration")
            .get();
    // set PARAM=VALUE : change current configuration
    Option setParam = Option.builder("set")
            .hasArg()
            .argName("key=value")
            .desc("change current game configuration, example : \"set debug=true\"")
            .get();
    this.options.addOption(create);
    this.options.addOption(history);
    this.options.addOption(load);
    this.options.addOption(quit);
    this.options.addOption(pause);
    this.options.addOption(hint);
    this.options.addOption(undo);
    this.options.addOption(redo);
    this.options.addOption(showBoard);
    this.options.addOption(showtime);
    this.options.addOption(showConf);
    this.options.addOption(setParam);
  }

  private void addGameCmds(){
    if (this.cmds.get("agon").isEmpty()) return;
    cmds.remove("agon");
    cmds.register("quit", new CmdQuit(this));
    cmds.register("new", new CmdCreate(this));
    cmds.register("load", new CmdLoad(this));
    cmds.register("help", new CmdHelp(this));
    cmds.register("save", new CmdSave(this));
    cmds.register("pause", new CmdPause(this));
    cmds.register("hint", new CmdHint(this));
    cmds.register("agon", new CmdAgon(this));
  }

  /** fill options for the cli (done twice internally) */
  private void initCmdsAndOptions() {
    if (this.cmds.isEmpty()) return;
    this.options = new Options();
    if (this.mode == ShellMode.RESTRICTED) {
      this.addRestrictedOptions();
      return;
    }
    this.cmds.reset();
    this.loadMainMenu(
            "cmdsInformations/agonShellMenu.txt"
    );
    this.addGameOptions();
    this.addGameCmds();
  }

  public void test() {
    var a = new ConsoleRenderer(null);
    a.render();
  }

  /** test to display a list in terminal */
  public void advancedTerminal() {
    ConsolePrompt prompt = new ConsolePrompt(terminal);
    this.promptBuilder = prompt.getPromptBuilder();

    // Create a list prompt for single selection
    this.promptBuilder
        .createListPrompt()
        .name("color")
        .message("Choose your favorite color")
        .newItem()
        .text("Red")
        .add()
        .newItem("green")
        .text("Green")
        .add()
        .newItem("blue")
        .text("Blue")
        .add()
        .newItem("yellow")
        .text("Yellow")
        .add()
        .pageSize(3) // Show 3 items at a time
        .addPrompt();
    try {
      Map<String, PromptResultItemIF> result = prompt.prompt(this.promptBuilder.build());
      System.out.println("Selected color: " + result.get("color").getResult());
      Thread.sleep(1000);
    } catch (Exception e) {
      this.cliErr("exception type: " + e.getClass());
      this.cliErr(e.getMessage());
    }
  }



  /** load menu character in a variable once
   * from a file in resource directory */
  @Nonnull
  private String loadMainMenu(String shellMenuTxtFile) {
    // DP Command here
    final String defaultMenu = "Menu not available";
    final String resourcePath = "/" + shellMenuTxtFile;
    InputStream stream = getClass().getResourceAsStream(resourcePath);
    if (stream == null) return defaultMenu;
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      var menu = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        menu.append(line).append("\n");
      }
      return menu.toString();
    } catch (IOException | NullPointerException e) {
      // if the file doesn't exist
      this.cliErr("when reading menu : " + e.getMessage());
    }
    return defaultMenu;
  }

  /**
   * format the output error to see where is the problem (following maven style)
   *
   * @param msg add a message to the error
   */
  private void cliErr(String msg) {
    final String tag = "ERROR";
    AttributedStringBuilder asb = new AttributedStringBuilder();
    asb.append(this.msgHA).append("[");
    asb.style(AttributedStyle.BOLD.foreground(AttributedStyle.RED)).append(tag);
    asb.style(AttributedStyle.DEFAULT).append("] ").append(msg);
    this.cliWln(asb.toAnsi());
  }

  /**
   * create the first bloc to know that we are in agon game to make a difference with maven messages
   * (must be used once in the constructor to set attr)
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
   * show a message in terminal using JLine shortened the code verbose (because used many times and
   * must be changed once for all)
   *
   * @param msg message to send in terminal
   */
  private void cliWln(String msg) {
    this.terminal.writer().println(msg);
    terminal.flush();
  }

  /**
   * show a message in terminal using JLine display inline without jump line ("\n")
   *
   * @param msg message to send in terminal
   */
  private void cliW(String msg) {
    this.terminal.writer().print(msg);
    terminal.flush();
  }

  public void showHelp() {
    this.cliWln(this.mainMenuASCII);
  }


  /**
   * display last command into terminal
   */
  private boolean cmdUndo(){
    if(this.undoCounts < this.cmdHistory.size()) this.undoCounts++;
    this.reader.getBuffer().clear();
    final String lastTypedLine = this.cmdHistory.get(
      this.cmdHistory.size()-this.undoCounts
    );
    this.reader.getBuffer().write(this.userPrompt+lastTypedLine);
    this.reader.callWidget(LineReader.REDRAW_LINE);
    this.reader.callWidget(LineReader.REDISPLAY);
    return true;
  }

  /**
   * display last undo command into terminal
   */
  private boolean cmdRedo(){
    if (this.undoCounts > 0) this.undoCounts--;


    // reset command at the end of redo/undo in the method
    // that have called one of those two methods
    // when do enter -> reset undo counts
    // this.undoCounts=0;
    return true;
  }

  /**
   * when in game mode undo a turn
   *
   * @return
   */
  public boolean undo(){

    return false;
  }

  /**
   * when in game mode redo a turn if history is not empty
   */
  public void redo(){

  }

  @Override
  public void saveGame(String f) {
    ///  TODO: saving file
    System.out.println("save : "+f);
  }

  /**
   * clit quit the game
   */
  @Override
  public void quitGame() { // OK
    this.cliWln(
    "Save the game before quitting ? [y/n]"
    );
    this.readLine();
    if (this.userCmdName.equalsIgnoreCase("y")) {
      ///  TODO: saving here the real file name
      cliWln("saving...");
      // code here
      cliWln("saved !");
    }
    this.running.set(false);
  }

  @Override
  public void start() {
    this.loop();
  }

  /**
   * action command n°1
   * role : initialize a new game
   * @param args useless arguments defined in the main interface
   */
  @Override
  public void startNewGame(String[] args){
    if (!(this.mode == ShellMode.GAME)){
      this.mode = ShellMode.GAME;
      this.initCmdsAndOptions();
    }
    /// TODO: start a new game here
  }

  /**
   * update board display to cli
   * @param boardRepresentation the object used to show characters into terminal
   */
  @Override
  public void updateBoard(ConsoleRenderer boardRepresentation){
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

  @Override
  public boolean getUserConfirmation(String question) {
    return false;
  }

  public Options getOptions(){
    return this.options;
  }

}
