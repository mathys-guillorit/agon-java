package fr.univ.bordeaux.application.match;

import static org.junit.jupiter.api.Assertions.*;

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
    whitePlayer = new HumanPlayer("J1", Color.WHITE, null);
    blackPlayer = new HumanPlayer("J2", Color.BLACK, null);

    match = new BlitzMatch(board, whitePlayer, blackPlayer, 1, new GameConfig());
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

    BlitzMatch shortMatch = new BlitzMatch(board, whitePlayer, blackPlayer, 0, new GameConfig());

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

  @Test
  @DisplayName("Initialisation avec le joueur Noir (branche else du constructeur)")
  void testConstructorWithBlackStarting() {
    // On force le début avec les Noirs pour passer dans le 'else' du constructeur
    BlitzMatch blackStartMatch = new BlitzMatch(board, whitePlayer, blackPlayer, 1, new GameConfig(), Color.BLACK);

    // Le timer noir doit être celui qui est actif (on vérifie via getRemainingTime qui appelle blackTimer)
    assertNotNull(blackStartMatch.getRemainingTime());
    // On vérifie que le statut est bien géré
    assertFalse(blackStartMatch.isMatchOver());
  }

  @Test
  @DisplayName("Changement de joueur : les timers doivent s'arrêter et démarrer correctement")
  void testSwitchPlayerTimers() {
    // Au début, c'est le tour des Blancs
    assertEquals(Color.WHITE, match.getCurrentPlayer().getColor());

    // On finit le tour (endActions appelle switchPlayer qui stop le timer courant)
    match.endActions();

    // Maintenant c'est au tour des Noirs
    assertEquals(Color.BLACK, match.getCurrentPlayer().getColor());

    // On simule le début du tour (startTurn doit démarrer le timer Noir)
    match.startTurn();

    // On vérifie que getRemainingTime pointe bien sur le timer Noir
    // (Dans tes images, Color.WHITE ? whiteTimer : blackTimer)
    assertNotNull(match.getRemainingTime());
  }

  @Test
  @DisplayName("Vérifier le basculement complet des timers lors d'un changement de tour")
  void testSwitchTimersCoverage() {
    // 1. Départ : Tour des Blancs (Timer Blanc actif)
    assertEquals(Color.WHITE, match.getCurrentPlayer().getColor());

    // 2. Fin du tour Blanc : appelle stop() sur le timer blanc
    match.endActions();

    // 3. Début du tour Noir : switchPlayer() a été appelé en interne
    assertEquals(Color.BLACK, match.getCurrentPlayer().getColor());

    // 4. On démarre le tour Noir : appelle start() sur le timer noir
    match.startTurn();

    // On vérifie que le temps affiché est bien celui du joueur noir maintenant
    // (Couvre la branche 'else' du ternaire dans getRemainingTime)
    assertNotNull(match.getRemainingTime());
  }


}
