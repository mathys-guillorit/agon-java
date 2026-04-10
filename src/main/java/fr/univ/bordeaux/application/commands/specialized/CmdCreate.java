package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.ai.strategy.IncompatibleAiConfigurationException;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.match.MatchFactory;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.match.ReadOnlyMatch;
import fr.univ.bordeaux.technical.io.config.ConfigBinder;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.utils.GameLogger;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.MatchObserver;
import fr.univ.bordeaux.ui.ObservableMatch;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** Command responsible for initializing a new game session. */
public class CmdCreate extends Cmd {

  /** Configuration object to store player types and colors. */
  private GameConfig gameConfig;

  /** The core game engine to be updated with the new match manager. */
  private GameEngine gameEngine;

  /** Arguments passed by the user (e.g., -a black). */
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
    super(ui, "new", null); // desc is override by method
    this.gameConfig = gameConfig;
    this.gameEngine = gameEngine;
    this.args = args;
    Options options = super.getOptions();
    ConfigBinder.fillOptions(options);
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

      MatchManager match;
      try {
        match = MatchFactory.createMatch(matchConfig, this.getCtx());
      } catch (IncompatibleAiConfigurationException e) {
        // En cas d'incompatibilité, la factory a déjà corrigé matchConfig.
        // On affiche l'erreur (qui contient les détails du fallback) et on réessaie.
        this.getCtx().showWarn("Warning: " + e.getMessage());
        match = MatchFactory.createMatch(matchConfig, this.getCtx());
      }

      ((ObservableMatch) match).setObserver((MatchObserver) super.getCtx());
      if (match instanceof ReadOnlyMatch) {
        ((MatchObserver) super.getCtx()).onMatchUpdate((ReadOnlyMatch) match);
      }
      gameEngine.setMatchManager(match);
    } catch (ParseException | IllegalArgumentException e) {
      GameLogger.error(e.getMessage());
      this.getCtx().showError("Error: " + e.getMessage());
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
    final var sb = new StringBuilder();
    sb.append("new ");
    for (Option opt : this.getOptions().getOptions()) {
      if (opt.hasLongOpt()) {
        sb.append(opt.getLongOpt());
      }
      if (opt.getOpt() != null) {
        sb.append(opt.getOpt());
      }
      sb.append(" ");
    }
    sb.append("\n").append("Description: Starts a new Agon game session.");
    sb.append(" This will reset the board and timers.\n");
    return sb.toString();
  }
}
