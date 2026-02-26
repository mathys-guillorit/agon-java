package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;

public abstract class AbstractPlayer implements Player {
  String name;
  Color color;

  public AbstractPlayer(String name,Color color){
    this.name=name;
    this.color=color;
  }
  public abstract Move play();
}
