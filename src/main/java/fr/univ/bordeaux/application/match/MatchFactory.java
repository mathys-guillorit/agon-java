package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.ai.strategy.AIFactory;
import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAI;
import fr.univ.bordeaux.application.ai.strategy.AgonAI;
import fr.univ.bordeaux.application.match.player.AiPlayer;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.util.Map;

public class MatchFactory {

  public static Match createMatch(GameConfig config, GameUserInterface gameUI) {
    // 1. On récupère la map des cerveaux IA (certaines couleurs seront null)
    Map<Color, AbstractAgonAI> aiMap = AIFactory.createAiMap(config);
    AgonBoard agonBoard = new AgonBoardImpl();
    agonBoard.initBaseConfiguration();

    // 2. On crée les vrais objets Player
    Player white = createPlayerFromAiMap(aiMap, Color.WHITE, agonBoard, gameUI);
    Player black = createPlayerFromAiMap(aiMap, Color.BLACK, agonBoard, gameUI);
    System.out.println("je suis dans match factory et p2isIA : " + black.isAI());
    /*if (config.isBlitzMode()) {
      // On récupère les deux temps distincts dans la config
      long whiteTime = config.getWhiteInitialTime();
      long blackTime = config.getBlackInitialTime();

      return new BlitzMatch(agonBoard,white, black, whiteTime, blackTime);
    }
    else if (mode.equals("contest")) {
      return new ContestMatch(agonBoard,white, black);
    } else {*/
    return new StandardMatch(agonBoard, white, black);
  }

  private static Player createPlayerFromAiMap(Map<Color, AbstractAgonAI> aiMap, Color color,
      AgonBoard agonBoard,
      GameUserInterface gameUI) {
    AgonAI aiStrategy = aiMap.get(color);
    System.out.println((aiStrategy==null));
    if (aiStrategy != null) {
      // C'est une IA selon la factory de ton collègue
      return new AiPlayer("IA_" + color, color, agonBoard, aiStrategy);
    } else {
      // C'est null, donc c'est un humain
      return new HumanPlayer("Joueur_" + color, color, gameUI);
    }
  }
}
