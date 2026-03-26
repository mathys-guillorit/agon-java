package fr.univ.bordeaux.ui.cli;

import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MoveDTO;
import fr.univ.bordeaux.ui.AbstractGameUi;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jline.keymap.KeyMap;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * The AgonShell class acts as the primary Command Line Interface (CLI) manager for the Agon game.
 * It integrates the JLine library to provide advanced terminal features such as command history,
 * real-time autocompletion (tab-completion), and styled ANSI output.
 *
 * <p>This class implements {@link GameUserInterface} to bridge the gap between the core game logic
 * and the user's terminal session.
 *
 * @author fr.univ.bordeaux
 * @version 1.0
 */
public class AgonShell extends AbstractGameUi implements GameUserInterface {

  /** Atomic flag used to control the main execution loop of the shell. */
  private AtomicBoolean running;

  /** The low-level JLine Terminal instance handling I/O streams. */
  private Terminal terminal;

  /** The high-level JLine LineReader responsible for parsing user input and managing history. */
  private LineReader reader;

  /** Buffers the last line of input received from the user. */
  private String line;

  /** ANSI-styled header for application-wide messages: [AGON]. */
  private final String msgHA;

  /** ANSI-styled tag for informational messages: [INFO]. */
  private final String msgBI;

  /** ANSI-styled tag for warning alerts: [WARNING]. */
  private final String msgBW;

  /** ANSI-styled tag for critical error reports: [ERROR]. */
  private final String msgBE;

  /** Stores the ASCII art representation of the main menu. */
  private String mainMenuASCII = "No default Menu set";

  /** The prompt string displayed at the beginning of each input line. */
  private String userPrompt = "> ";

  /** Registry containing all executable commands available in the shell. */
  private AgonRegister<CmdAction> cmds;

  /** If true, the shell outputs detailed operational feedback. */
  private boolean verbose;

  /** Atomic flag for debug mode, allowing real-time toggling of technical logs. */
  private AtomicBoolean debug;

  /**
   * Internal initialization method. Sets default states for flags and constructs the default user
   * prompt.
   */
  private void init() {
    this.verbose = false;
    this.debug = new AtomicBoolean(false);
    this.running = new AtomicBoolean(true);
    this.userPrompt = this.msgHA + "> ";
  }

  /**
   * Constructs a new AgonShell and configures the JLine environment. Binds "Ctrl+R" for incremental
   * history search by default.
   *
   * @param term The {@link Terminal} to be used for physical I/O.
   * @param reader The {@link LineReader} used for capturing user input.
   * @param cmds The {@link AgonRegister} containing the command set.
   */
  public AgonShell(Terminal term, LineReader reader, AgonRegister<CmdAction> cmds) {
    super();
    this.terminal = term;
    this.reader = reader;
    this.msgHA = this.cliLayer();
    this.msgBI = this.cliInfo();
    this.msgBW = this.cliWarn();
    this.msgBE = this.cliError();
    this.init();
    this.cmds = cmds;
    reader
        .getKeyMaps()
        .get(LineReader.MAIN)
        .bind(new Reference(LineReader.HISTORY_INCREMENTAL_SEARCH_BACKWARD), KeyMap.ctrl('R'));
  }

  /**
   * Updates the main menu ASCII art displayed via the {@link #showHelp()} method.
   *
   * @param mainMenu The raw ASCII string to be loaded.
   */
  public void loadMainMenu(String mainMenu) {
    this.mainMenuASCII = mainMenu;
  }

  /**
   * Signals the application to terminate the main loop and triggers the terminal's graceful
   * shutdown.
   */
  public void leave() {
    this.running.set(false);
    this.safeCloseTerminal();
  }

  /**
   * Checks if the shell is currently active and accepting input.
   *
   * @return true if the shell's running state is active.
   */
  public boolean isRunning() {
    return this.running.get();
  }

  /**
   * Captures a single line of input from the terminal. Intercepts {@link UserInterruptException}
   * (Ctrl+C) to trigger the quit process.
   *
   * @return The trimmed input string, or null if the input is empty or interrupted.
   */
  public String getUserInput() {
    try {
      line = this.reader.readLine(this.userPrompt).trim();
    } catch (UserInterruptException e) {
      this.quit();
      return null;
    }
    if (line.isEmpty()) {
      return null;
    }

    this.reader.getHistory().add(line);
    return line;
  }

  /**
   * Closes the terminal and its associated streams. Logs an error if the closing operation fails.
   */
  public void safeCloseTerminal() {
    try {
      this.cliWln("System: Terminal session closed. Bye!");
      this.terminal.close();
    } catch (IOException e) {
      this.showError("Failed to close terminal: " + e.getMessage());
    }
  }

  /**
   * Core completion logic for JLine. 1. If input is empty, provides a list of all available
   * commands. 2. If a partial command is typed, suggests matching command keys. 3. If a command is
   * fully recognized, delegates completion to the command's own completer.
   *
   * @param reader The current LineReader instance.
   * @param line The parsed input line containing words and cursor position.
   * @param candidates The list to be populated with completion suggestions.
   */
  public void globalCompleter(LineReader reader, ParsedLine line, List<Candidate> candidates) {
    List<String> words = line.words();
    if (words.isEmpty() || words.getFirst().isEmpty()) {
      this.cmds.getKeys().forEach(key -> candidates.add(new Candidate(key)));
      return;
    }

    String firstWord = words.getFirst();
    Optional<CmdAction> cmdOpt = this.cmds.get(firstWord.toLowerCase());

    if (cmdOpt.isEmpty()) {
      this.cmds.getKeys().stream()
          .filter(key -> key.startsWith(firstWord.toLowerCase()))
          .forEach(key -> candidates.add(new Candidate(key)));
      return;
    }

    // Delegation to the specific command completer
    CmdAction cmd = cmdOpt.get();
    if (cmd.getAutoCompleter() != null) {
      cmd.getAutoCompleter().complete(reader, line, candidates);
    }
  }

