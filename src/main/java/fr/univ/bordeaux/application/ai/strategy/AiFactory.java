package fr.univ.bordeaux.application.ai.strategy;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.ai.heuristics.*;
import fr.univ.bordeaux.application.ai.strategy.mcts.MctsStrategy;
import fr.univ.bordeaux.application.ai.strategy.minimax.MinimaxStrategy;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import java.util.HashMap;
import java.util.Map;

/** Factory for AI. */
public final class AiFactory {

  private AiFactory() {}

  /**
   * Create AI mapped with colors.
   *
   * @param config {@link GameConfig}
   * @return {@link Map}
   * @throws IncompatibleAiConfigurationException if the configuration is invalid.
   */
  public static Map<Color, AbstractAgonAi> createAiMap(GameConfig config)
      throws IncompatibleAiConfigurationException {
    Map<Color, AbstractAgonAi> aiMap = new HashMap<>();
    if (config.isWhiteAi()) {
      aiMap.put(Color.WHITE, createAi(config, Color.WHITE));
    }
    if (config.isBlackAi()) {
      aiMap.put(Color.BLACK, createAi(config, Color.BLACK));
    }
    return aiMap;
  }

  /**
   * Create an AI.
   *
   * @param config {@link GameConfig}
   * @param color {@link Color}
   * @return {@link AbstractAgonAi}
   * @throws IncompatibleAiConfigurationException if the heuristic and mode are incompatible.
   */
  public static AbstractAgonAi createAi(GameConfig config, Color color)
      throws IncompatibleAiConfigurationException {
    String mode = config.getAiMode();
    String heuristicName = config.getAiHeuristic();

    boolean isIncompatible = false;
    String expected = "";

    switch (mode) {
      case "minimax", "iterative" -> {
        Heuristic heuristic = createHeuristic(heuristicName);
        if (heuristic == null) {
          isIncompatible = true;
          expected = "centrality, mobility or mixed";
        } else {
          return new MinimaxStrategy(
              heuristic,
              color,
              config.getAiDepth(),
              config.isAiIterativeDeepening() || mode.equals("iterative"),
              config.getAiTimeLimit());
        }
      }
      case "mcts" -> {
        MctsSelectionHeuristic heuristic = createSelectionHeuristic(heuristicName);
        if (heuristic == null) {
          isIncompatible = true;
          expected = "uct or ml";
        } else {
          return new MctsStrategy(color, heuristic, config.getAiTimeLimit());
        }
      }
      default -> {
        return null;
      }
    }

    if (isIncompatible) {
      String message =
          "Incompatible configuration: Heuristic '"
              + heuristicName
              + "' is not compatible with mode '"
              + mode
              + "'. Expected "
              + expected
              + ". Falling back to default AI: Minimax, Mixed, Depth 4, Iterative.";
      config.setAiMode("minimax");
      config.setAiHeuristic("mixed");
      config.setAiDepth(4);
      config.setAiIterativeDeepening(true);

      throw new IncompatibleAiConfigurationException(message);
    }

    return null;
  }

  private static Heuristic createHeuristic(String type) {
    switch (type) {
      case "centrality" -> {
        return new CentralityHeuristic();
      }
      case "mobility" -> {
        return new MobilityHeuristic();
      }
      case "mixed" -> {
        return new MixedHeuristic(10, 1);
      }
      default -> {
        return null;
      }
    }
  }

  private static MctsSelectionHeuristic createSelectionHeuristic(String type) {
    switch (type) {
      case "uct" -> {
        return new UctHeuristic(Math.sqrt(2));
      }
      case "ml" -> {
        return new MlHeuristic(Math.sqrt(2));
      }
      default -> {
        return null;
      }
    }
  }

  /**
   * See next best turn.
   *
   * @param color {@link Color}
   * @return {@link AbstractAgonAi}
   */
  public static AbstractAgonAi createHintAi(Color color) {
    return new MinimaxStrategy(new MixedHeuristic(10, 1), color, 4, true, 5);
  }
}
