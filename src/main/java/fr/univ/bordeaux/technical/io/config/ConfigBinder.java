package fr.univ.bordeaux.technical.io.config;

import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;

/**
 * Utility class to bind CommandLine options to a GameConfig object. This prevents code duplication
 * between the main launcher and the 'new' command.
 */
public class ConfigBinder {

  /**
   * Binds parsed command line options to the provided game configuration.
   *
   * @param cmd The parsed command line.
   * @param config The configuration object to update.
   */
  public static void bindOptionsToConfig(CommandLine cmd, GameConfig config, GameUserInterface Ui) {

    // --- F4 & F31: Blitz Mode ---
    boolean isBlitz = cmd.hasOption("b");
    config.setBlitzMode(isBlitz);
    if (cmd.hasOption("t")) {
      if (isBlitz) {
        try {
          int time = Integer.parseInt(cmd.getOptionValue("t"));
          config.setTimeout(time);
        } catch (NumberFormatException e) {
          // Optionnel : logger ou ignorer
        }
      } else {
        Ui.showInfo("Option 't' ignored because option blitz is missing.");
      }
    }

      // --- F8: AI Activation (-a [COLOR]) ---
      if (cmd.hasOption("a")) {
        String aiValue = cmd.getOptionValue("a");
        if (aiValue == null) {
          Ui.showInfo("No Color given for Ai player, setting by default black as Ai.");
          config.setBlackAi(true);
        } else {
          switch (aiValue.toLowerCase()) {
            case "white":
            case "w":
              config.setWhiteAi(true);
              break;
            case "black":
            case "b":
              config.setBlackAi(true);
              break;
            case "a":
            case "all":
              config.setWhiteAi(true);
              config.setBlackAi(true);
              break;
            default:
              Ui.showInfo("Invalid Color given for Ai player, setting by default Black as Ai.");
              config.setBlackAi(true);
              break;
          }
        }
      }

      // --- F32, F34, F36: AI Algorithm Mode --,
      if (cmd.hasOption("ai-mode")) {
        String mode = cmd.getOptionValue("ai-mode").toLowerCase();
        if (Arrays.asList("minimax", "mcts", "iterative").contains(mode)) {
          config.setAiMode(mode);
          // F34: Si le mode est itératif, on active le flag spécifique
          config.setAiIterativeDeepening(mode.equals("iterative"));
        }
      }

      // --- F35: AI Depth ---
      if (cmd.hasOption("ai-minimax-depth")) {
        try {
          int depth = Integer.parseInt(cmd.getOptionValue("ai-minimax-depth"));
          if (depth >= 1) {
            config.setAiDepth(depth);
          }
        } catch (NumberFormatException ignored) {
        }
      }

      // --- F33: AI Heuristics ---
      if (cmd.hasOption("ai-minimax-scoring")) {
        String scoring = cmd.getOptionValue("ai-minimax-scoring").toLowerCase();
        if (Arrays.asList("mixed", "centrality", "mobility").contains(scoring)) {
          config.setAiHeuristic(scoring);
        }
      }

      // --- AI Time Limit ---
      if (cmd.hasOption("ai-time")) {
        try {
          int aiTime = Integer.parseInt(cmd.getOptionValue("ai-time"));
          config.setAiTimeLimit(aiTime);
        } catch (NumberFormatException ignored) {
        }
      }

      // --- Verbose & Debug (Généralités) ---
      if (cmd.hasOption("v") || cmd.hasOption("verbose")) {
        config.setVerbose(true);
      }
    }
  }
