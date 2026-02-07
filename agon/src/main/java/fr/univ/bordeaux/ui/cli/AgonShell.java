package fr.univ.bordeaux.ui.cli;

import fr.univ.bordeaux.ui.AbstractGameUI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import java.util.Map;

import org.jline.consoleui.prompt.ConsolePrompt;
import org.jline.consoleui.prompt.PromptResultItemIF;
import org.jline.consoleui.prompt.builder.PromptBuilder;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import javax.annotation.Nonnull;

public class AgonShell extends AbstractGameUI {

  private boolean running;
  private String userPromptLine;

  private Terminal terminal;
  private LineReader reader;
  private PromptBuilder promptBuilder;

  /**
   * Message Header of the cli for the entire App
   */
  private final String msgHA;
  private final String mainMenuASCII;


  /**
   * ASCII engine renderer
   */
  public AgonShell(){
    super(); // require the work of others
    this.running = true;
    this.msgHA = this.cliLayer();
    this.mainMenuASCII = this.loadMainMenu();
    try {
      this.terminal = TerminalBuilder.builder().build(); // IOException
      this.reader = LineReaderBuilder.builder().terminal(terminal).build();
    } catch (IOException e) {
      this.cliErr("terminal initialization failed");
      this.cliErr(e.getMessage());
    }
  }

  /**
   * test to display a list in terminal
   */
  public void advancedTerminal(){
    ConsolePrompt prompt = new ConsolePrompt(terminal);
    this.promptBuilder = prompt.getPromptBuilder();

    // Create a list prompt for single selection
    this.promptBuilder.createListPrompt()
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
      this.cliErr("exception type: "+e.getClass());
      this.cliErr(e.getMessage());
    }
  }

  private void safeCloseTerminal(){
    try {
      this.terminal.close();
    } catch (IOException e){
      this.cliErr("error closing terminal");
      this.cliErr(e.getMessage());
    }
  }

  /**
   * load menu character in a variable
   * once
   */
  @Nonnull
  private String loadMainMenu(){ // DP Command here
    final String defaultMenu = "Menu not available";
    final String shellMenuTxtFile = "agonShellMenu.txt";
    final String resourcePath = "/"+ shellMenuTxtFile;
    InputStream is = getClass().getResourceAsStream(resourcePath);
    if (is == null) return defaultMenu;
    try (BufferedReader reader = new BufferedReader(
       new InputStreamReader(is, StandardCharsets.UTF_8)
      )) {
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
    this.cliW(this.mainMenuASCII);
    while (running) {
      this.userPromptLine = reader.readLine(">> ");
      running = this.conditionalReturning();
    }
    this.cliWln("Bye !");
    this.safeCloseTerminal();
  }

  /**
   * check if the user want to exit or not
   * @return boolean : false if the user want to exit, true otherwise
   */
  private boolean conditionalReturning(){ // Locked Here need work from the others
    // exit case
    if ("quit".equalsIgnoreCase(this.userPromptLine)){
      terminal.writer().println(
        "Save the game before quitting ? [y/n]"
      );
      if(reader.readLine(">> ").equalsIgnoreCase("y")){
        /// TODO: DP Command here
        this.cliWln("saving...");
        this.cliWln("saved");
      }
      return false;
    }
    // others cases
    if ("help".equalsIgnoreCase(this.userPromptLine)){
      /// TODO: next version add argument with regex
      this.cliWln(this.mainMenuASCII);
      return true;
    }
    /// TODO: other cases required
    return true;
  }

  /**
   * format the output error to see where is the problem
   * (following maven style)
   * @param msg add a message to the error
   */
  private void cliErr(String msg){
    final String tag = "ERROR";
    AttributedStringBuilder asb = new AttributedStringBuilder();
    asb.append(this.msgHA)
     .append("[");
    asb.style(AttributedStyle.BOLD.foreground(AttributedStyle.RED))
     .append(tag);
    asb.style(AttributedStyle.DEFAULT)
     .append("] ")
     .append(msg);
    this.cliWln(asb.toAnsi());
  }

  /**
   * create the first bloc to know that we are in
   * agon game to make a difference with maven messages
   * (must be used once in the constructor to set attr)
   */
  private String cliLayer(){
    final String tag = "AGON";
    AttributedStringBuilder asb = new AttributedStringBuilder();
    asb.style(AttributedStyle.DEFAULT)
     .append("[");
    asb.style(AttributedStyle.BOLD.foreground(AttributedStyle.MAGENTA))
     .append(tag);
    asb.style(AttributedStyle.DEFAULT)
     .append("]");
    return asb.toAnsi();
  }


  /**
   * show a message in terminal using JLine
   * shortened the code verbose (because used many
   * times and must be changed once for all)
   * @param msg message to send in terminal
   */
  private void cliWln(String msg){
    this.terminal.writer().println(msg);
    terminal.flush();
  }

  /**
   * show a message in terminal using JLine
   * display inline without jumpline ("\n")
   * @param msg message to send in terminal
   */
  private void cliW(String msg){
    this.terminal.writer().print(msg);
    terminal.flush();
  }


}
