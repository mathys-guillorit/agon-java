package fr.univ.bordeaux.technical.io.config;

import fr.univ.bordeaux.technical.utils.GameLogger;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Utility class to bind CommandLine options to a GameConfig object. *
 *
 * <p>This class centralizes the parsing logic for CLI arguments, ensuring consistency between the
 * initial application launch and the 'new' game command. It handles game modes (Blitz), AI
 * configurations (Minimax/MCTS), and logging levels.
 */
public class ConfigBinder {

  /**
   * Binds parsed command line options to the provided game configuration.
   *
   * <p>This method updates the {@link GameConfig} state based on flags like '-b' (blitz), '-a' (AI
   * activation), and advanced AI parameters such as depth, scoring functions, and time limits.
   *
   * @param cmd The parsed {@link CommandLine} containing user arguments.
   * @param config The {@link GameConfig} object to be updated.
   * @param ui The {@link GameUserInterface} context for displaying feedback or warnings.
   */
  public static void bindOptionsToConfig(CommandLine cmd, GameConfig config, GameUserInterface ui) {
    GameLogger.debug("ConfigBinder: Starting binding process...");

    // --- F4 & F31: Blitz Mode ---
    if (cmd.hasOption("b")) {
      config.setBlitzMode(true);
      GameLogger.info("ConfigBinder: Blitz mode enabled via options.");
    }

    if (cmd.hasOption("t")) {
      if (config.isBlitzMode()) {
        try {
          int time = Integer.parseInt(cmd.getOptionValue("t"));
          config.setTimeout(time);
          GameLogger.info("ConfigBinder: Global timeout set to " + time + " minutes.");
        } catch (NumberFormatException e) {
          GameLogger.error("ConfigBinder: Invalid time format for option 't'. Ignoring.");
        }
      } else {
        GameLogger.debug("ConfigBinder: Option 't' provided but ignored (not in Blitz mode).");
        ui.showInfo("Option 't' ignored because option blitz (-b) is missing.");
      }
    }

    // --- F8: AI Activation (-a [COLOR]) ---
    if (cmd.hasOption("a")) {
      config.setAi(true);
      String aiValue = cmd.getOptionValue("a");
      if (aiValue == null) {
        GameLogger.info("ConfigBinder: No color specified for AI, defaulting to BLACK.");
        ui.showInfo("No Color given for Ai player, setting by default black as Ai.");
        config.setBlackAi(true);
      } else {
        switch (aiValue.toLowerCase()) {
          case "white":
          case "w":
            config.setWhiteAi(true);
            GameLogger.info("ConfigBinder: White player set to AI.");
            break;
          case "black":
          case "b":
            config.setBlackAi(true);
            GameLogger.info("ConfigBinder: Black player set to AI.");
            break;
          case "a":
          case "all":
            config.setWhiteAi(true);
            config.setBlackAi(true);
            GameLogger.info("ConfigBinder: Both players set to AI.");
            break;
          default:
            GameLogger.error(
                "ConfigBinder: Invalid AI color '" + aiValue + "'. Defaulting to BLACK.");
            ui.showInfo("Invalid Color given for Ai player, setting by default Black as Ai.");
            config.setBlackAi(true);
            break;
        }
      }
    }
    boolean isMcts = false;
    // --- F32, F34, F36: AI Algorithm Mode ---
    if (cmd.hasOption("ai-mode")) {
      String mode = cmd.getOptionValue("ai-mode").toLowerCase();
      if (Arrays.asList("minimax", "mcts", "iterative").contains(mode)) {
        isMcts = mode.equals("mcts");
        if (isMcts) {
          config.setAiHeuristic("uct");
        }
        config.setAiMode(mode);
        config.setAiIterativeDeepening(mode.equals("iterative"));
        GameLogger.info("ConfigBinder: AI mode set to " + mode.toUpperCase());
      } else {
        GameLogger.error("ConfigBinder: Unknown AI mode '" + mode + "'. Using default.");
      }
    }

    // --- F35: AI Depth ---
    if (cmd.hasOption("ai-minimax-depth")) {
      try {
        int depth = Integer.parseInt(cmd.getOptionValue("ai-minimax-depth"));
        if (depth >= 1) {
          config.setAiDepth(depth);
          GameLogger.info("ConfigBinder: AI Minimax depth set to " + depth);
        } else {
          GameLogger.error("ConfigBinder: Depth must be >= 1. Ignoring value.");
        }
      } catch (NumberFormatException e) {
        GameLogger.error("ConfigBinder: Invalid depth format.");
      }
    }

    // --- F33: AI Heuristics ---
    if (cmd.hasOption("ai-minimax-scoring")) {
      String scoring = cmd.getOptionValue("ai-minimax-scoring").toLowerCase();
      if (Arrays.asList("mixed", "centrality", "mobility").contains(scoring)) {
        config.setAiHeuristic(scoring);
        GameLogger.info("ConfigBinder: AI Heuristic set to " + scoring);
      }
    }

    // --- AI Time Limit ---
    if (cmd.hasOption("ai-time")) {
      try {
        int aiTime = Integer.parseInt(cmd.getOptionValue("ai-time"));
        config.setAiTimeLimit(aiTime);
        GameLogger.info("ConfigBinder: AI response time limit set to " + aiTime + " ms.");
      } catch (NumberFormatException e) {
        GameLogger.error("ConfigBinder: Invalid AI time format.");
      }
    }

    if (cmd.hasOption("ai-mcts-selection")) {
      if (!isMcts) {
        ui.showInfo("Mcts mode for Ai is not active, option ai-mcts-selection is ignored.\n");
      } else {
        String selection = cmd.getOptionValue("ai-mcts-selection");
        if (selection.equals("ML") || selection.equals("UCT")) {
          config.setAiHeuristic(selection.toLowerCase());
          GameLogger.info("AI mcts mode set to " + selection);
        } else {
          ui.showInfo("Unreconised mode for mcts : " + selection + " setting by default UCT.\n");
          GameLogger.info("AI mcts mode set to default (UCT).");
        }
      }
    }

    // --- Verbose & Debug (Généralités) ---
    if (cmd.hasOption("v")) {
      config.setVerbose(true);
      GameLogger.getInstance().setVerbose(true);
      GameLogger.info("Verbose mode enabled.\n");
    }
    if (cmd.hasOption("d")) {
      config.setDebug(true);
      GameLogger.getInstance().setDebugMode(true);
      GameLogger.debug("ConfigBinder: Debug mode activated. Logging level increased.");
    }
  }

  /**
   * Populates an {@link Options} object with all possible game configuration flags.
   *
   * <p>This includes short and long flags for Blitz mode, AI activation, and all technical AI
   * parameters (heuristics, depth, mode).
   *
   * @param options The {@link Options} container to fill.
   */
  public static void fillOptions(Options options) {
    options.addOption("b", "blitz", false, "Launches the game in blitz mode.");
    options.addOption("t", "time", true, "Sets the time limit for each player (in minutes).");
    options.addOption(
        "a", "ai", true, "Replace the given color by an Ai. Can be both using A for color.");
    options.addOption(null, "ai-mode", true, "Set the mode to use for Ai player.\n");
    options.addOption(null, "ai-time", true, "Set the reflexion time for Ai players\n");
    options.addOption(null, "ai-minimax-depth", true, "Set the minimax depth for Ai players\n");
    options.addOption(
        null, "ai-minimax-scoring", true, "Set the minimax scoring function for Ai players\n");
    options.addOption(
        null, "ai-mcts-selection", true, "Set the MCTS algorithme function for Ai players\n");
  }
}
