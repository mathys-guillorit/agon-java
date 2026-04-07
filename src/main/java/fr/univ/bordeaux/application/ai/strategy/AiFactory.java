package fr.univ.bordeaux.application.ai.strategy;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.ai.heuristics.*;
import fr.univ.bordeaux.application.ai.strategy.mcts.MctsStrategy;
import fr.univ.bordeaux.application.ai.strategy.minimax.MinimaxStrategy;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import java.util.HashMap;
import java.util.Map;

public class AiFactory {

  private AiFactory() {}

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

  public static AbstractAgonAi createAi(GameConfig config, Color color) {
    String mode = config.getAiMode();
    switch (mode) {
      case "minimax" -> {
        Heuristic heuristic = createHeuristic(config.getAiHeuristic());
        return new MinimaxStrategy(
            heuristic,
            color,
            config.getAiDepth(),
            config.isAiIterativeDeepening(),
            config.getAiTimeLimit());
      }
      case "mcts" -> {
        MctsSelectionHeuristic heuristic = createSelectionHeuristic(config.getAiHeuristic());
        return new MctsStrategy(color, heuristic, config.getAiTimeLimit());
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

  public static AbstractAgonAi createHintAi(Color color) {
    return new MinimaxStrategy(new MixedHeuristic(10, 1), color, 4, true, 5);
  }
}
