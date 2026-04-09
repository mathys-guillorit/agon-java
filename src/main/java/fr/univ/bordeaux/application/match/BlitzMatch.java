package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.utils.GameLogger;

/**
 * Represents a match played in Blitz mode, where each player has a limited amount of time.
 *
 * <p>This class extends {@link Match} by adding management for two distinct timers. If a timer
 * expires, the match ends immediately, and the opposing player is declared the winner by timeout.
 */
public class BlitzMatch extends Match {

  /** The timer dedicated to the White player. */
  private final GameTimer whiteTimer;

  /** The timer dedicated to the Black player. */
  private final GameTimer blackTimer;

  private boolean isPaused = false;

  /**
   * Constructs a BlitzMatch with the specified board, players, and time limit. By default, the
   * White player starts the game.
   *
   * @param agonBoard The board used for the match.
   * @param player1 The first player (White).
   * @param player2 The second player (Black).
   * @param time The time limit for each player in minutes.
   * @param gameConfig The global game configuration.
   */
  public BlitzMatch(
      AgonBoard agonBoard, Player player1, Player player2, long time, GameConfig gameConfig) {
    this(agonBoard, player1, player2, time, gameConfig, Color.WHITE);
  }

  /**
   * Constructs a full BlitzMatch by specifying the starting player color.
   *
   * @param agonBoard The board used for the match.
   * @param player1 The first player.
   * @param player2 The second player.
   * @param time The time limit for each player in minutes.
   * @param gameConfig The game configuration.
   * @param startingColor The color of the player who starts the match.
   */
  public BlitzMatch(
      AgonBoard agonBoard,
      Player player1,
      Player player2,
      long time,
      GameConfig gameConfig,
      Color startingColor) {
    super(agonBoard, player1, player2, gameConfig, startingColor);

    this.whiteTimer = new GameTimer(time, this::handleTimeout);
    this.blackTimer = new GameTimer(time, this::handleTimeout);

    GameLogger.info("BlitzMatch: Starting new blitz game with " + time + " minutes per player.");

    if (startingColor == Color.WHITE) {
      GameLogger.debug("BlitzMatch: Starting White timer.");
      this.whiteTimer.start();
    } else {
      GameLogger.debug("BlitzMatch: Starting Black timer.");
      this.blackTimer.start();
    }
  }

  /**
   * Retrieves the timer corresponding to the player whose turn it currently is.
   *
   * @return The {@link GameTimer} of the current player.
   */
  private GameTimer getCurrentTimer() {
    return (super.getCurrentPlayer().getColor() == Color.WHITE) ? whiteTimer : blackTimer;
  }

  /**
   * * Actions to perform at the start of a turn.
   *
   * <p>Checks if the match is finished, ensures the current timer is running, and handles immediate
   * expiration if necessary.
   */
  @Override
  public void startActions() {
    if (this.getMatchStatus() == MatchStatus.FINISHED) {
      return;
    }
    if (!isPaused && !getCurrentTimer().isRunning()) {
      GameLogger.debug("BlitzMatch: Resuming " + super.getCurrentPlayer().getColor() + " timer.");
      getCurrentTimer().start();
    }
    if (getCurrentTimer().isExpired()) {
      handleTimeout();
    }
  }

  /**
   * Pauses the current player's timer.
   *
   * @return {@code true} if the timer was successfully stopped.
   */
  public boolean pause() {
    isPaused = !isPaused;
    if (isPaused) {
      GameLogger.info("BlitzMatch: Game paused.");
      this.getCurrentTimer().stop();
    } else {
      this.getCurrentTimer().start();
    }
    return true;
  }

  /**
   * * Handles the expiration of a player's timer.
   *
   * <p>This method is synchronized to prevent state conflicts. It sets the match status to
   * FINISHED, assigns the winner based on who timed out, terminates timer threads, and notifies the
   * UI.
   */
  private synchronized void handleTimeout() {
    if (this.getMatchStatus() != MatchStatus.FINISHED) {
      GameLogger.info(
          "BlitzMatch: TIMEOUT! " + this.getCurrentPlayer().getColor() + " ran out of time.");

      this.setMatchStatus(MatchStatus.FINISHED);
      this.setWinner(
          (this.getCurrentPlayer().getColor() == Color.BLACK)
              ? getWhitePlayer()
              : getBlackPlayer());

      whiteTimer.kill();
      blackTimer.kill();
      GameLogger.debug("BlitzMatch: Timer threads terminated.");
      this.notifyUi();
    }
  }

  /**
   * * Actions to perform at the end of a turn.
   *
   * <p>Stops the timers if the match is finished; otherwise, switches to the next player.
   */
  @Override
  public void endActions() {
    if (this.getMatchStatus() == MatchStatus.FINISHED) {
      GameLogger.debug("BlitzMatch: Stopping all timers (Match Finished).");
      whiteTimer.stop();
      blackTimer.stop();
      return;
    }
    this.switchPlayer();
  }

  /**
   * * Switches the current player while managing timer transitions.
   *
   * <p>Stops the current player's clock before invoking the superclass player switch logic.
   */
  @Override
  public void switchPlayer() {
    Color previous = super.getCurrentPlayer().getColor();
    getCurrentTimer().stop();

    super.switchPlayer();

    Color current = super.getCurrentPlayer().getColor();
    GameLogger.debug("BlitzMatch: Switched timer from " + previous + " to " + current);
  }

  /** Starts the current player's timer at the beginning of their gameplay phase. */
  @Override
  public void startTurn() {
    if (!isPaused) {
      GameLogger.debug("BlitzMatch: Starting turn for " + super.getCurrentPlayer().getColor());
      getCurrentTimer().start();
    }
  }

  /**
   * Gets the formatted remaining time for the current player.
   *
   * @return A string representing the remaining time (format mm:ss).
   */
  @Override
  public String getCurrentPlayerRemainingTime() {
    return (getCurrentPlayer().getColor() == Color.WHITE)
        ? whiteTimer.getFormattedRemainingTime()
        : blackTimer.getFormattedRemainingTime();
  }

  /**
   * Gets the formatted remaining time for all players. * @return An array of strings with remaining
   * times.
   */
  public String[] getAllPlayersRemainingTime() {
    return new String[] {
      whiteTimer.getFormattedRemainingTime(), blackTimer.getFormattedRemainingTime()
    };
  }
}
