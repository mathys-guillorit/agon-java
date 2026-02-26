package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agonCore.agonElements.Color;

public interface Player {
  String getName();
  Color getColor();
  boolean isAI();
}
