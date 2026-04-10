package fr.univ.bordeaux.ui.cli;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.AppMode;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MoveDtO;
import fr.univ.bordeaux.application.match.ReadOnlyMatch;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.utils.GameLogger; // Import ajouté
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.MatchObserver;
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
public class AgonShell implements GameUserInterface, MatchObserver {

  /** Atomic flag used to control the main execution loop of the shell. */
  private AtomicBoolean running;

  /** The low-level JLine Terminal instance handling I/O streams. */
  private Terminal terminal;

  /** The high-level JLine LineReader responsible for parsing user input and managing history. */
  private LineReader reader;

  /** Buffers the last line of input received from the user. */
  private String line;

  /** ANSI-styled header for application-wide messages: [AGON]. */
  private final String msgHa;

  /** ANSI-styled tag for informational messages: [INFO]. */
  private final String msgBi;

  /** ANSI-styled tag for warning alerts: [WARNING]. */
  private final String msgBw;

  /** ANSI-styled tag for critical error reports: [ERROR]. */
  private final String msgBe;

  /** Stores the ASCII art representation of the main menu. */
  private String mainMenuAscii = "No default Menu set";

  /** The prompt string displayed at the beginning of each input line. */
  private String userPrompt = "> ";

  /** Registry containing all executable commands available in the shell. */
  private AgonRegister<CmdAction> cmds;

  /** If true, the shell outputs detailed operational feedback. */
  private boolean verbose;

  /** Atomic flag for debug mode, allowing real-time toggling of technical logs. */
  private AtomicBoolean debug;

  private String boardFooter = "";

  /**
   * Internal initialization method. Sets default states for flags and constructs the default user
   * prompt.
   */
  private void init() {
    this.verbose = false;
    this.debug = new AtomicBoolean(false);
    this.running = new AtomicBoolean(true);
    this.userPrompt = this.msgHa + "> ";
    GameLogger.info("AgonShell: CLI components initialized.");
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
    this.msgHa = this.cliLayer();
    this.msgBi = this.cliInfo();
    this.msgBw = this.cliWarn();
    this.msgBe = this.cliError();
    this.init();
    this.cmds = cmds;
    reader
        .getKeyMaps()
        .get(LineReader.MAIN)
        .bind(new Reference(LineReader.HISTORY_INCREMENTAL_SEARCH_BACKWARD), KeyMap.ctrl('R'));
    GameLogger.info("AgonShell: Terminal session started.");
  }

  /**
   * Updates the main menu ASCII art displayed via the {@link #showHelp()} method.
   *
   * @param mainMenu The raw ASCII string to be loaded.
   */
  public void loadMainMenu(String mainMenu) {
    this.mainMenuAscii = mainMenu;
    GameLogger.debug("AgonShell: Main menu ASCII loaded.");
  }

