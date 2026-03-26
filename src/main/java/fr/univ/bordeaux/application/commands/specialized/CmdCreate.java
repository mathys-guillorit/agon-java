package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.match.MatchFactory;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** Command responsible for initializing a new game session. */
public class CmdCreate extends Cmd {

  private GameConfig gameConfig;

  private GameEngine gameEngine;

  private String[] args;

  /**
   * Constructs a base command instance with default empty arguments.
   *
   * @param ui The user interface context for interaction.
   * @param gameConfig The game configuration to be populated.
   * @param gameEngine The engine that will run the match.
   */
  public CmdCreate(GameUserInterface ui, GameConfig gameConfig, GameEngine gameEngine) {
    this(ui, gameConfig, gameEngine, new String[0]);
  }

  /**
   * Constructs a command instance ready for execution with specific arguments.
   *
   * @param ui The user interface context.
   * @param gameConfig The configuration to modify.
   * @param gameEngine The engine to update.
   * @param args The arguments parsed from the user input.
   */
  public CmdCreate(
      GameUserInterface ui, GameConfig gameConfig, GameEngine gameEngine, String[] args) {
    super(ui);
    this.setName("new");
    this.gameConfig = gameConfig;
    this.gameEngine = gameEngine;
    this.args = args;
    Options options = super.getOptions();
    options.addOption("p1Ia", "player1IsAi", true, "Define if the player 1 is an AI (true/false)");
    options.addOption("p2Ia", "player2IsAi", true, "Define if the player 2 is an AI (true/false)");
    options.addOption(
        "p1Color", "player1Color", true, "Define the color for the player 1 (white/black)");
    options.addOption(
        "p2Color", "player2Color", true, "Define the color for the player 2 (white/black)");
    options.addOption("b", "blitz", false, "Set the game mode to blitz\n");
    options.addOption("t", "time", true, "Set the reflexion time for both player\n");
  }

  /**
   * Executes the game creation logic.
   *
   * @param unused The MatchManager (not used during creation as a new one is generated).
   * @return true if the game was successfully initialized, false if parsing failed.
   */
  @Override
  public boolean execute(MatchManager unused) {
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmd = parser.parse(super.getOptions(), args);
      Color p1Color = null;
      Color p2Color = null;

      if (cmd.hasOption("p1Color")) {
        p1Color = Color.valueOf(cmd.getOptionValue("p1Color").toUpperCase());
      }
      if (cmd.hasOption("p2Color")) {
        p2Color = Color.valueOf(cmd.getOptionValue("p2Color").toUpperCase());
      }

      if (p1Color != null && p2Color != null) {
        if (p1Color == p2Color) {
          this.getCtx().showError("Player 1 and Player 2 cannot have the same color.\n");
          return false;
        }
      } else if (p1Color != null) {
        p2Color = (p1Color == Color.WHITE) ? Color.BLACK : Color.WHITE;
      } else if (p2Color != null) {
        p1Color = (p2Color == Color.WHITE) ? Color.BLACK : Color.WHITE;
      } else {
        p1Color = Color.WHITE;
        p2Color = Color.BLACK;
      }

      boolean p1IsAi = false;
      if (cmd.hasOption("p1Ia")) {
        p1IsAi = Boolean.parseBoolean(cmd.getOptionValue("p1Ia"));
      }

      boolean p2IsAi = true;
      if (cmd.hasOption("p2Ia")) {
        p2IsAi = Boolean.parseBoolean(cmd.getOptionValue("p2Ia"));
      }

      if (p1Color == Color.WHITE) {
        gameConfig.setWhiteAI(p1IsAi);
        gameConfig.setBlackAI(p2IsAi);
      } else {
        gameConfig.setWhiteAI(p2IsAi);
        gameConfig.setBlackAI(p1IsAi);
      }
      this.checkBlitzMode(cmd);
    } catch (ParseException | IllegalArgumentException e) {
      this.getCtx().showError("Invalid options for command 'new': " + e.getMessage());
      return false;
    }

    MatchManager match = MatchFactory.createMatch(gameConfig, this.getCtx());
    gameEngine.setMatchManager(match);
    return true;
  }

  /**
   * Checks and sets blitz mode configuration.
   *
   * @param cmd The parsed command line options.
   */
  public void checkBlitzMode(CommandLine cmd) {
    boolean blitzOption = cmd.hasOption("b");
    boolean timeOption = cmd.hasOption("t");
    gameConfig.setBlitzMode(blitzOption);
    if (timeOption && !blitzOption) {
      this.getCtx().showWarn("Use option -t/--time for blitz games\n");
    }
    if (!timeOption) {
      gameConfig.setTimeout(30);
    } else {
      int time = Integer.parseInt(cmd.getOptionValue("t"));
      gameConfig.setTimeout(time);
    }
  }

  /**
   * Factory method to create a new instance of this command with specific arguments.
   *
   * @param args The arguments to be associated with the new command instance.
   * @return A new {@link CmdAction} ready to be executed.
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdCreate(super.getCtx(), this.gameConfig, this.gameEngine, args);
  }

  /**
   * Provides a help description and usage example for this command.
   *
   * @return A formatted string describing how to use 'new'.
   */
  @Override
  public String getDescription() {
    return "Usage: new\n"
        + "Description: Starts a new Agon game session. This will reset the board and timers.\n";
  }
}
