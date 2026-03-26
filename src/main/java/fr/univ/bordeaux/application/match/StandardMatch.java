package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.match.player.Player;

/**
 * TODO: complete here.
 */
public class StandardMatch extends Match {

  /**
   * Create default match where two players play together.
   *
   * @param agonBoard {@link AgonBoard} Board to play on.
   *
   * @param player1 {@link Player} first player.
   *
   * @param player2 {@link Player} second player.
   *
   */
  public StandardMatch(AgonBoard agonBoard, Player player1, Player player2) {
    super(agonBoard, player1, player2);
  }

  /**
   * Completed Actions when beginning a turn.
   */
  @Override
  public void startActions() {}

  /**
   * Actions done when a turn is about to end.
   */
  @Override
  public void endActions() {
    super.switchPlayer();
  }
}
