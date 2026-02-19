package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;

public class StandardMatch extends Match {
  private AgonBoard agonBoard;
  public StandardMatch(AgonBoard agonBoard) {
    this.agonBoard = agonBoard;
  }
}
