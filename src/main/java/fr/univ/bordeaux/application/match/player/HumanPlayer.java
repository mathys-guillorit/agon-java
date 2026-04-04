package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.UiPromptParser;

/** Default class for Human Behavior. */
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
  public CmdAction getAction(AgonRegister<CmdAction> cmds) {
    String input = ui.getUserInput();
    if (input == null || input.trim().isEmpty()) {
      return null;
    }
    // Utilisation static du parseur
    return UiPromptParser.parse(input, cmds, ui);
  }

  @Override
  public String getName() {
    return name;
  }
}