  /**
   * Generates a styled [ERROR] tag using ANSI red foreground.
   *
   * @return ANSI formatted string.
   */
  private String cliError() {
    return new AttributedStringBuilder()
        .append("[")
        .style(AttributedStyle.BOLD.foreground(AttributedStyle.RED))
        .append("ERROR")
        .style(AttributedStyle.DEFAULT)
        .append("]")
        .toAnsi();
  }

  /**
   * Generates a styled [AGON] tag using ANSI magenta foreground.
   *
   * @return ANSI formatted string.
   */
  private String cliLayer() {
    return new AttributedStringBuilder()
        .append("[")
        .style(AttributedStyle.BOLD.foreground(AttributedStyle.MAGENTA))
        .append("AGON")
        .style(AttributedStyle.DEFAULT)
        .append("]")
        .toAnsi();
  }

  /**
   * Generates a styled [INFO] tag using ANSI blue foreground.
   *
   * @return ANSI formatted string.
   */
  private String cliInfo() {
    return new AttributedStringBuilder()
        .append("[")
        .style(AttributedStyle.BOLD.foreground(AttributedStyle.BLUE))
        .append("INFO")
        .style(AttributedStyle.DEFAULT)
        .append("]")
        .toAnsi();
  }

  /**
   * Generates a styled [WARNING] tag using ANSI yellow foreground.
   *
   * @return ANSI formatted string.
   */
  private String cliWarn() {
    return new AttributedStringBuilder()
        .append("[")
        .style(AttributedStyle.BOLD.foreground(AttributedStyle.YELLOW))
        .append("WARNING")
        .style(AttributedStyle.DEFAULT)
        .append("]")
        .toAnsi();
  }

  @Override
  public void showError(String msg) {
    this.cliW(this.msgHA + this.msgBE + " " + msg + "\n");
  }

  /**
   * Displays an informational message with full CLI branding.
   *
   * @param msg The info content.
   */
  @Override
  public void showInfo(String msg) {
    this.cliW(this.msgHA + this.msgBI + " " + msg + "\n");
  }

  /**
   * Displays a warning message with full CLI branding.
   *
   * @param msg The warning content.
   */
  @Override
  public void showWarn(String msg) {
    this.cliW(this.msgHA + this.msgBW + " " + msg + "\n");
  }

  /**
   * Internal write method that flushes the terminal buffer immediately.
   *
   * @param msg String to print.
   */
  private void cliWln(String msg) {
    this.terminal.writer().println(msg);
    this.terminal.flush();
  }

  /**
   * Internal write method without an automatic newline.
   *
   * @param msg String to print.
   */
  private void cliW(String msg) {
    this.terminal.writer().print(msg);
    this.terminal.flush();
  }

  /** Displays the help menu art to the terminal. */
  public void showHelp() {
    this.cliWln(this.mainMenuASCII);
  }

  /**
   * Initiates the shutdown sequence. Asks the user for a save confirmation before flipping the
   * running state.
   */
  @Override
  public void quit() {
    this.showWarn("Unsaved changes may be lost. Save game now? [y/n]");
    String input = this.getUserInput();
    if (input != null && input.equalsIgnoreCase("y")) {
      this.saveGame();
      this.showInfo("Game saved successfully.");
    }
    this.leave();
  }

  /**
   * Renders the current state of the game board using the ConsoleRenderer.
   *
   * @param board The restricted board view to render.
   */
  @Override
  public void updateBoard(RestrictedAgonBoard board) {
    this.cliWln(ConsoleRenderer.getBoardRepresentation(board));
  }

  /**
   * Prints a raw message to the terminal without specific CLI tags.
   *
   * @param message The content to display.
   */
  @Override
  public void showMessage(String message) {
    this.cliW(message);
  }

  /**
   * Toggles the verbosity of shell output.
   *
   * @param state true to enable detailed feedback.
   */
  public void setVerbose(boolean state) {
    this.verbose = state;
    this.showInfo("Verbosity is now " + (state ? "ON" : "OFF"));
  }

  public boolean getVerbose() {
    return this.verbose;
  }

  /**
   * Provides access to the debug mode state for external command logic.
   *
   * @return AtomicBoolean reference.
   */
  @Override
  public AtomicBoolean getDebugMode() {
    return this.debug;
  }

  /**
   * Returns the execution state of the shell.
   *
   * @return AtomicBoolean reference.
   */
  public AtomicBoolean getRunning() {
    return running;
  }

  public void displayHistory(List<MoveDTO> moves) {
    List<MoveDTO> history = moves;

    if (history.isEmpty()) {
      this.showInfo("The history is currently empty.");
      return;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("[history]\n");

    // On parcourt l'historique 2 par 2 (un tour = un coup O + un coup X)
    for (int i = 0; i < history.size(); i += 2) {
      // Coup du joueur O (Premier joueur du tour)
      MoveDTO moveO = history.get(i);
      sb.append("O ")
          .append(moveO.from().toLowerCase())
          .append(" ")
          .append(moveO.to().toLowerCase())
          .append(";");

      // Coup du joueur X (S'il existe déjà dans la liste)
      if (i + 1 < history.size()) {
        MoveDTO moveX = history.get(i + 1);
        sb.append(" X ")
            .append(moveX.from().toLowerCase())
            .append(" ")
            .append(moveX.to().toLowerCase())
            .append(";");
      }

      sb.append("\n");
    }

    this.showMessage(sb.toString());
  }
}
