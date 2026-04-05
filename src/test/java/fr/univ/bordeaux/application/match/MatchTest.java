package fr.univ.bordeaux.application.match;

import static org.junit.jupiter.api.Assertions.*;

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
      this.switchPlayer();
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
  @DisplayName("Initialisation : le joueur blanc doit commencer")
  void testInitialPlayer() {
    assertEquals(Color.WHITE, match.getCurrentPlayer().getColor());
    assertTrue(match.isRunning());
    assertFalse(match.isMatchOver());
    assertEquals(MatchStatus.RUNNING, match.getMatchStatus());
  }

  @Test
  @DisplayName("Getters : getwhitePlayer et getblackPlayer doivent retourner les bons joueurs")
  void testGetWhiteAndBlackPlayer() {
    assertEquals(p1, match.getWhitePlayer());
    assertEquals(p2, match.getBlackPlayer());

    assertEquals(Color.WHITE, match.getWhitePlayer().getColor());
    assertEquals(Color.BLACK, match.getBlackPlayer().getColor());
  }

  @Test
  @DisplayName(
      "Getters : getwhitePlayer et getblackPlayer fonctionnent même si les joueurs sont inversés")
  void testGetWhiteAndBlackPlayerReversed() {
    Player blackFirst = new HumanPlayer("Noir", Color.BLACK, null);
    Player whiteSecond = new HumanPlayer("Blanc", Color.WHITE, null);

    TestMatch reversedMatch = new TestMatch(board, blackFirst, whiteSecond);

    assertEquals(whiteSecond, reversedMatch.getWhitePlayer());
    assertEquals(blackFirst, reversedMatch.getBlackPlayer());

    assertEquals(Color.WHITE, reversedMatch.getWhitePlayer().getColor());
    assertEquals(Color.BLACK, reversedMatch.getBlackPlayer().getColor());
  }

  @Test
  @DisplayName("Winner : getWinner doit renvoyer null tant que le match n'est pas terminé")
  void testGetWinnerInitiallyNull() {
    assertNull(match.getWinner());
  }

  @Test
  @DisplayName("Move : un mouvement valide doit changer de joueur")
  void testValidMove() {
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
  @DisplayName("Move : on ne peut pas bouger une pièce adverse")
  void testInvalidPieceColor() {
    int from = CoordinateMapper.toIndex('K', 7);
    int to = CoordinateMapper.toIndex('J', 7);
    Move move = new Move(from, to, Color.BLACK);

    boolean result = match.move(move);

    assertFalse(result, "On ne doit pas pouvoir bouger les pièces de l'adversaire");
    assertEquals(Color.WHITE, match.getCurrentPlayer().getColor(), "Le joueur ne doit pas changer");
  }

  @Test
  @DisplayName("Branche : move impossible si le match est déjà FINISHED")
  void testMoveWhenFinished() {
    match.quit();

    Move move =
        new Move(CoordinateMapper.toIndex('B', 1), CoordinateMapper.toIndex('C', 1), Color.WHITE);

    boolean result = match.move(move);

    assertFalse(result, "Le move doit échouer car le match est fini");
  }

  @Test
  @DisplayName("Branche : move impossible si la pièce appartient à l'adversaire")
  void testMoveOpponentPiece() {
    int fromBlack = CoordinateMapper.toIndex('K', 7);
    int to = CoordinateMapper.toIndex('J', 7);
    Move move = new Move(fromBlack, to, Color.WHITE);

    boolean result = match.move(move);

    assertFalse(result, "Le move doit échouer car la pièce en K7 est noire");
  }

  @Test
  @DisplayName("Branche : retourne false si applyMove échoue")
  void testApplyMoveFails() {
    int from = CoordinateMapper.toIndex('C', 2);
    int to = CoordinateMapper.toIndex('C', 1);
    Move illegalMove = new Move(from, to, Color.WHITE);

    boolean result = match.move(illegalMove);

    assertFalse(result, "Le move doit renvoyer false car applyMove a refusé le mouvement");
  }

  @Test
  @DisplayName("Undo/Redo : vérification de la cohérence de l'historique")
  void testUndoRedo() {
    match.move(
        new Move(CoordinateMapper.toIndex('B', 1), CoordinateMapper.toIndex('C', 1), Color.WHITE));
    match.move(
        new Move(CoordinateMapper.toIndex('B', 7), CoordinateMapper.toIndex('C', 7), Color.BLACK));

    boolean undoRes = match.undo();
    assertTrue(undoRes, "Le undo devrait fonctionner après des coups joués");

    boolean redoRes = match.redo();
    assertTrue(redoRes, "Le redo devrait fonctionner si des coups ont été annulés");
  }

  @Test
  @DisplayName("Undo : retourne false si aucun coup n'a été joué")
  void testUndoWithoutHistory() {
    assertFalse(match.undo());
  }

  @Test
  @DisplayName("Redo : retourne false si aucun coup n'a été annulé")
  void testRedoWithoutUndo() {
    assertFalse(match.redo());
  }

  @Test
  @DisplayName("Quit : le match doit s'arrêter")
  void testQuit() {
    match.quit();
    assertTrue(match.isMatchOver());
    assertFalse(match.isRunning());
    assertEquals(MatchStatus.FINISHED, match.getMatchStatus());
  }

  @Test
  @DisplayName("Hint : l'IA doit suggérer un mouvement")
  void testHint() {
    Move hint = match.hint();
    assertNotNull(hint, "L'IA devrait proposer un coup");
    assertEquals(Color.WHITE, hint.getColor(), "Le coup suggéré doit être pour le joueur actuel");
  }

  @Test
  @DisplayName("History : getHistory doit être vide au début")
  void testGetHistoryInitiallyEmpty() {
    assertTrue(match.getHistory().isEmpty());
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
  @DisplayName("Branche : le match passe en FINISHED en cas de victoire")
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

    AgonBoardImpl customBoard = new AgonBoardImpl(queenw, new BitBoard(), pawnw, new BitBoard());
    Player white = new HumanPlayer("Blanc", Color.WHITE, null);
    Player black = new HumanPlayer("Noir", Color.BLACK, null);
    TestMatch customMatch = new TestMatch(customBoard, white, black);

    Move winningMove =
        new Move(CoordinateMapper.toIndex('E', 7), CoordinateMapper.toIndex('E', 6), Color.WHITE);

    customMatch.move(winningMove);

    assertEquals(
        MatchStatus.FINISHED, customMatch.getMatchStatus(), "Le status doit être FINISHED");
    assertTrue(customMatch.isMatchOver());
  }

  @Test
  @DisplayName("Winner : getWinner doit retourner le joueur gagnant après une victoire")
  void testGetWinnerAfterWin() {
    BitBoard queenw = new BitBoard();
    BitBoard pawnw = new BitBoard();

    queenw.setBit(60, 1L);
    pawnw.setBit(59, 1L);
    pawnw.setBit(61, 1L);
    pawnw.setBit(CoordinateMapper.toIndex('G', 6), 1L);
    pawnw.setBit(CoordinateMapper.toIndex('G', 7), 1L);
    pawnw.setBit(CoordinateMapper.toIndex('E', 5), 1L);
    pawnw.setBit(CoordinateMapper.toIndex('E', 7), 1L);

    AgonBoardImpl customBoard = new AgonBoardImpl(queenw, new BitBoard(), pawnw, new BitBoard());
    Player white = new HumanPlayer("Blanc", Color.WHITE, null);
    Player black = new HumanPlayer("Noir", Color.BLACK, null);
    TestMatch customMatch = new TestMatch(customBoard, white, black);

    Move winningMove =
        new Move(CoordinateMapper.toIndex('E', 7), CoordinateMapper.toIndex('E', 6), Color.WHITE);

    boolean result = customMatch.move(winningMove);

    assertTrue(result);
    assertEquals(MatchStatus.FINISHED, customMatch.getMatchStatus());
    assertEquals(white, customMatch.getWinner());
  }

  @Test
  @DisplayName("Pause : retourne false")
  void testPause() {
    assertFalse(match.pause());
  }

  @Test
  @DisplayName("Remaining time : retourne null")
  void testRemainingTime() {
    assertNull(match.getCurrentPlayerRemainingTime());
  }
}
