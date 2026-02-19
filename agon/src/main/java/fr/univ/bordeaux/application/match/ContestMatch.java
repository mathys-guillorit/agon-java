package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;

public class ContestMatch extends Match {
  private AgonBoard agonBoard;
  public ContestMatch(AgonBoard agonBoard) {
    this.agonBoard = agonBoard;
  }
}
