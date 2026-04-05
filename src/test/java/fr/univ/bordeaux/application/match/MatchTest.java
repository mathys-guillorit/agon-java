package fr.univ.bordeaux.application.match;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agoncore.bitboard.BitBoard;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchTest {

  private AgonBoardImpl board;
  private Player p1;
  private Player p2;
  private TestMatch match;

  // Sous-classe concrète pour tester la classe abstraite Match
  private static class TestMatch extends Match {
    public boolean startCalled = false;
    public boolean endCalled = false;

    public TestMatch(AgonBoardImpl board, Player p1, Player p2) {
      super(board, p1, p2, new GameConfig());
    }

    @Override
    public void startActions() {
      startCalled = true;
    }

    @Override
    public void endActions() {
      endCalled = true;
      this.switchPlayer(); // On simule un comportement standard
    }
  }

  @BeforeEach
  void setUp() {
    board = new AgonBoardImpl();
    board.initBaseConfiguration();
    p1 = new HumanPlayer("Blanc", Color.WHITE, null);
    p2 = new HumanPlayer("Noir", Color.BLACK, null);
    match = new TestMatch(board, p1, p2);
  }

  @Test
  @DisplayName("Initialisation : Le joueur blanc doit commencer")
  void testInitialPlayer() {
    assertEquals(Color.WHITE, match.getCurrentPlayer().getColor());
    assertTrue(match.isRunning());
  }

  @Test
  @DisplayName("Move : Un mouvement valide doit changer de joueur")
  void testValidMove() {
    // Pion blanc en K10 (index 120) vers J10 (index 109)
    int from = CoordinateMapper.toIndex('K', 10);
    int to = CoordinateMapper.toIndex('J', 10);
    Move move = new Move(from, to, Color.WHITE);

    boolean result = match.move(move);

    assertTrue(result, "Le mouvement devrait être valide");
    assertTrue(match.endCalled, "endActions devrait être appelé");
    assertEquals(
        Color.BLACK, match.getCurrentPlayer().getColor(), "Le joueur devrait avoir changé");
  }

  @Test
  @DisplayName("Move : On ne peut pas bouger une pièce adverse")
  void testInvalidPieceColor() {
    // On essaie de bouger un pion noir (K7) alors que c'est au blanc
    int from = CoordinateMapper.toIndex('K', 7);
    int to = CoordinateMapper.toIndex('J', 7);
    Move move = new Move(from, to, Color.BLACK);

    boolean result = match.move(move);

    assertFalse(result, "On ne doit pas pouvoir bouger les pièces de l'adversaire");
    assertEquals(Color.WHITE, match.getCurrentPlayer().getColor(), "Le joueur ne doit pas changer");
  }

  @Test
  @DisplayName("Undo/Redo : Vérification de la cohérence de l'historique")
  void testUndoRedo() {
    // 1. Faire deux mouvements
    match.move(
        new Move(CoordinateMapper.toIndex('B', 1), CoordinateMapper.toIndex('C', 1), Color.WHITE));
    match.move(
        new Move(CoordinateMapper.toIndex('B', 7), CoordinateMapper.toIndex('C', 7), Color.BLACK));

    // 2. Undo (doit annuler 2 undoMove() internes selon ton code)
    boolean undoRes = match.undo();
    assertTrue(undoRes);

    // 3. Redo (doit refaire 2 redoMove() internes)
    boolean redoRes = match.redo();
    // Attention : Si ton redo renvoie false, c'est le problème de pile vide qu'on a vu avant !
    // Mais ici, avec 2 moves réels, ça devrait passer si la pile est bien de taille 2.
    assertTrue(redoRes, "Le redo devrait fonctionner si 2 moves ont été annulés");
  }

  @Test
  @DisplayName("Quit : Le match doit s'arrêter")
  void testQuit() {
    match.quit();
    assertTrue(match.isMatchOver());
    assertFalse(match.isRunning());
  }

  @Test
  @DisplayName("Hint : L'IA doit suggérer un mouvement")
  void testHint() {
    Move hint = match.hint();
    assertNotNull(hint, "L'IA devrait proposer un coup");
    assertEquals(Color.WHITE, hint.getColor(), "Le coup suggéré doit être pour le joueur actuel");
  }

  @Test
  @DisplayName("History : getHistory doit renvoyer des MoveDTO")
  void testGetHistory() {
    match.move(
        new Move(CoordinateMapper.toIndex('B', 1), CoordinateMapper.toIndex('C', 1), Color.WHITE));

    List<MoveDtO> history = match.getHistory();
    assertFalse(history.isEmpty());
    assertEquals("b1", history.get(0).from().toLowerCase());
  }

  @Test
  @DisplayName("Branche : Move impossible si le match est déjà FINISHED")
  void testMoveWhenFinished() {
    match.quit(); // Force l'état à FINISHED

    Move move =
        new Move(CoordinateMapper.toIndex('B', 1), CoordinateMapper.toIndex('C', 1), Color.WHITE);
    boolean result = match.move(move);

    assertFalse(result, "Le move doit échouer car le match est fini");
  }

  @Test
  @DisplayName("Branche : Move impossible si la pièce appartient à l'adversaire")
  void testMoveOpponentPiece() {
    // Supposons que Blanc commence. On essaie de bouger un pion NOIR.
    int fromBlack = CoordinateMapper.toIndex('K', 7);
    int to = CoordinateMapper.toIndex('J', 7);
    Move move = new Move(fromBlack, to, Color.WHITE); // Le move dit que c'est le Blanc qui joue

    boolean result = match.move(move);

    assertFalse(result, "Le move doit échouer car la pièce en K7 est noire");
  }

  @Test
  @DisplayName("Branche : Le match passe en FINISHED en cas de victoire")
  void testGameWinStatus() {
    BitBoard queenw = new BitBoard();
    BitBoard pawnw = new BitBoard();
    queenw.setBit(60, 1L);
    pawnw.setBit(59, 1L);
    pawnw.setBit(61, 1L);
    pawnw.setBit(CoordinateMapper.toIndex('G', 6), 1L);
    pawnw.setBit(CoordinateMapper.toIndex('G', 7), 1L);
    pawnw.setBit(CoordinateMapper.toIndex('E', 5), 1L);
    pawnw.setBit(CoordinateMapper.toIndex('E', 7), 1L);
    AgonBoardImpl board = new AgonBoardImpl(queenw, new BitBoard(), pawnw, new BitBoard());
    p1 = new HumanPlayer("Blanc", Color.WHITE, null);
    p2 = new HumanPlayer("Noir", Color.BLACK, null);
    TestMatch match1 = new TestMatch(board, p1, p2);
    Move winningMove =
        new Move(
            CoordinateMapper.toIndex('E', 7),
            CoordinateMapper.toIndex('E', 6),
            Color.WHITE); // Ton coup gagnant
    match1.move(winningMove);

    assertEquals(MatchStatus.FINISHED, match1.getMatchStatus(), "Le status doit être FINISHED");
    assertTrue(match1.isMatchOver());
  }

  @Test
  @DisplayName("Branche : Retourne false si applyMove échoue (règles Agon non respectées)")
  void testApplyMoveFails() {
    // Un pion blanc ne peut pas reculer.
    // Si on essaie de le faire reculer, applyMove renverra false.
    int from = CoordinateMapper.toIndex('C', 2);
    int to = CoordinateMapper.toIndex('C', 1); // Recul interdit
    Move illegalMove = new Move(from, to, Color.WHITE);

    boolean result = match.move(illegalMove);

    assertFalse(result, "Le move doit renvoyer false car applyMove a refusé le mouvement");
  }

  @Test
  void testPause() {
    assertFalse(match.pause());
  }

  @Test
  void testRemainingTime() {
    assertNull(match.getCurrentPlayerRemainingTime());
  }
}
