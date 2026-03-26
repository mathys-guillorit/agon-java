package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** Command responsible for displaying various game-related information. */
public final class CmdShow extends Cmd {

  private String target;

  private GameConfig gameConfig;

  /**
   * Constructs the base Show command for registration.
   *
   * @param uictx The user interface context.
   * @param gameConfig The current game configuration.
   */
  public CmdShow(GameUserInterface uictx, GameConfig gameConfig) {
    super(uictx);
    this.gameConfig = gameConfig;
    this.setDesc("Description: Displays specific information about the current game state.");
    this.setName("show");
    Options options = super.getOptions();
    options.addOption("board", null, false, "Display the Board");
    options.addOption("history", null, false, "Display the History");
    options.addOption("time", null, false, "Display the Time of both players");
    options.addOption("configuration", null, false, "Display the Configuration");
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
   * @return A new specialized CmdShow instance.
   */
  @Override
  public CmdAction createNew(String[] args) {
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine line = parser.parse(super.getOptions(), args);
      if (line.getOptions().length > 1) {
        String msg = "Please specify only one target (e.g., -board or -history).\n";
        this.getCtx().showError(msg);
        return null;
      }
      String selectedTarget = "";
      if (line.hasOption("history")) {
        selectedTarget = "history";
      } else if (line.hasOption("time")) {
        selectedTarget = "time";
      } else if (line.hasOption("configuration")) {
        selectedTarget = "configuration";
      } else if (line.hasOption("board")) {
        selectedTarget = "board";
      }
      return new CmdShow(this.getCtx(), this.gameConfig, selectedTarget);
    } catch (ParseException e) {
      this.getCtx().showError("Invalid show command. Use 'help show' for details.\n");
      return null;
    }
  }

  /**
   * Displays the game history.
   *
   * @param match The current match.
   * @return false.
   */
  private boolean showHistory(MatchManager match) {
    this.getCtx().displayHistory(match.getHistory());
    return false;
  }

  /**
   * Displays the current board.
   *
   * @param match The current match.
   * @return false.
   */
  private boolean showBoard(MatchManager match) {
    if (match == null) {
      this.getCtx().showError("Error: No active match. Please create or load a game first.\n");
      return false;
    } else {
      this.getCtx().updateBoard(match.getAgonBoard());
      return false;
    }
  }

  /**
   * Displays the remaining time for the current player.
   *
   * @param match The current match.
   * @return false.
   */
  private boolean showTime(MatchManager match) {
    GameUserInterface ui = this.getCtx();
    if (match == null) {
      ui.showMessage("This command can only be used when you are currently in a blitz match");
      return false;
    } else {
      String playerTimer = match.getRemainingTime();
      Player currentPlayer = match.getCurrentPlayer();
      ui.showMessage(
          "Remaining time for : "
              + currentPlayer.getName()
              + "( "
              + currentPlayer.getColor()
              + " ) : "
              + playerTimer
              + "\n");
    }
    return false;
  }

  /**
   * Displays the current system configuration.
   *
   * @return true.
   */
  private boolean showConfiguration() {
    this.getCtx().showMessage(this.gameConfig.toString() + "\n");
    return true;
  }
}
