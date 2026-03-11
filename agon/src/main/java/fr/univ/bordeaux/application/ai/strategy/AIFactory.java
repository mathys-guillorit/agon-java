package fr.univ.bordeaux.application.ai.strategy;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.application.ai.heuristics.CentralityHeuristic;
import fr.univ.bordeaux.application.ai.heuristics.Heuristic;
import fr.univ.bordeaux.application.ai.heuristics.MixedHeuristic;
import fr.univ.bordeaux.application.ai.heuristics.MobilityHeuristic;
import fr.univ.bordeaux.application.ai.strategy.mcts.MctsStrategy;
import fr.univ.bordeaux.application.ai.strategy.minimax.MinimaxStrategy;
import fr.univ.bordeaux.technical.config.GameConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory utility class responsible for instantiating Artificial Intelligence strategies.
 * <p>
 * This class reads the parameters provided in a {@link GameConfig} object and
 * creates the corresponding {@link AbstractAgonAI} instances along with their
 * required {@link Heuristic} evaluation functions.
 * </p>
 */
public class AIFactory {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AIFactory(){
    }

    /**
     * Creates a map of AI strategies for the current game based on the configuration.
     * <p>
     * This method checks the configuration to determine if the White player,
     * the Black player, or both are controlled by an AI. It then builds and returns
     * a map containing only the active AI instances associated with their respective colors.
     * </p>
     *
     * @param config The {@link GameConfig} containing the game settings and AI preferences.
     * @return A {@link Map} associating a {@link Color} to an initialized {@link AbstractAgonAI}.
     * The map will be empty if no AIs are configured to play.
     */
    public static Map<Color, AbstractAgonAI> createAiMap(GameConfig config){
        Map<Color, AbstractAgonAI> aiMap = new HashMap<>();

        if(config.isWhiteAI()){
            aiMap.put(Color.WHITE, createAi(config, Color.WHITE));
        }
        if(config.isBlackAI()){
            aiMap.put(Color.BLACK, createAi(config, Color.BLACK));
        }
        return aiMap;
    }

    /**
     * Instantiates a specific AI strategy based on the configured mode and heuristic.
     * <p>
     * This method maps string values from the configuration (like {@code "minimax"}
     * or {@code "mcts"}) to their actual class implementations.
     * </p>
     *
     * @param config The {@link GameConfig} containing the AI algorithm and depth parameters.
     * @param color  The {@link Color} that this AI instance will play.
     * @return An instantiated {@link AbstractAgonAI} ready to compute moves,
     * or {@code null} if the requested mode is unknown.
     */
    public static AbstractAgonAI createAi(GameConfig config, Color color){
        Heuristic heuristic = createHeuristic(config.getAiHeuristic());
        String mode = config.getAiMode();
        switch(mode){
            case "minimax"->{
                return new MinimaxStrategy(heuristic, color, config.getAiDepth(), config.isAiIterativeDeepening(), config.getAiTimeLimit());
            }
            case "mcts"->{
                return new MctsStrategy(heuristic, color);
            }
            default->{
                return null;
            }
        }
    }


    /**
     * Creates a pre-configured AI specifically tailored for providing in-game hints.
     * <p>
     * This method bypasses the standard configuration to ensure the hint generation
     * is fast and reliable. It instantiates a {@link MinimaxStrategy} using a
     * {@link MixedHeuristic}, a fixed depth of 4, Iterative Deepening enabled,
     * and a strict 5-second time limit.
     * </p>
     *
     * @param color The {@link Color} of the player requesting the hint.
     * @return A fully configured {@link AbstractAgonAI} ready to calculate a suggested move.
     */
    public static AbstractAgonAI createHintAi(Color color){
        return new MinimaxStrategy(new MixedHeuristic(10, 1), color, 4, true, 5);
    }

    /**
     * Creates the heuristic evaluation function requested by the configuration.
     *
     * @param type A {@link String} representing the name of the heuristic
     * (e.g., {@code "centrality"}, {@code "mobility"}, {@code "mixed"}).
     * @return An instance of {@link Heuristic} corresponding to the requested type,
     * or {@code null} if the type is unknown.
     */
    private static Heuristic createHeuristic(String type){
        switch(type){
            case "centrality"->{
                return new CentralityHeuristic();
            }
            case "mobility"->{
                return new MobilityHeuristic();
            }
            case "mixed"->{
                return new MixedHeuristic(10, 1);
            }
            default->{
                return null;
            }
        }
    }
}