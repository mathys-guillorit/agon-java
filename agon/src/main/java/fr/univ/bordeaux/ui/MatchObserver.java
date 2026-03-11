package fr.univ.bordeaux.ui;

import fr.univ.bordeaux.agonCore.bitboard.RestrictedAgonBoard;

public interface MatchObserver {
  void updateBoard(RestrictedAgonBoard agonBoard);
}
