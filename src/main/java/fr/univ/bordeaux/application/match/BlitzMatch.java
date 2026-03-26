package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.match.player.Player;

/** Represents a match played in Blitz mode, where each player has a limited amount of time. */
public class BlitzMatch extends Match {

  /** Represents a Timer for the WhitePlayer. */
  private final GameTimer whiteTimer;

  /** Represents a Timer for the BlackPlayer. */
  private final GameTimer blackTimer;

  /**
   * Constructs a BlitzMatch with the specified board, players, and time limit.
   *
   * @param agonBoard The board used for the match.
   * @param player1 The first player.
   * @param player2 The second player.
   * @param time The time limit for each player in minutes.
   */
  public BlitzMatch(AgonBoard agonBoard, Player player1, Player player2, long time) {
    super(agonBoard, player1, player2);

    this.whiteTimer = new GameTimer(time, () -> {});
    this.blackTimer = new GameTimer(time, () -> {});
    this.whiteTimer.start();
  }

  /**
   * Gets the timer for the current player.
   *
   * @return The current player's GameTimer.
   */
  private GameTimer getCurrentTimer() {
    return (super.getCurrentPlayer().getColor() == Color.WHITE) ? whiteTimer : blackTimer;
  }

  /** Actions to perform at the start of a turn, specifically managing the timer. */
  @Override
  public void startActions() {
    if (!getCurrentTimer().isRunning()) {
      getCurrentTimer().start();
    }
    if (getCurrentTimer().isExpired()) {
      handleTimeout();
    }
  }

  /**
   * Pauses the current player's timer.
   *
   * @return true if the timer was successfully paused.
   */
  public boolean pause() {
    this.getCurrentTimer().stop();
    return true;
  }

  /** Handles the expiration of a player's timer. */
  private void handleTimeout() {
    super.setMatchStatus(MatchStatus.FINISHED);
  }

  /** Actions to perform at the end of a turn, specifically switching players. */
  @Override
  public void endActions() {
    this.switchPlayer();
  }

  /** Switches the current player and manages the timers accordingly. */
  public void switchPlayer() {
    getCurrentTimer().stop();
    super.switchPlayer();
    getCurrentTimer().start();
  }

  /**
   * Gets the formatted remaining time for the current player.
   *
   * @return A string representing the remaining time (mm:ss).
   */
  @Override
  public String getRemainingTime() {
    return (getCurrentPlayer().getColor() == Color.WHITE)
        ? whiteTimer.getFormattedRemainingTime()
        : blackTimer.getFormattedRemainingTime();
  }
}
