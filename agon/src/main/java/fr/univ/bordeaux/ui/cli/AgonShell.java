package fr.univ.bordeaux.ui.cli;

import fr.univ.bordeaux.agonCore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdRegister;
import fr.univ.bordeaux.application.commands.ICmd;
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

public class AgonShell extends AbstractGameUI {

  private AtomicBoolean running; // copy reference for usage in commands
  private String userCmdName;
  private String[] userOptions; // alias "args"
  private ICmd command;
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
  /** allow only default minimal terminal (agon mode) */
  private ShellMode mode;

  /** ASCII engine renderer Used to communicate through the terminal with the user*/
  public AgonShell() {
    super(); // require the work of others
    this.running = new AtomicBoolean(true);
    this.msgHA = this.cliLayer();
    this.mainMenuASCII = this.loadMainMenu();
    this.cmdHistory = new ArrayList<>();
    this.mode = ShellMode.RESTRICTED;
    try {
      this.terminal = TerminalBuilder.builder().system(true).build(); // IOException
      this.reader = LineReaderBuilder.builder().terminal(terminal).build();
    } catch (IOException e) {
      this.cliErr("terminal initialization failed");
      this.cliErr(e.getMessage());
    }
    this.initOptions();
  }


  /** run the program to interact with the user */
  public void loop() {
    // this.showMainMenu();
    this.cliWln(this.mainMenuASCII);
    String line;
    List<String> words;
    boolean batchMode = true;
    int startCursorIdx = 0;
    if (CmdRegister.getInstance().isEmpty()) {
      this.cliErr(String.format("%s%s",
        "No Command \"Cmd\" registered, pleas fill \"CmdRegister\"",
        " first with \"CmdRegister.getInstance()\""
      ));
      return;
    }
    while (this.running.get()) {
      line = this.reader.readLine(this.userPrompt).trim();
      this.cliWln("message: '"+line+"'");
      // handle input from commands (if they require input)
      if (this.activeCmd != null && this.activeCmd.requiresInput()) {
        this.activeCmd.handleInput(line);
        this.activeCmd = null;
        continue;
      }
      if (line.isEmpty()) continue;
      // tokenized by JLine into words
      ParsedLine parsed = reader.getParser().parse(line, startCursorIdx);
      words = parsed.words();
      this.userCmdName = words.getFirst(); // == word.get(0);
      this.userOptions = words.subList(startCursorIdx+1, words.size()).toArray(new String[0]);
      this.doCommand();
    }
    this.cliWln("\nBye !");
    this.safeCloseTerminal();
  }

  /**
   * apply command action associated with the command name
   * split command logic
   */
  private void doCommand() { // Locked Here need work from the others
    // exit case
    CmdRegister cmdRegister = CmdRegister.getInstance();
    Optional<ICmd> optional = cmdRegister.get(this.userCmdName.toLowerCase());
    if(optional.isEmpty()) {
      this.cliErr("Unknown command: " + this.userCmdName);
      return;
    }
    Cmd cmd = (Cmd)optional.get();
    this.cliWln(Arrays.toString(this.userOptions)); // something ab > opt is ab
    if (this.mode == ShellMode.RESTRICTED) {
      ICmd cmdName = optional.get();
      if (userCmdName.equalsIgnoreCase("quit")) this.running.set(false);
      // 4 possible options
      return;
    }

    cmd.shellExecute();
    if (cmd.requiresInput()) this.activeCmd = cmd;

  }

  /**
   * get shell context for commands
   * @return ICmdShellDelegate the concerned delegate that contains information
   */
  public ICmdShellDelegate getDelegate(){
      return new CmdShellDelegate(
        this.terminal,
        this.reader,
        this.running,
        this.cmdHistory,
        this.mode
      );
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

  /** fill options for the cli (done twice internally) */
  private void initOptions() {
    this.options = new Options();
    if (this.mode == ShellMode.RESTRICTED) {
      this.addRestrictedOptions();
    } else {
      this.addGameOptions();
    }
  }

  public void test() {
    var a = new ConsoleRenderer(null);
    a.renderer(new CoordinateMapper());
  }

  /**
   * display and update board into screen
   * @param board used to comunicate informations
   */
  @Override
  public void updateBoard(ConsoleRenderer board) {
    board.renderer(new CoordinateMapper());
  }

  @Override
  public void showMessage(String message) {}

  @Override
  public void showError(String error) {}

  @Override
  public boolean getUserConfirmation(String prompt) {
    return false;
  }

  @Override
  public void start() {}

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

  private void safeCloseTerminal() {
    try {
      this.terminal.close();
    } catch (IOException e) {
      this.cliErr("error closing terminal");
      this.cliErr(e.getMessage());
    }
  }

  /** load menu character in a variable once
   * from a file in resource directory */
  @Nonnull
  private String loadMainMenu() { // DP Command here
    final String defaultMenu = "Menu not available";
    final String shellMenuTxtFile = "cmdsInformations/agonShellMenu.txt";
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
}
