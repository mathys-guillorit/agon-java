package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;

public class HumanPlayer extends AbstractPlayer {

  public HumanPlayer(String name, Color color) {
    super(name, color);
  }

  @Override
  public Color getColor() {
    return color;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isAI() {
    return false;
  }

  @Override
  public Move play() {
    return null;
  }
}
