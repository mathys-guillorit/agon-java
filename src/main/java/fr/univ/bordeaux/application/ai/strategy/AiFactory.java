package fr.univ.bordeaux.application.ai.strategy;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.ai.heuristics.CentralityHeuristic;
import fr.univ.bordeaux.application.ai.heuristics.Heuristic;
import fr.univ.bordeaux.application.ai.heuristics.MixedHeuristic;
import fr.univ.bordeaux.application.ai.heuristics.MobilityHeuristic;
import fr.univ.bordeaux.application.ai.strategy.mcts.MctsStrategy;
import fr.univ.bordeaux.application.ai.strategy.minimax.MinimaxStrategy;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for AI.
 */
public class AiFactory {

  private AiFactory() {}

  /**
   * Create AI mapped with colors.
   *
   * @param config {@link GameConfig}
   *
   * @return {@link Map}
   */
  public static Map<Color, AbstractAgonAi> createAiMap(GameConfig config) {
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
   *
   * @param color {@link Color}
   *
   * @return {@link AbstractAgonAi}
   */
  public static AbstractAgonAi createAi(GameConfig config, Color color) {
    Heuristic heuristic = createHeuristic(config.getAiHeuristic());
    String mode = config.getAiMode();
    switch (mode) {
      case "minimax" -> {
        return new MinimaxStrategy(
            heuristic,
            color,
            config.getAiDepth(),
            config.isAiIterativeDeepening(),
            config.getAiTimeLimit());
      }
      case "mcts" -> {
        return new MctsStrategy(heuristic, color);
      }
      default -> {
        return null;
      }
    }
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

  /**
   * See next best turn.
   *
   * @param color {@link Color}
   *
   * @return {@link AbstractAgonAi}
   */
  public static AbstractAgonAi createHintAi(Color color) {
    return new MinimaxStrategy(new MixedHeuristic(10, 1), color, 4, true, 5);
  }
}
