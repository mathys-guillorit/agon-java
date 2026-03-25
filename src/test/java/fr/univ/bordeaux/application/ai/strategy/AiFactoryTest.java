package fr.univ.bordeaux.application.ai.strategy;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.ai.strategy.mcts.MctsStrategy;
import fr.univ.bordeaux.application.ai.strategy.minimax.MinimaxStrategy;
import fr.univ.bordeaux.technical.config.GameConfig;
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
    config.setAiHeuristic("mobility");

    AbstractAgonAi ai = AiFactory.createAi(config, Color.WHITE);

    assertNotNull(ai, "The factory should return an Ai instance.");
    assertInstanceOf(MctsStrategy.class, ai, "The returned Ai should be an MctsStrategy.");
  }

  @Test
  void testCreateAiUnknownModeReturnsNull() {
    config.setAiMode("neural_network");
    config.setAiHeuristic("mixed");

    AbstractAgonAi ai = AiFactory.createAi(config, Color.WHITE);

    assertNull(ai, "The factory should return null for an unknown Ai mode.");
  }

  @Test
  void testCreateHintAi() {
    AbstractAgonAi hintAi = AiFactory.createHintAi(Color.WHITE);

    assertNotNull(hintAi, "The factory should return a hint Ai instance.");
    assertInstanceOf(
        MinimaxStrategy.class, hintAi, "The returned hint Ai should be a MinimaxStrategy.");
  }
}
