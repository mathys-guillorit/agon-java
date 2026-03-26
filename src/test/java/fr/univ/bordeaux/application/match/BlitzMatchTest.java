package fr.univ.bordeaux.application.match;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.application.match.player.Player;
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
    whitePlayer = new HumanPlayer("J1", Color.WHITE, null);
    blackPlayer = new HumanPlayer("J2", Color.BLACK, null);

    match = new BlitzMatch(board, whitePlayer, blackPlayer, 1);
  }

  @Test
  @DisplayName("Le timer du joueur courant doit être actif")
  void testTimerStatus() {
    assertTrue(match.isRunning(), "Le match doit être en cours");
    String time = match.getRemainingTime();
    assertNotNull(time);
    assertTrue(time.startsWith("01:00") || time.startsWith("00:59"));
  }

  @Test
  @DisplayName("Le match se termine quand le temps est écoulé")
  void testTimeoutRealCondition() throws InterruptedException {

    BlitzMatch shortMatch = new BlitzMatch(board, whitePlayer, blackPlayer, 0);

    Thread.sleep(200);

    shortMatch.startActions();

    assertEquals(
        MatchStatus.FINISHED,
        shortMatch.getMatchStatus(),
        "Le match devrait être fini car le timer est à 0");
  }

  @Test
  @DisplayName("Vérifier que le temps diminue réellement avec le passage des millisecondes")
  void testTimeElapsing() throws InterruptedException {

    String timeAtStart = match.getRemainingTime();

    Thread.sleep(1200);

    String timeAfterWait = match.getRemainingTime();

    assertNotSame(
        timeAtStart,
        timeAfterWait,
        "Le temps restant ("
            + timeAfterWait
            + ") devrait être inférieur au temps de départ ("
            + timeAtStart
            + ")");
  }
}
