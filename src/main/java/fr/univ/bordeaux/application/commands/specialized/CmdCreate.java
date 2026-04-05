package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.match.MatchFactory;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.technical.io.config.ConfigBinder;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.utils.GameLogger;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.MatchObserver;
import fr.univ.bordeaux.ui.ObservableMatch;
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
    options.addOption("a", "ai", true, "Set the [Color] player with an Ai.\n");
    options.addOption("b", "blitz", false, "Set the game mode to blitz\n");
    options.addOption("t", "time", true, "Set the reflexion time for both player\n");
    options.addOption(null, "ai-mode", true, "Set the mode to use for Ai player.\n");
    options.addOption(null, "ai-time", true, "Set the reflexion time for Ai players\n");
    options.addOption(null, "ai-minimax-depth", true, "Set the minimax depth for Ai players\n");
    options.addOption(
        null, "ai-minimax-scoring", true, "Set the minimax scoring function for Ai players\n");
  }

  /**
   * Executes the game creation logic.
   *
   * @param unused The MatchManager (not used during creation as a new one is generated).
   * @return true if the game was successfully initialized, false if parsing failed.
   */
  @Override
  public boolean execute(MatchManager unused) {
    GameLogger.info("Executing 'new' command...");
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmd = parser.parse(super.getOptions(), args);
      GameConfig matchConfig = this.gameConfig.copy();
      ConfigBinder.bindOptionsToConfig(cmd, matchConfig, super.getCtx());
      MatchManager match = MatchFactory.createMatch(matchConfig, this.getCtx());
      ((ObservableMatch) match).setObserver((MatchObserver) super.getCtx());
      gameEngine.setMatchManager(match);
    } catch (ParseException | IllegalArgumentException e) {
      GameLogger.error(e.getMessage());
      this.getCtx().showError("Invalid options for command 'new': " + e.getMessage());
      return false;
    }

    return true;
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
