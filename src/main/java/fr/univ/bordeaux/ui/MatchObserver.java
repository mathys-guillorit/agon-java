package fr.univ.bordeaux.ui;

import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;

public interface MatchObserver {

  void updateBoard(RestrictedAgonBoard agonBoard);
}
