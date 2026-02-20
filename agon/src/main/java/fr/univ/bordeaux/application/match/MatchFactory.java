package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoardImpl;

public class MatchFactory {

  public static Match createMatch(String matchType) {
    AgonBoard agonBoard = new AgonBoardImpl();
    return switch (matchType) {
      case "Blitz" -> new BlitzMatch(agonBoard);
      case "Contest" -> new ContestMatch(agonBoard);
      case "Standard" -> new StandardMatch(agonBoard);
      default -> throw new IllegalArgumentException("Invalid match type");
    };
  }
}
