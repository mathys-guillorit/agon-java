package fr.univ.bordeaux.ui.cli;

import fr.univ.bordeaux.application.commands.ICmd;
import fr.univ.bordeaux.application.commands.network.*;
import fr.univ.bordeaux.ui.AbstractGameUI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
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

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.CmdRegistry;

import fr.univ.bordeaux.application.commands.specialized.CmdQuit;

public class AgonShell extends AbstractGameUI {

  private boolean running;
  private String userCmdName;
  private String[] userOptions; // alias "args"
  private ICmd command;

  private Terminal terminal;
  private LineReader reader;
  private PromptBuilder promptBuilder;

  /** represent all options available from the menu */
  private Options options = null;

  /** Message Header of the cli for the entire App */
  private final String msgHA;

  private final String mainMenuASCII;

  private ArrayList<String> cmdHistory;

  private final CmdRegistry registry = CmdRegistry.getInstance();
  private final AppContext context = new AppContext();

  /**
   * ASCII engine renderer
   */
  public AgonShell(){
    super(); // require the work of others
    this.running = true;
    this.msgHA = this.cliLayer();
    this.mainMenuASCII = this.loadMainMenu();
    this.cmdHistory = new ArrayList<>();
    this.initOptions();
    try {
      this.terminal = TerminalBuilder.builder().build(); // IOException
      this.reader = LineReaderBuilder.builder().terminal(terminal).build();
    } catch (IOException e) {
      this.cliErr("terminal initialization failed");
      this.cliErr(e.getMessage());
    }
    this.registerCommands();
  }

  /**
   * Registers all available network and system commands into the command registry.
   */
  private void registerCommands() {
    registry.register("ping", new CmdPing(context));
    registry.register("quit", new CmdQuit(context));
    registry.register("join", new CmdJoin(context));
    registry.register("server start", new CmdServerStart(context));
    registry.register("server stop",  new CmdServerStop(context));
    registry.register("server list",  new CmdServerList(context));
  }

  /** fill options for the cli (done once internally) */
  private void initOptions() {
    if (this.options != null) return; // done once
    Option help =
        Option.builder("h")
            .longOpt("help")
            .optionalArg(true)
            .hasArg()
            .argName("cmd")
            .desc("Show help (otpionally for a specific command")
            .get();
    this.options = new Options();
    // display Shell help
    this.options.addOption("h", "help", false, "show help");
    // display command help
    this.options.addOption("h", "help", true, "show help command");
  }

  public void test() {
    var a = new ConsoleRenderer(null);
    a.renderer();
  }

  @Override
  public void updateBoard(String board) {}

  @Override
  public void showMessage(String message) {}

  @Override
  public void showError(String error) {}

  @Override
  public boolean getUserConfirmation(String prompt) {
    return false;
  }

  @Override
  public void start() {
    this.loop();
  }

  /**
   * test to display a list in terminal
   */
  public void advancedTerminal(){
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

  /** load menu character in a variable once */
  @Nonnull
  private String loadMainMenu() { // DP Command here
    final String defaultMenu = "Menu not available";
    final String shellMenuTxtFile = "cmdsInformations/agonShellMenu.txt";
    final String resourcePath = "/" + shellMenuTxtFile;
    InputStream is = getClass().getResourceAsStream(resourcePath);
    if (is == null) return defaultMenu;
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
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
   * run the program to interact with the user
   */
  public void loop(){
    //this.showMainMenu();
    int startCursorIdx = 0;
    this.cliW(this.mainMenuASCII);
    String line;
    List<String> words;
    while (this.running) {
      line = this.reader.readLine(">> ");
      // tokenized by JLine into words
      ParsedLine parsed = reader.getParser().parse(line, startCursorIdx);
      words = parsed.words();
      this.userCmdName = words.getFirst(); // == word.get(0);
      startCursorIdx++;
      // in options, we must get only options not the command name included
      this.userOptions = words.subList(startCursorIdx, words.size())
        .toArray(new String[0]);
      this.running = this.conditionalReturning();
    }
    this.cliWln("Bye !");
    this.safeCloseTerminal();
  }

  /**
   * check if the user want to exit or not
   *
   * @return boolean : false if the user want to exit, true otherwise
   */
  private boolean conditionalReturning(){ // Locked Here need work from the others
    // 1) HELP: tu peux garder en dur (simple)
    if ("help".equalsIgnoreCase(this.userCmdName)) {
      this.cliWln(this.mainMenuASCII);
      return true;
    }

    // 2) Chercher la commande dans le registry
    var opt = registry.get(this.userCmdName);

    if (opt.isEmpty()) {
      this.cliErr("Unknown command: " + this.userCmdName);
      return true;
    }

    ICmd cmd = opt.get();

    // 3) Exécuter avec args
    try {
      cmd.execute(this.userOptions);
    } catch (Exception e) {
      this.cliErr("Command failed: " + e.getMessage());
      return true;
    }

    // 4) Si la commande est "quit" et qu'elle veut quitter l'app
    if (cmd.isQuit()) {
      terminal.writer().println("Save the game before quitting ? [y/n]");
      terminal.flush();

      String answer = reader.readLine(">> ");
      if ("y".equalsIgnoreCase(answer)) {
        // TODO: later -> save command
        this.cliWln("saving...");
        this.cliWln("saved");
      }
      return false; // stop loop
    }

    // 5) Sinon on continue
    return true;
  }

  private void cliActOnOptions() {
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmdl = parser.parse(options, this.userOptions);
      // just an example to how it works but no if and no switch must be done
      if (cmdl.hasOption("v")) {
        cliWln("Verbose mode");
      }
    } catch (Exception e) {
      this.cliWln("Invalid Command");
      //      new HelpFormatter().printHelp("load", options);
    }
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
