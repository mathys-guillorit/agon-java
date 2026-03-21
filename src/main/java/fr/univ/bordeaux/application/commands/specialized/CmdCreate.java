package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.match.MatchFactory;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.technical.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import javax.annotation.Nonnull;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jline.reader.Completer;

/**
 * Command responsible for initializing a new game session.
 *
 * <p>This command parses player configurations (AI vs Human, Color selection) and resets the game
 * state using the {@link MatchFactory}. *
 *
 * <p>Command representation in CLI: {@code new [ARGS]}
 */
public class CmdCreate extends Cmd {

  /** Configuration object to store player types and colors. */
  private GameConfig gameConfig;

  /** The core game engine to be updated with the new match manager. */
  private GameEngine gameEngine;

  /** CLI options for player and color configuration. */
  private Options options;

  /** Arguments passed by the user (e.g., -p1Ia true). */
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
   * Constructs a command instance ready for execution with specific arguments. Defines all
   * available CLI options (-p1Ia, -p2Ia, -p1Color, -p2Color).
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
    this.options = new Options();
    this.options.addOption(
        "p1Ia", "player1IsAi", true, "Define if the player 1 is an AI (true/false)");
    this.options.addOption(
        "p2Ia", "player2IsAi", true, "Define if the player 2 is an AI (true/false)");
    this.options.addOption(
        "p1Color", "player1Color", true, "Define the color for the player 1 (white/black)");
    this.options.addOption(
        "p2Color", "player2Color", true, "Define the color for the player 2 (white/black)");
  }

  /**
   * Retrieves the autocompleter for this command. * @return The {@link Completer} instance,
   * inherited from {@link Cmd}.
   */
  @Nonnull
  @Override
  public Completer getAutoCompleter() {
    return super.getAutoCompleter();
  }

  /**
   * Returns the CLI options recognized by this command. * @return An {@link Options} object
   * containing player and color settings.
   */
  @Override
  public Options getOptions() {
    return this.options;
  }

  /**
   * Executes the game creation logic.
   *
   * <p>This method parses the arguments, updates the {@link GameConfig}, creates a new {@link
   * Match} via {@link MatchFactory}, and registers it into the {@link GameEngine}.
   *
   * @param unused The MatchManager (not used during creation as a new one is generated).
   * @return true if the game was successfully initialized, false if parsing failed.
   */
  @Override
  public boolean execute(MatchManager unused) {
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmd = parser.parse(options, args);
      Color p1Color = Color.WHITE;

      // Color Logic
      if (cmd.hasOption("p1Color")) {
        p1Color = Color.valueOf(cmd.getOptionValue("p1Color").toUpperCase());
      } else if (cmd.hasOption("p2Color")) {
        Color p2Color = Color.valueOf(cmd.getOptionValue("p2Color").toUpperCase());
        p1Color = (p2Color == Color.WHITE) ? Color.BLACK : Color.WHITE;
      }

      // AI Logic
      boolean p1IsAi = false;
      if (cmd.hasOption("p1Ia")) {
        p1IsAi = Boolean.parseBoolean(cmd.getOptionValue("p1Ia"));
      }

      boolean p2IsAi = true;
      if (cmd.hasOption("p2Ia")) {
        p2IsAi = Boolean.parseBoolean(cmd.getOptionValue("p2Ia"));
      }

      // Applying configuration
      if (p1Color == Color.WHITE) {
        gameConfig.setWhiteAI(p1IsAi);
        gameConfig.setBlackAI(p2IsAi);
      } else {
        gameConfig.setWhiteAI(p2IsAi);
        gameConfig.setBlackAI(p1IsAi);
      }

    } catch (ParseException | IllegalArgumentException e) {
      this.getCtx().showError("Invalid options for command 'new': " + e.getMessage());
      return false;
    }

    Match match = MatchFactory.createMatch(gameConfig, this.getCtx());
    gameEngine.setMatchManager((MatchManager) match);
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
   * Provides a help description and usage example for this command. * @return A formatted string
   * describing how to use 'new'.
   */
  @Override
  public String getDescription() {
    return "Usage: new\n"
        + "Description: Starts a new Agon game session. This will reset the board and timers.\n";
  }
}
