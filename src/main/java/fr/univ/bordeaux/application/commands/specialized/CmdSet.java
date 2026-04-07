package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.utils.GameLogger; // Import ajouté
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.Arrays; // Import ajouté pour le debug des args

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
    return "Usage: set PARAM=VALUE\n"
        + "Description: Changes the current game configuration dynamically, if you use this command in game the change will be effective in the real configuration but not in the match configuration.\n"
        + "Example: set aiDepth=5 verbose=true\n";
  }

  /**
   * Executes the configuration update logic.
   *
   * @param match The current match manager (unused during config update).
   * @return true if the configuration was successfully parsed and updated.
   */
  @Override
  public boolean execute(MatchManager match) {
    if (args == null || args.length == 0) {
      GameLogger.error("CmdSet: Execution failed - No arguments provided.");
      this.getCtx().showError("Error: No parameters provided. Usage: set PARAM=VALUE");
      return false;
    }

    GameLogger.info("CmdSet: Attempting to update configuration with: " + Arrays.toString(args));
    StringBuilder feedback = new StringBuilder("Configuration updated:\n");

    try {
      for (String arg : args) {
        if (!arg.contains("=")) {
          GameLogger.error("CmdSet: Invalid argument format -> " + arg);
          this.getCtx().showError("Invalid format for: " + arg + ". Expected PARAM=VALUE");
          continue;
        }

        String[] parts = arg.split("=", 2);
        String param = parts[0].trim();
        String value = parts[1].trim();

        // On log chaque changement individuellement en DEBUG pour la traçabilité fine
        GameLogger.debug("CmdSet: Processing parameter [" + param + "] with value [" + value + "]");

        switch (param) {
          case "verbose" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setVerbose(val);
            feedback.append("  - Verbose: ").append(val).append("\n");
          }
          case "debug" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setDebug(val);
            GameLogger.getInstance().setDebugMode(val); // Mise à jour dynamique du logger
            feedback.append("  - Debug: ").append(val).append("\n");
          }
          case "blitzmode" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setBlitzMode(val);
            feedback.append("  - BlitzMode: ").append(val).append("\n");
          }
          case "timeout" -> {
            int val = Integer.parseInt(value);
            gameConfig.setTimeout(val);
            feedback.append("  - Timeout: ").append(val).append("ms\n");
          }
          case "aiActive" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setAi(val);
            feedback.append("  - AI Active: ").append(val).append("\n");
          }
          case "aiMode" -> {
            gameConfig.setAiMode(value.toLowerCase());
            feedback.append("  - AI Mode: ").append(value).append("\n");
          }
          case "aiDepth" -> {
            int val = Integer.parseInt(value);
            gameConfig.setAiDepth(val);
            feedback.append("  - AI Depth: ").append(val).append("\n");
          }
          case "aiTimeLimit" -> {
            int val = Integer.parseInt(value);
            gameConfig.setAiTimeLimit(val);
            feedback.append("  - AI Time Limit: ").append(val).append("s\n");
          }
          case "aiIterativeDeepening" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setAiIterativeDeepening(val);
            feedback.append("  - Iterative Deepening: ").append(val).append("\n");
          }
          case "aiHeuristic" -> {
            gameConfig.setAiHeuristic(value.toLowerCase());
            feedback.append("  - Heuristic: ").append(value).append("\n");
          }
          case "whiteIsAi" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setWhiteAi(val);
            feedback.append("  - White is Ai: ").append(val).append("\n");
          }
          case "blackIsAi" -> {
            boolean val = Boolean.parseBoolean(value);
            gameConfig.setBlackAi(val);
            feedback.append("  - Black is Ai: ").append(val).append("\n");
          }
          default -> {
            GameLogger.error("CmdSet: Unknown parameter attempted -> " + param);
            this.getCtx().showError("Unknown parameter: " + param);
            this.getCtx().showInfo(this.getDescription());
            return false;
          }
        }
      }

      GameLogger.info("CmdSet: Configuration update successful.");
      this.getCtx().showInfo(feedback.toString());
      return true;

    } catch (NumberFormatException e) {
      GameLogger.error("CmdSet: Numerical parsing error -> " + e.getMessage());
      this.getCtx().showError("Error: Numeric value expected.");
      return false;
    }
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
