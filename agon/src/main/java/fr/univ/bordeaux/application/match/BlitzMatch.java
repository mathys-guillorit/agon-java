package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.match.player.Player;

public class BlitzMatch extends Match {

  private final GameTimer whiteTimer;
  private final GameTimer blackTimer;

  public BlitzMatch(
      AgonBoard agonBoard,
      Player player1,
      Player player2,
      long initialWhiteTime,
      long initialBlackTime2) {
    super(agonBoard, player1, player2);

    this.whiteTimer = new GameTimer(initialWhiteTime);
    this.blackTimer = new GameTimer(initialBlackTime2);
  }

  private GameTimer getCurrentTimer() {
    return (super.getCurrentPlayer().getColor() == Color.WHITE) ? whiteTimer : blackTimer;
  }

  @Override
  public void startActions() {
    getCurrentTimer().start();
    if (getCurrentTimer().isExpired()) {
      handleTimeout();
    }
  }

  private void handleTimeout() {
    super.setMatchStatus(MatchStatus.FINISHED);
  }

  @Override
  public void endActions() {
    // Le coup est fini, on arrête le chrono
    getCurrentTimer().stop();
  }

  public void startGame() {

  }
}
