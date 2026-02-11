package fr.univ.bordeaux.agonCore.bitboard;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;

public interface AgonBoard extends RestrictedAgonBoard {

  boolean applyMove(Move move);

  boolean undoMove();

  boolean isGameWon(Color color);

  int getScore(Color color);
}