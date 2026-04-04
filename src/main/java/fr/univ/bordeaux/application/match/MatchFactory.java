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
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.Map;

/** Create Match. */
public class MatchFactory {

  /**
   * Explicit.
   *
   * @param config {@link GameConfig}
   * @param gameUi {@link GameUserInterface} ui part to display the game.
   * @return {@link Match}
   */
  public static Match createMatch(GameConfig config, GameUserInterface gameUi) {
    AgonBoard agonBoard = new AgonBoardImpl();
    agonBoard.initBaseConfiguration();
    return createMatch(config, gameUi, agonBoard, Color.WHITE);
  }

  /**
   * Explicit.
   *
   * @param config {@link GameConfig}
   * @param gameUi {@link GameUserInterface} ui part to display the game.
   * @param agonBoard {@link AgonBoard} pre-initialized board.
   * @param startingColor {@link Color} current player turn.
   * @return {@link Match}
   */
  public static Match createMatch(
      GameConfig config, GameUserInterface gameUi, AgonBoard agonBoard, Color startingColor) {
    Map<Color, AbstractAgonAi> aiMap = AiFactory.createAiMap(config);

    Player white = createPlayerFromAiMap(aiMap, Color.WHITE, agonBoard, gameUi);
    Player black = createPlayerFromAiMap(aiMap, Color.BLACK, agonBoard, gameUi);
    if (config.isBlitzMode()) {
      return new BlitzMatch(agonBoard, white, black, config.getTimeout(), config, startingColor);
    } else {
      return new StandardMatch(agonBoard, white, black, config, startingColor);
    }
  }

  /**
   * TODO: complete here.
   *
   * @param aiMap {@link Map} get players by colors.
   * @param color {@link Color}
   * @param agonBoard {@link AgonBoard} Board to play on.
   * @param gameUi {@link GameUserInterface} ui that display the game.
   * @return {@link Player}
   */
  private static Player createPlayerFromAiMap(
      Map<Color, AbstractAgonAi> aiMap,
      Color color,
      AgonBoard agonBoard,
      GameUserInterface gameUi) {
    AgonAi aiStrategy = aiMap.get(color);
    if (aiStrategy != null) {
      return new AiPlayer("IA_" + color, color, agonBoard, aiStrategy);
    } else {
      return new HumanPlayer("Joueur_" + color, color, gameUi);
    }
  }

  /**
   * Creates an online match for two remote human players.
   *
   * <p>This method does not use UI, config, or AI. It is intended for server-side network matches.
   */
  public static Match createOnlineMatch(String whitePlayerName, String blackPlayerName) {
    AgonBoard agonBoard = new AgonBoardImpl();
    agonBoard.initBaseConfiguration();

    Player white = new NetworkPlayer(whitePlayerName, Color.WHITE);
    Player black = new NetworkPlayer(blackPlayerName, Color.BLACK);

    return new StandardMatch(agonBoard, white, black, new GameConfig());
  }
}
