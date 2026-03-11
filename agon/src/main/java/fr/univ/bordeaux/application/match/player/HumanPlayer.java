package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.UIPromptParser;

public class HumanPlayer extends AbstractPlayer{
  GameUserInterface ui;
  public HumanPlayer(String name, Color color, GameUserInterface ui) {
    super(name,color);
    this.ui=ui;
  }

  @Override
  public Color getColor() {
    return color;
  }

  @Override
  public CmdAction getAction() {
    String input = ui.getLine();
    System.out.println(input);
    UIPromptParser parser = new UIPromptParser(ui.getUserPrompt());

    if (!parser.parse(input)){
      System.out.println("Invalid input");
      return null;}

    String cmdName = parser.getUserCmdName();
    String[] options = parser.getUserOptions();

    // On cherche dans le registre d'AgonShell
    return ui.getCmds().get(cmdName)
        .map(prototype -> prototype.createNew(options)) // Prototype !
        .orElseGet(() -> handleDefault(cmdName, options));
  }

  private CmdAction handleDefault(String cmdName, String[] options) {
    // 1. Toujours passer en majuscule pour éviter le décalage ASCII des minuscules
    String input = cmdName.toUpperCase();

    // 2. Utiliser un Regex ou une analyse plus fine si tu as des colonnes à 2 chiffres (10, 11)
    // Mais restons simple si tu n'as que 1 chiffre pour l'instant :
    if (input.length() < 4) return null;

    char letterFrom = input.charAt(0);
    // On convertit le char '5' en int 5 proprement
    int colFrom = Character.getNumericValue(input.charAt(1));

    char letterTo = input.charAt(2);
    int colTo = Character.getNumericValue(input.charAt(3));

    System.out.println("Move from " + letterFrom + colFrom + " to " + letterTo + colTo);

    int indexFrom = CoordinateMapper.toIndex(letterFrom, colFrom);
    int indexTo = CoordinateMapper.toIndex(letterTo, colTo);

    return new CmdMove(new Move(indexFrom, indexTo, this.color));
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isAI() {
    return false;
  }
}
