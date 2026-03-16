package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.UIPromptParser;

public class HumanPlayer extends AbstractPlayer {

  GameUserInterface ui;

  public HumanPlayer(String name, Color color, GameUserInterface ui) {
    super(name, color);
    this.ui = ui;
  }

  @Override
  public Color getColor() {
    return color;
  }

  @Override
  public CmdAction getAction() {
    String input = ui.getUserInput();
    if (input == null || input.trim().isEmpty()) {
      return null;
    }
    // Utilisation static du parseur
    return UIPromptParser.parse(input, ui.getCmds());

  }
/*
    // Si aucune commande n'est trouvée, on tente de parser un mouvement court (ex: A1B2)
    return handleDefault(input);
  }

  private CmdAction handleDefault(String input) {
    input = input.trim().toUpperCase();
    if (input.length() < 4) {
      return null;
    }

    try {
      char letterFrom = input.charAt(0);
      int colFrom = Character.getNumericValue(input.charAt(1));

      char letterTo = input.charAt(2);
      int colTo = Character.getNumericValue(input.charAt(3));

      int indexFrom = CoordinateMapper.toIndex(letterFrom, colFrom);
      int indexTo = CoordinateMapper.toIndex(letterTo, colTo);

      return new CmdMove(new Move(indexFrom, indexTo, this.color));
    } catch (Exception e) {
      return null;
    }
  }
*/

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isAI() {
    return false;
  }
}
