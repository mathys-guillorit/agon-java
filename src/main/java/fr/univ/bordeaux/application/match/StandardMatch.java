package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.match.player.Player;

public class StandardMatch extends Match {

  public StandardMatch(AgonBoard agonBoard, Player player1, Player player2) {
    super(agonBoard, player1, player2);
  }

  @Override
  public void startActions() {}

  @Override
  public void endActions() {
    super.switchPlayer();
  }
}
