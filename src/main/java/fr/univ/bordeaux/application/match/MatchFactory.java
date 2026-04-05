package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAi;
import fr.univ.bordeaux.application.ai.strategy.AgonAi;
import fr.univ.bordeaux.application.ai.strategy.AiFactory;
import fr.univ.bordeaux.application.match.player.AiPlayer;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.utils.GameLogger;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.Map;

/**
 * Factory class responsible for instantiating the appropriate Match type based on game configuration.
 * It handles player creation (Human or AI) and board initialization.
 */
public class MatchFactory {

  /**
   * Creates a new match with a default board configuration.
   *
   * @param config The global {@link GameConfig} containing match rules.
   * @param gameUi The {@link GameUserInterface} used for player interactions.
   * @return A fully initialized {@link Match} instance.
   */
  public static Match createMatch(GameConfig config, GameUserInterface gameUi) {
    AgonBoard agonBoard = new AgonBoardImpl();
    agonBoard.initBaseConfiguration();
    GameLogger.debug("MatchFactory: Initialized default AgonBoard.");
    return createMatch(config, gameUi, agonBoard, Color.WHITE);
  }

  /**
   * Creates a match with a specific board state and starting player.
   *
   * @param config The global {@link GameConfig}.
   * @param gameUi The user interface.
   * @param agonBoard A pre-initialized {@link AgonBoard}.
   * @param startingColor The {@link Color} of the player who takes the first turn.
   * @return A {@link BlitzMatch} or {@link StandardMatch} depending on the config.
   */
  public static Match createMatch(
      GameConfig config, GameUserInterface gameUi, AgonBoard agonBoard, Color startingColor) {
    GameLogger.info("MatchFactory: Creating new match instance...");
    Map<Color, AbstractAgonAi> aiMap = AiFactory.createAiMap(config);

    Player white = createPlayerFromAiMap(aiMap, Color.WHITE, agonBoard, gameUi);
    Player black = createPlayerFromAiMap(aiMap, Color.BLACK, agonBoard, gameUi);

    if (config.isBlitzMode()) {
      GameLogger.info("MatchFactory: Mode = BLITZ (Timeout: " + config.getTimeout() + " min).");
      return new BlitzMatch(agonBoard, white, black, config.getTimeout(), config, startingColor);
    } else {
      GameLogger.info("MatchFactory: Mode = STANDARD.");
      return new StandardMatch(agonBoard, white, black, config, startingColor);
    }
  }

  /**
   * Helper method to instantiate a Player (Human or AI) based on the AI strategy map.
   *
   * @param aiMap A map containing AI strategies for each color (null strategy implies a Human player).
   * @param color The {@link Color} of the player to create.
   * @param agonBoard The board the player will interact with (required for AI).
   * @param gameUi The UI used for human input.
   * @return A concrete {@link Player} instance.
   */
  private static Player createPlayerFromAiMap(
      Map<Color, AbstractAgonAi> aiMap,
      Color color,
      AgonBoard agonBoard,
      GameUserInterface gameUi) {
    AgonAi aiStrategy = aiMap.get(color);
    if (aiStrategy != null) {
      GameLogger.info("MatchFactory: " + color + " player assigned to AI (" + aiStrategy.getClass().getSimpleName() + ")");
      return new AiPlayer("IA_" + color, color, agonBoard, aiStrategy);
    } else {
      GameLogger.info("MatchFactory: " + color + " player assigned to HUMAN.");
      return new HumanPlayer("Joueur_" + color, color, gameUi);
    }
  }
}