package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.application.commands.CmdAction;

public interface Player {
  CmdAction getAction();
  String getName();

  Color getColor();

  boolean isAI();
}