  /**
   * Signals the application to terminate the main loop and triggers the terminal's graceful
   * shutdown.
   */
  public void leave() {
    GameLogger.info("AgonShell: Requesting application shutdown.");
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
   * Captures a single line of input from the terminal.
   *
   * <p>Handles special cases:
   *
   * <ul>
   *   <li><b>Ctrl+C / Ctrl+D:</b> Returns "quit" to trigger the interactive save/exit logic.
   *   <li><b>Thread Interruption (Blitz):</b> Returns null to let the engine handle the timeout.
   * </ul>
   *
   * @return The trimmed input string, "quit" on user interrupt, or null on timeout/error.
   */
  public String getUserInput() {
    try {
      String readLine = this.reader.readLine(this.userPrompt);

      // Cas du Ctrl+D (EOF)
      if (readLine == null) {
        GameLogger.debug("AgonShell: EOF received (null input).");
        return "quit";
      }

      // --- CORRECTION : TRIM ET VÉRIFICATION ---
      line = readLine.trim();
      if (line.isEmpty()) {
        return null; // Retourne null pour les lignes vides (espaces inclus)
      }
      // ------------------------------------------

      this.reader.getHistory().add(line);
      GameLogger.debug("AgonShell: User entered command: " + line);
      return line;

    } catch (UserInterruptException e) {
      // Si le thread est interrompu par le chrono, on ne veut pas quitter
      if (Thread.currentThread().isInterrupted()) {
        GameLogger.debug("AgonShell: Input interrupted by match timer.");
        Thread.interrupted(); // Nettoie le flag d'interruption
        return null;
      }
      GameLogger.info("AgonShell: User interrupted (Ctrl+C).");
      return "quit";

    } catch (org.jline.reader.EndOfFileException e) {
      return "quit";

    } catch (Exception e) {
      if (e.getMessage() != null) {
        GameLogger.error("AgonShell: Unexpected error: " + e.getMessage());
      }
      return null;
    }
  }

  /**
   * Closes the terminal and its associated streams. Logs an error if the closing operation fails.
   */
  public void safeCloseTerminal() {
    try {
      this.cliWln("System: Terminal session closed. Bye!");
      this.terminal.close();
      GameLogger.info("AgonShell: Terminal closed successfully.");
    } catch (IOException e) {
      GameLogger.error("AgonShell: Failed to close terminal: " + e.getMessage());
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
    GameLogger.error("AgonShell (UI Display): " + msg);
    this.cliW(this.msgHa + this.msgBe + " " + msg + "\n");
  }

  /**
   * Displays an informational message with full CLI branding.
   *
   * @param msg The info content.
   */
  @Override
  public void showInfo(String msg) {
    // On ne loggue pas systématiquement en INFO ici car c'est souvent de l'affichage pur
    // pour l'utilisateur, mais on peut le mettre en DEBUG
    GameLogger.debug("AgonShell (UI Display Info): " + msg);
    this.cliW(this.msgHa + this.msgBi + " " + msg + "\n");
  }

  /**
   * Displays a warning message with full CLI branding.
   *
   * @param msg The warning content.
   */
  @Override
  public void showWarn(String msg) {
    GameLogger.debug("AgonShell (UI Display Warning): " + msg);
    this.cliW(this.msgHa + this.msgBw + " " + msg + "\n");
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
    GameLogger.debug("AgonShell: Displaying help menu.");
    this.cliWln(this.mainMenuAscii);
  }

  /**
   * Initiates the shutdown sequence. Asks the user for a save confirmation before flipping the
   * running state.
   */
  @Override
  public void quit() {
    this.leave();
  }

  /**
   * Renders the current state of the game board and match information.
   *
   * @param match The read-only match view to render.
   */
  @Override
  public void onMatchUpdate(ReadOnlyMatch match) {
    String renderedBoard = ConsoleRenderer.getBoardRepresentation(match.getAgonBoard());

    if (boardFooter != null && !boardFooter.isBlank()) {
      renderedBoard += "\n" + boardFooter;
    }

    this.cliWln(renderedBoard);

    if (match.isMatchOver()) {
      Player winner = match.getWinner();
      String winnerInfo = (winner != null) ? winner.getColor().toString() : "UNKNOWN";
      GameLogger.info("AgonShell: Match over. Winner: " + winnerInfo);
      this.showInfo("MATCH FINISHED! Winner: " + winnerInfo);
    } else {
      String[] timers = match.getAllPlayersRemainingTime();
      if (timers != null) {
        this.showInfo(
            "Current turn: "
                + match.getCurrentPlayer().getColor()
                + " Remaining time : White "
                + timers[0]
                + " Black "
                + timers[1]);
      } else {
        this.showInfo("Current turn: " + match.getCurrentPlayer().getColor());
      }
    }
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
   * Returns the execution state of the shell.
   *
   * @return AtomicBoolean reference.
   */
  public AtomicBoolean getRunning() {
    return running;
  }

  /**
   * Explicit.
   *
   * @param history {@link List}
   */
  public void displayHistory(List<MoveDtO> history) {
    GameLogger.debug("AgonShell: Displaying history with " + history.size() + " moves.");
    if (history.isEmpty()) {
      this.showInfo("The history is currently empty.");
      return;
    }

    StringBuilder sb = new StringBuilder();
    sb.append("[history]\n");

    // On parcourt l'historique 2 par 2 (un tour = un coup O + un coup X)
    for (int i = 0; i < history.size(); i += 2) {
      // Coup du joueur O (Premier joueur du tour)
      MoveDtO moveO = history.get(i);
      sb.append("O ")
          .append(moveO.from().toLowerCase())
          .append(" ")
          .append(moveO.to().toLowerCase())
          .append(";");

      // Coup du joueur X (S'il existe déjà dans la liste)
      if (i + 1 < history.size()) {
        MoveDtO moveX = history.get(i + 1);
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

  public void clearBoardDisplay() {
    this.cliWln("");
  }

  public void setBoardFooter(String boardFooter) {
    this.boardFooter = (boardFooter == null) ? "" : boardFooter;
  }

  /**
   * Initializes the player session directly from the shell.
   *
   * <p>The shell asks for the player name, then for the application mode, and updates the shared
   * application context accordingly.
   *
   * @param context the shared application context to initialize
   */
  public void initializeSession(AppContext context) {
    String name = askPlayerName();
    AppMode mode = askApplicationMode();

    context.setPlayerName(name);
    context.setMode(mode);

    GameLogger.info(
        "AgonShell: Session initialized with player '" + name + "' in mode " + mode + ".");
  }

  /**
   * Asks the user to enter a non-empty player name from the command line.
   *
   * @return the validated player name
   */
  private String askPlayerName() {
    String name = this.reader.readLine(this.userPrompt + "Enter your player name: ").trim();

    while (name.isEmpty()) {
      name =
          this.reader
              .readLine(this.userPrompt + "Name cannot be empty. Enter your player name: ")
              .trim();
    }

    return name;
  }

  /**
   * Asks the user to select the application mode from the command line.
   *
   * <p>The available modes are:
   *
   * <ul>
   *   <li>1 - Local
   *   <li>2 - Online
   * </ul>
   *
   * @return the selected application mode
   */
  private AppMode askApplicationMode() {
    this.terminal.writer().println(this.userPrompt + "Select mode:");
    this.terminal.writer().println("1 - Local");
    this.terminal.writer().println("2 - Online");
    this.terminal.flush();

    String input = this.reader.readLine(this.userPrompt + "Your choice: ").trim();

    while (!"1".equals(input) && !"2".equals(input)) {
      input =
          this.reader
              .readLine(this.userPrompt + "Invalid choice. Enter 1 (Local) or 2 (Online): ")
              .trim();
    }

    return "2".equals(input) ? AppMode.ONLINE : AppMode.LOCAL;
  }
}
