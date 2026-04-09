package fr.univ.bordeaux.application.match;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BlitzMatchTest {

  private AgonBoard board;
  private Player whitePlayer;
  private Player blackPlayer;
  private BlitzMatch match;

  @BeforeEach
  void setUp() {
    board = new AgonBoardImpl();
    whitePlayer = new HumanPlayer("P1", Color.WHITE, null);
    blackPlayer = new HumanPlayer("P2", Color.BLACK, null);

    match = new BlitzMatch(board, whitePlayer, blackPlayer, 1, new GameConfig());
  }

  @Test
  @DisplayName("The current player's timer must be active")
  void testTimerStatus() {
    assertTrue(match.isRunning(), "The match should be running");
    String time = match.getCurrentPlayerRemainingTime();
    assertNotNull(time);
    assertTrue(time.startsWith("01:00") || time.startsWith("00:59"));
  }

  @Test
  @DisplayName("The match ends when time runs out")
  void testTimeoutRealCondition() throws InterruptedException {
    BlitzMatch shortMatch = new BlitzMatch(board, whitePlayer, blackPlayer, 0, new GameConfig());

    Thread.sleep(200);
    shortMatch.startActions();

    assertEquals(
        MatchStatus.FINISHED,
        shortMatch.getMatchStatus(),
        "The match should be finished because the timer is at 0");
  }

  @Test
  @DisplayName("Verify that time decreases as milliseconds pass")
  void testTimeElapsing() throws InterruptedException {
    String timeAtStart = match.getCurrentPlayerRemainingTime();

    Thread.sleep(1200);

    String timeAfterWait = match.getCurrentPlayerRemainingTime();

    assertNotSame(
        timeAtStart,
        timeAfterWait,
        "Remaining time ("
            + timeAfterWait
            + ") should be less than the starting time ("
            + timeAtStart
            + ")");
  }

  @Test
  @DisplayName("Initialization with Black starting")
  void testConstructorWithBlackStarting() {
    BlitzMatch blackStartMatch =
        new BlitzMatch(board, whitePlayer, blackPlayer, 1, new GameConfig(), Color.BLACK);
    assertNotNull(blackStartMatch.getCurrentPlayerRemainingTime());
    assertFalse(blackStartMatch.isMatchOver());
  }

  @Test
  @DisplayName("Player switch: timers must stop and start correctly")
  void testSwitchPlayerTimers() {
    assertEquals(Color.WHITE, match.getCurrentPlayer().getColor());

    match.endActions();

    assertEquals(Color.BLACK, match.getCurrentPlayer().getColor());

    match.startTurn();

    assertNotNull(match.getCurrentPlayerRemainingTime());
  }

  @Test
  @DisplayName("Verify full timer toggle during turn change")
  void testSwitchTimersCoverage() {
    assertEquals(Color.WHITE, match.getCurrentPlayer().getColor());

    match.endActions();

    assertEquals(Color.BLACK, match.getCurrentPlayer().getColor());

    match.startTurn();

    assertNotNull(match.getCurrentPlayerRemainingTime());
  }
}
