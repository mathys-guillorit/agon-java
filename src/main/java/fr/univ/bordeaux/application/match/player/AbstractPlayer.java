package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;

/** Player Default class. */
public abstract class AbstractPlayer implements Player {

  String name;
  Color color;

  public AbstractPlayer(String name, Color color) {
    this.name = name;
    this.color = color;
  }

  public abstract CmdAction getAction(AgonRegister<CmdAction> cmds);
}
