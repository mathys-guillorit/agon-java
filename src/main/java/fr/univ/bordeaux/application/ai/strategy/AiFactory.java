package fr.univ.bordeaux.application.ai.strategy;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.ai.heuristics.*;
import fr.univ.bordeaux.application.ai.strategy.mcts.MctsStrategy;
import fr.univ.bordeaux.application.ai.strategy.minimax.MinimaxStrategy;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.utils.GameLogger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
  public static Map<Color, AbstractAgonAi> createAiMap(final GameConfig config)
      throws IncompatibleAiConfigurationException {
    final Map<Color, AbstractAgonAi> aiMap = new ConcurrentHashMap<>();
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
   * @throws IncompatibleAiConfigurationException if heuristic and mode don't match.
   */
  public static AbstractAgonAi createAi(final GameConfig config, final Color color)
      throws IncompatibleAiConfigurationException {
    final AbstractAgonAi resultAi;
    final String mode = config.getAiMode();
    final String heuristicName = config.getAiHeuristic();
    boolean isIncompatible = false;

    switch (mode) {
      case "minimax", "iterative" -> {
        final Heuristic heuristic = createHeuristic(heuristicName);
        if (heuristic == null) {
          isIncompatible = true;
          resultAi = null;
        } else {
          resultAi = new MinimaxStrategy(
              heuristic,
              color,
              config.getAiDepth(),
              config.isAiIterativeDeepening() || mode.equals("iterative"),
              config.getAiTimeLimit());
        }
      }
      case "mcts" -> {
        final MctsSelectionHeuristic heuristic = createSelectionHeuristic(heuristicName);
        if (heuristic == null) {
          isIncompatible = true;
          resultAi = null;
        } else {
          resultAi = new MctsStrategy(color, heuristic, config.getAiTimeLimit());
        }
      }
      default -> resultAi = null;
    }

    if (isIncompatible) {
      config.setAiMode("minimax");
      config.setAiHeuristic("mixed");
      config.setAiDepth(4);
      config.setAiIterativeDeepening(true);

      throw new IncompatibleAiConfigurationException(
          "Heuristic '" + heuristicName + "' is incompatible with mode '" + mode + "'. Creating a base default Ai : minimax iterative deepening, depth 4 , heuristic mixed.");
    }

    return resultAi;
  }

  private static Heuristic createHeuristic(final String type) {
    return switch (type) {
      case "centrality" -> new CentralityHeuristic();
      case "mobility" -> new MobilityHeuristic();
      case "mixed" -> new MixedHeuristic(10, 1);
      default -> null;
    };
  }

  private static MctsSelectionHeuristic createSelectionHeuristic(final String type) {
    return switch (type) {
      case "uct" -> new UctHeuristic(Math.sqrt(2));
      case "ml" -> new MlHeuristic(Math.sqrt(2));
      default -> null;
    };
  }

  /**
   * See next best turn.
   *
   * @param color {@link Color}
   * @return {@link AbstractAgonAi}
   */
  public static AbstractAgonAi createHintAi(final Color color) {
    return new MinimaxStrategy(new MixedHeuristic(10, 1), color, 4, true, 5);
  }
}