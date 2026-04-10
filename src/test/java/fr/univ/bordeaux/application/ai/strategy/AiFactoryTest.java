package fr.univ.bordeaux.application.ai.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.ai.strategy.mcts.MctsStrategy;
import fr.univ.bordeaux.application.ai.strategy.minimax.MinimaxStrategy;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Test suite for the {@link AiFactory} class. */
class AiFactoryTest {

  private GameConfig config;

  @BeforeEach
  void setUp() {
    config = new GameConfig();
  }

  @Test
  void testCreateAiMapNoAi() {
    config.setWhiteAi(false);
    config.setBlackAi(false);

    Map<Color, AbstractAgonAi> aiMap = AiFactory.createAiMap(config);

    assertNotNull(aiMap);
    assertTrue(aiMap.isEmpty(), "The map should be empty when no Ais are configured.");
  }

  @Test
  void testCreateAiMapSingleAi() {
    config.setWhiteAi(true);
    config.setBlackAi(false);
    config.setAiMode("minimax");
    config.setAiHeuristic("mixed");

    Map<Color, AbstractAgonAi> aiMap = AiFactory.createAiMap(config);

    assertEquals(1, aiMap.size(), "The map should contain exactly one Ai.");
    assertTrue(
        aiMap.containsKey(Color.WHITE), "The map should contain an Ai for the White player.");
    assertFalse(aiMap.containsKey(Color.BLACK));
  }

  @Test
  void testCreateAiMapBothAis() {
    config.setWhiteAi(true);
    config.setBlackAi(true);
    config.setAiMode("minimax");
    config.setAiHeuristic("mixed");

    Map<Color, AbstractAgonAi> aiMap = AiFactory.createAiMap(config);

    assertEquals(2, aiMap.size(), "The map should contain two Ais.");
    assertTrue(aiMap.containsKey(Color.WHITE));
    assertTrue(aiMap.containsKey(Color.BLACK));
  }

  @Test
  void testCreateAiMinimaxMode() {
    config.setAiMode("minimax");
    config.setAiHeuristic("centrality");
    config.setAiDepth(3);

    AbstractAgonAi ai = AiFactory.createAi(config, Color.BLACK);

    assertNotNull(ai, "The factory should return an Ai instance.");
    assertInstanceOf(MinimaxStrategy.class, ai, "The returned Ai should be a MinimaxStrategy.");
  }

  @Test
  void testCreateAiMctsMode() {
    config.setAiMode("mcts");
    config.setAiHeuristic("uct");

    AbstractAgonAi ai = AiFactory.createAi(config, Color.WHITE);

    assertNotNull(ai, "The factory should return an Ai instance.");
    assertInstanceOf(MctsStrategy.class, ai, "The returned Ai should be an MctsStrategy.");
  }

  @Test
  void testCreateAiUnknownModeReturnsNull() {
    config.setAiMode("neural_network");
    config.setAiHeuristic("mixed");

    AbstractAgonAi ai = AiFactory.createAi(config, Color.WHITE);

    assertNotNull(ai, "The factory should return a base default Ai.");
    assertTrue(config.getAiMode().equals("minimax"));
    assertTrue(config.getAiHeuristic().equals("mixed"));
  }

  @Test
  void testCreateHintAi() {
    AbstractAgonAi hintAi = AiFactory.createHintAi(Color.WHITE);

    assertNotNull(hintAi, "The factory should return a hint Ai instance.");
    assertInstanceOf(
        MinimaxStrategy.class, hintAi, "The returned hint Ai should be a MinimaxStrategy.");
  }

  @Test
  void testCreateAiMinimaxModeIncompatibleHeuristic() {
    config.setAiMode("minimax");
    config.setAiHeuristic("uct");

    assertThrows(
        IncompatibleAiConfigurationException.class,
        () -> {
          AiFactory.createAi(config, Color.BLACK);
        },
        "The factory should throw an exception for an unknown heuristic in minimax.");
  }

  @Test
  void testCreateAiMctsModeWithUct() {
    config.setAiMode("mcts");
    config.setAiHeuristic("uct");

    AbstractAgonAi ai = AiFactory.createAi(config, Color.WHITE);

    assertNotNull(ai, "The factory should return an Ai instance for MCTS with UCT.");
    assertInstanceOf(MctsStrategy.class, ai, "The returned Ai should be an MctsStrategy.");
  }

  @Test
  void testCreateAiMctsModeWithMl() {
    config.setAiMode("mcts");
    config.setAiHeuristic("ml");

    AbstractAgonAi ai = AiFactory.createAi(config, Color.WHITE);

    assertNotNull(ai, "The factory should return an Ai instance for MCTS with ML.");
    assertInstanceOf(MctsStrategy.class, ai, "The returned Ai should be an MctsStrategy.");
  }

  @Test
  void testCreateAiMctsModeUnknownHeuristic() {
    config.setAiMode("mcts");
    config.setAiHeuristic("random_string");

    assertThrows(
        IncompatibleAiConfigurationException.class,
        () -> {
          AiFactory.createAi(config, Color.WHITE);
        },
        "The factory should throw an exception for an unknown selection heuristic in mcts.");
  }

  @Test
  void testIncompatibleMinimaxMctsHeuristic() {
    config.setAiMode("minimax");
    config.setAiHeuristic("uct"); // Incompatible

    assertThrows(
        IncompatibleAiConfigurationException.class,
        () -> {
          AiFactory.createAi(config, Color.WHITE);
        });
  }

  @Test
  void testIncompatibleMctsMinimaxHeuristic() {
    config.setAiMode("mcts");
    config.setAiHeuristic("centrality"); // Incompatible

    assertThrows(
        IncompatibleAiConfigurationException.class,
        () -> {
          AiFactory.createAi(config, Color.WHITE);
        });
  }
}
