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
    super(
        uictx,
        "show",
        "show [target]\n"
            + "Description: Displays specific information about the current game state.\n"
            + "Available targets: -board -history -time -configuration"
            + "for more details type \"help show\"\n");
    this.gameConfig = gameConfig;
    Options options = super.getOptions();
    options.addOption("board", null, false, "Shows the current hexagonal board state");
    options.addOption("history", null, false, "Shows the history of all played turns.");
    options.addOption("time", null, false, "Shows the remaining time for each player.");
    options.addOption("configuration", null, false, "Shows the current game settings.");
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
      case "configuration" -> showConfiguration(match);
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
    if (match == null || match.isMatchOver()) {
      this.getCtx().showError("Cannot show history because you are not currently in match.\n");
      return false;
    }
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
    if (match == null || match.isMatchOver()) {
      this.getCtx().showError("Error: No active match. Please create or load a game first.\n");
      return false;
    } else {
      this.getCtx().onMatchUpdate(match);
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
    if (match == null || match.isMatchOver()) {
      ui.showMessage("This command can only be used when you are currently in a blitz match.\n");
      return false;
    } else {
      String playerTimer = match.getCurrentPlayerRemainingTime();
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
  private boolean showConfiguration(MatchManager match) {
    if (match != null) {
      this.getCtx()
          .showMessage(
              "This is the configuration for the match you are playing "
                  + "it may have some differences between the real configuration "
                  + "if you have used the SET command.\n"
                  + match.getGameConfig().toString()
                  + "\n");
    } else {
      super.getCtx().showMessage(this.gameConfig.toString() + "\n");
    }
    return true;
  }
}
