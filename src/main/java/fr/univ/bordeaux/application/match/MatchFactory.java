package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAi;
import fr.univ.bordeaux.application.ai.strategy.AgonAi;
import fr.univ.bordeaux.application.ai.strategy.AiFactory;
import fr.univ.bordeaux.application.match.player.AiPlayer;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.application.match.player.NetworkPlayer;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.utils.GameLogger;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.Map;

/**
 * Factory class responsible for instantiating the appropriate Match type based on game
 * configuration. It handles player creation (Human, AI, or Network) and board initialization.
 *
 * <p>This class centralizes the complex logic of assembling a match, ensuring that dependencies
 * like AI strategies and UI contexts are correctly injected.
 */
public class MatchFactory {

  /**
   * Creates a new match with a default board configuration and starting with White pieces.
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
   * @param config The global {@link GameConfig} containing rules and mode.
   * @param gameUi The user interface context.
   * @param agonBoard A pre-initialized {@link AgonBoard} instance.
   * @param startingColor The {@link Color} of the player who takes the first turn.
   * @return A {@link BlitzMatch} or {@link StandardMatch} depending on the configuration.
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
   * @param aiMap A map containing AI strategies for each color (null implies a Human player).
   * @param color The {@link Color} of the player to create.
   * @param agonBoard The board instance required for AI decision making.
   * @param gameUi The UI used for human input.
   * @return A concrete {@link Player} instance (either {@link AiPlayer} or {@link HumanPlayer}).
   */
  private static Player createPlayerFromAiMap(
      Map<Color, AbstractAgonAi> aiMap,
      Color color,
      AgonBoard agonBoard,
      GameUserInterface gameUi) {
    AgonAi aiStrategy = aiMap.get(color);
    if (aiStrategy != null) {
      GameLogger.info(
          "MatchFactory: "
              + color
              + " player assigned to AI ("
              + aiStrategy.getClass().getSimpleName()
              + ")");
      return new AiPlayer("IA_" + color, color, agonBoard, aiStrategy);
    } else {
      GameLogger.info("MatchFactory: " + color + " player assigned to HUMAN.");
      return new HumanPlayer("Joueur_" + color, color, gameUi);
    }
  }

  /**
   * Creates an online match for two remote human players using standard rules.
   *
   * @param whitePlayerName Name of the player using white pieces.
   * @param blackPlayerName Name of the player using black pieces.
   * @return A standard {@link Match} instance using {@link NetworkPlayer}s.
   */
  public static Match createOnlineMatch(String whitePlayerName, String blackPlayerName) {
    return createOnlineMatch(whitePlayerName, blackPlayerName, false);
  }

  /**
   * Creates an online match for two remote human players with an optional Blitz mode.
   *
   * @param whitePlayerName Name of the white player.
   * @param blackPlayerName Name of the black player.
   * @param blitzMode If {@code true}, instantiates a {@link BlitzMatch}; otherwise a {@link
   *     StandardMatch}.
   * @return A fully initialized online {@link Match}.
   */
  public static Match createOnlineMatch(
      String whitePlayerName, String blackPlayerName, boolean blitzMode) {
    AgonBoard agonBoard = new AgonBoardImpl();
    agonBoard.initBaseConfiguration();

    Player white = new NetworkPlayer(whitePlayerName, Color.WHITE);
    Player black = new NetworkPlayer(blackPlayerName, Color.BLACK);

    GameConfig config = new GameConfig();

    if (blitzMode) {
      return new BlitzMatch(agonBoard, white, black, config.getTimeout(), config, Color.WHITE);
    }

    return new StandardMatch(agonBoard, white, black, config);
  }
}
