package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.OptCompleterAdapter;
import javax.annotation.Nonnull;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jline.reader.Completer;

/**
 * Command responsible for displaying various game-related information.
 *
 * <p>Supported targets include the game board, move history, player timers, and current system
 * configuration.
 *
 * @author fr.univ.bordeaux
 * @version 1.0
 */
public final class CmdShow extends Cmd {

  /** CLI options for identifying the display target. */
  private final Options options;

  /** The specific target to display (e.g., "board", "history"). */
  private String target;

  /** Reference to the global game configuration. */
  private GameConfig gameConfig;

  /**
   * Constructs the base Show command for registration. Defines the available flags: -board,
   * -history, -time, -configuration.
   *
   * @param uictx The user interface context.
   * @param gameConfig The current game configuration.
   */
  public CmdShow(GameUserInterface uictx, GameConfig gameConfig) {
    super(uictx);
    this.gameConfig = gameConfig;
    this.setDesc("Description: Displays specific information about the current game state.");
    this.setName("show");
    this.options = new Options();
    this.options.addOption("board", null, false, "Display the Board");
    this.options.addOption("history", null, false, "Display the History");
    this.options.addOption("time", null, false, "Display the Time of both players");
    this.options.addOption("configuration", null, false, "Display the Configuration");
  }

  /**
   * Internal constructor used to create an instance with a specific target.
   *
   * @param uictx The user interface context.
   * @param gameConfig The configuration object.
   * @param target The identified target string.
   */
  private CmdShow(GameUserInterface uictx, GameConfig gameConfig, String target) {
    this(uictx, gameConfig);
    this.target = target;
  }

  /**
   * Returns the help description and available targets.
   *
   * @return A formatted string for the help menu.
   */
  @Override
  public String getDescription() {
    return "Usage: show [target]\n"
        + "Description: Displays specific information about the current game state.\n"
        + "Available targets:\n"
        + "  -board         : Shows the current hexagonal board state.\n"
        + "  -history       : Shows the history of all played turns.\n"
        + "  -time          : Shows the remaining time for each player.\n"
        + "  -configuration : Shows the current game settings.\n";
  }

  /**
   * Provides the autocompleter for show targets based on options.
   *
   * @return A {@link Completer} instance.
   */
  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return new OptCompleterAdapter(this.options).getCompleter(this.getName());
  }

  /**
   * Executes the display logic based on the identified target.
   *
   * @param match The current match manager.
   * @return true if the information was displayed, false otherwise.
   */
  @Override
  public boolean execute(MatchManager match) {
    if (target == null || target.isEmpty()) {
      this.getCtx().showError("No target specified. Use 'help show' for details.\n");
      return false;
    }

    return switch (target) {
      case "board" -> showBoard(match);
      case "history" -> showHistory(match);
      case "time" -> showTime(match);
      case "configuration" -> showConfiguration();
      default -> false;
    };
  }

  /**
   * Factory method to create an executable instance by parsing CLI arguments.
   *
   * @param args The flags provided by the user (e.g., ["-board"]).
   * @return A new specialized {@link CmdShow} instance.
   */
  @Override
  public CmdAction createNew(String[] args) {
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine line = parser.parse(this.options, args);

      if (line.getOptions().length > 1) {
        this.getCtx()
            .showError("Error: Please specify only one target (e.g., -board or -history).\n");
        return null;
      }

      String selectedTarget = "";
      if (line.hasOption("history")) selectedTarget = "history";
      else if (line.hasOption("time")) selectedTarget = "time";
      else if (line.hasOption("configuration")) selectedTarget = "configuration";
      else if (line.hasOption("board")) selectedTarget = "board";

      return new CmdShow(this.getCtx(), this.gameConfig, selectedTarget);

    } catch (ParseException e) {
      this.getCtx().showError("Invalid show command. Use 'help show' for details.\n");
      return null;
    }
  }

  private boolean showHistory(MatchManager match) {
    this.getCtx().showInfo("History command recognized but not yet implemented.\n");
    return true;
  }

  private boolean showBoard(MatchManager match) {
    if (match == null) {
      this.getCtx().showError("Error: No active match. Please create or load a game first.\n");
      return false;
    } else {
      this.getCtx().updateBoard(match.getAgonBoard());
      return true;
    }
  }

  private boolean showTime(MatchManager match) {
    this.getCtx().showInfo("Timer display recognized but not yet implemented.\n");
    return false;
  }

  private boolean showConfiguration() {
    this.getCtx().showMessage(this.gameConfig.toString() + "\n");
    return true;
  }

  @Override
  public Options getOptions() {
    return this.options;
  }
}
