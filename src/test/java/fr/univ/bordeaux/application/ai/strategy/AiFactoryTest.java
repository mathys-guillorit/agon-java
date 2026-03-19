package fr.univ.bordeaux.application.ai.strategy;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.ai.strategy.mcts.MctsStrategy;
import fr.univ.bordeaux.application.ai.strategy.minimax.MinimaxStrategy;
import fr.univ.bordeaux.technical.config.GameConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for the {@link AiFactory} class.
 */
class AiFactoryTest {

    private GameConfig config;

    @BeforeEach
    void setUp() {
        config = new GameConfig();
    }

    @Test
    void testCreateAiMapNoAi() {
        config.setWhiteAI(false);
        config.setBlackAI(false);

        Map<Color, AbstractAgonAi> aiMap = AiFactory.createAiMap(config);

        assertNotNull(aiMap);
        assertTrue(aiMap.isEmpty(), "The map should be empty when no AIs are configured.");
    }

    @Test
    void testCreateAiMapSingleAi() {
        config.setWhiteAI(true);
        config.setBlackAI(false);
        config.setAiMode("minimax");
        config.setAiHeuristic("mixed");

        Map<Color, AbstractAgonAi> aiMap = AiFactory.createAiMap(config);

        assertEquals(1, aiMap.size(), "The map should contain exactly one AI.");
        assertTrue(aiMap.containsKey(Color.WHITE), "The map should contain an AI for the White player.");
        assertFalse(aiMap.containsKey(Color.BLACK));
    }

    @Test
    void testCreateAiMapBothAis() {
        config.setWhiteAI(true);
        config.setBlackAI(true);
        config.setAiMode("minimax");
        config.setAiHeuristic("mixed");

        Map<Color, AbstractAgonAi> aiMap = AiFactory.createAiMap(config);

        assertEquals(2, aiMap.size(), "The map should contain two AIs.");
        assertTrue(aiMap.containsKey(Color.WHITE));
        assertTrue(aiMap.containsKey(Color.BLACK));
    }

    @Test
    void testCreateAiMinimaxMode() {
        config.setAiMode("minimax");
        config.setAiHeuristic("centrality");
        config.setAiDepth(3);

        AbstractAgonAi ai = AiFactory.createAi(config, Color.BLACK);

        assertNotNull(ai, "The factory should return an AI instance.");
        assertInstanceOf(MinimaxStrategy.class, ai, "The returned AI should be a MinimaxStrategy.");
    }

    @Test
    void testCreateAiMctsMode() {
        config.setAiMode("mcts");
        config.setAiHeuristic("mobility");

        AbstractAgonAi ai = AiFactory.createAi(config, Color.WHITE);

        assertNotNull(ai, "The factory should return an AI instance.");
        assertInstanceOf(MctsStrategy.class, ai, "The returned AI should be an MctsStrategy.");
    }

    @Test
    void testCreateAiUnknownModeReturnsNull() {
        config.setAiMode("neural_network");
        config.setAiHeuristic("mixed");

        AbstractAgonAi ai = AiFactory.createAi(config, Color.WHITE);

        assertNull(ai, "The factory should return null for an unknown AI mode.");
    }

    @Test
    void testCreateHintAi() {
        AbstractAgonAi hintAi = AiFactory.createHintAi(Color.WHITE);

        assertNotNull(hintAi, "The factory should return a hint AI instance.");
        assertInstanceOf(MinimaxStrategy.class, hintAi, "The returned hint AI should be a MinimaxStrategy.");
    }
}