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
    // 1. On récupère la map des cerveaux IA (certaines couleurs seront null)
    Map<Color, AbstractAgonAi> aiMap = AiFactory.createAiMap(config);
    AgonBoard agonBoard = new AgonBoardImpl();
    agonBoard.initBaseConfiguration();

    // 2. On crée les vrais objets Player
    Player white = createPlayerFromAiMap(aiMap, Color.WHITE, agonBoard, gameUi);
    Player black = createPlayerFromAiMap(aiMap, Color.BLACK, agonBoard, gameUi);
    if (config.isBlitzMode()) {

      return new BlitzMatch(agonBoard, white, black, config.getTimeout());
    } else {
      return new StandardMatch(agonBoard, white, black);
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
      // C'est une IA selon la factory de ton collègue
      return new AiPlayer("IA_" + color, color, agonBoard, aiStrategy);
    } else {
      // C'est null, donc c'est un humain
      return new HumanPlayer("Joueur_" + color, color, gameUi);
    }
  }
}
