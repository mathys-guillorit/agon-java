package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** Command responsible for dynamically updating the game configuration. */
public final class CmdSet extends Cmd {

  private GameConfig gameConfig;

  private String[] args;

  /**
   * Constructs the base Set command for registration.
   *
   * @param uictx The user interface context.
   * @param gameConfig The configuration object to be modified.
   */
  public CmdSet(GameUserInterface uictx, GameConfig gameConfig) {
    super(uictx);
    Options options = super.getOptions();
    options.addOption("verbose", null, true, "increases the verbosity of the programme");
    options.addOption("debug", null, true, "displays the debug output");
    options.addOption("blitzmode", null, true, "sets if the game is a blitz");
    options.addOption("timeout", null, true, "set the timeout in milliseconds for blitzmode");
    options.addOption("aiActive", null, true, "enable aiPlayers for the game");
    options.addOption("aiMode", null, true, "set the algorithme to use for ai");
    options.addOption("aiDepth", null, true, "set the maximum depth for the AI algorithm");
    options.addOption(
        "aiIterativeDeepening", null, true, "set IterativeDeepening for AI algorithm");
    options.addOption("aiTimeLimit", null, true, "set the response time for an AI");
    options.addOption("aiHeuristic", null, true, "set the heuristic use for AI algorithm");
    options.addOption("whiteIsAI", null, true, "set if the white player is an AI");
    options.addOption("blackIsAI", null, true, "set if the black player an AI");
    this.gameConfig = gameConfig;
    this.setName("set");
  }

  /**
   * Internal constructor used to create an executable instance with arguments.
   *
   * @param uictx The user interface context.
   * @param gameConfig The configuration object.
   * @param args The CLI arguments to parse.
   */
  private CmdSet(GameUserInterface uictx, GameConfig gameConfig, String[] args) {
    this(uictx, gameConfig);
    this.args = args;
  }

  /**
   * Returns the usage and description for the set command.
   *
   * @return A formatted string for the help menu.
   */
  @Override
  public String getDescription() {
    return "Usage: set -PARAM VALUE\n"
        + "Description: Changes the current game configuration dynamically.\n"
        + "Example: set -aiDepth 5 -verbose true\n";
  }

  /**
   * Executes the configuration update logic.
   *
   * @param match The current match manager (unused during config update).
   * @return true if the configuration was successfully parsed and updated.
   */
  @Override
  public boolean execute(MatchManager match) {
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine cmd = parser.parse(super.getOptions(), args);
      StringBuilder feedback = new StringBuilder("Configuration updated:\n");

      if (cmd.hasOption("verbose")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("verbose"));
        gameConfig.setVerbose(val);
        feedback.append("  - Verbose: ").append(val).append("\n");
      }
      if (cmd.hasOption("debug")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("debug"));
        gameConfig.setDebug(val);
        feedback.append("  - Debug: ").append(val).append("\n");
      }

      if (cmd.hasOption("blitzmode")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("blitzmode"));
        gameConfig.setBlitzMode(val);
        feedback.append("  - BlitzMode: ").append(val).append("\n");
      }
      if (cmd.hasOption("timeout")) {
        int val = Integer.parseInt(cmd.getOptionValue("timeout"));
        gameConfig.setTimeout(val);
        feedback.append("  - Timeout: ").append(val).append("ms\n");
      }

      if (cmd.hasOption("aiActive")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("aiActive"));
        gameConfig.setAi(val);
        feedback.append("  - AI Active: ").append(val).append("\n");
      }
      if (cmd.hasOption("aiMode")) {
        String val = cmd.getOptionValue("aiMode").toLowerCase().trim();
        gameConfig.setAiMode(val);
        feedback.append("  - AI Mode: ").append(val).append("\n");
      }
      if (cmd.hasOption("aiDepth")) {
        int val = Integer.parseInt(cmd.getOptionValue("aiDepth"));
        gameConfig.setAiDepth(val);
        feedback.append("  - AI Depth: ").append(val).append("\n");
      }
      if (cmd.hasOption("aiTimeLimit")) {
        int val = Integer.parseInt(cmd.getOptionValue("aiTimeLimit"));
        gameConfig.setAiTimeLimit(val);
        feedback.append("  - AI Time Limit: ").append(val).append("s\n");
      }
      if (cmd.hasOption("aiIterativeDeepening")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("aiIterativeDeepening"));
        gameConfig.setAiIterativeDeepening(val);
        feedback.append("  - Iterative Deepening: ").append(val).append("\n");
      }
      if (cmd.hasOption("aiHeuristic")) {
        String val = cmd.getOptionValue("aiHeuristic").toLowerCase().trim();
        gameConfig.setAiHeuristic(val);
        feedback.append("  - Heuristic: ").append(val).append("\n");
      }

      if (cmd.hasOption("whiteIsAI")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("whiteIsAI"));
        gameConfig.setWhiteAi(val);
        feedback.append("  - White is AI: ").append(val).append("\n");
      }
      if (cmd.hasOption("blackIsAI")) {
        boolean val = Boolean.parseBoolean(cmd.getOptionValue("blackIsAI"));
        gameConfig.setBlackAi(val);
        feedback.append("  - Black is AI: ").append(val).append("\n");
      }

      this.getCtx().showInfo(feedback.toString());

    } catch (ParseException e) {
      this.getCtx().showError("Syntax error: " + e.getMessage());
      return false;
    } catch (NumberFormatException e) {
      this.getCtx().showError("Error: Value must be a number.");
      return false;
    }

    return true;
  }

  /**
   * Factory method to create an instance of the set command with provided arguments.
   *
   * @param args CLI arguments for configuration.
   * @return A new CmdSet instance.
   */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdSet(super.getCtx(), this.gameConfig, args);
  }
}
