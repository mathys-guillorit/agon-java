package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agonCore.agonElements.Move;

public interface MatchManager {
  boolean move(Move move);
  void startGame();
}
