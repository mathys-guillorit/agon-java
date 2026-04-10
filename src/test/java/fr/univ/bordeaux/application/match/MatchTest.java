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

  /** Concrete subclass used to test the abstract Match class. */
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

    @Override
    public void startTurn() {}
  }

  @BeforeEach
  void setUp() {
    board = new AgonBoardImpl();
    board.initBaseConfiguration();
    p1 = new HumanPlayer("White", Color.WHITE, null);
    p2 = new HumanPlayer("Black", Color.BLACK, null);
    match = new TestMatch(board, p1, p2);
  }

  @Test
  @DisplayName("Initialization: the white player should start")
  void testInitialPlayer() {
    assertEquals(Color.WHITE, match.getCurrentPlayer().getColor());
    assertTrue(match.isRunning());
    assertFalse(match.isMatchOver());
    assertEquals(MatchStatus.RUNNING, match.getMatchStatus());
  }

  @Test
  @DisplayName("Getters: getWhitePlayer and getBlackPlayer should return the correct players")
  void testGetWhiteAndBlackPlayer() {
    assertEquals(p1, match.getWhitePlayer());
    assertEquals(p2, match.getBlackPlayer());

    assertEquals(Color.WHITE, match.getWhitePlayer().getColor());
    assertEquals(Color.BLACK, match.getBlackPlayer().getColor());
  }

  @Test
  @DisplayName(
      "Getters: getWhitePlayer and getBlackPlayer work even if players are swapped in constructor")
  void testGetWhiteAndBlackPlayerReversed() {
    Player blackFirst = new HumanPlayer("Black", Color.BLACK, null);
    Player whiteSecond = new HumanPlayer("White", Color.WHITE, null);

    TestMatch reversedMatch = new TestMatch(board, blackFirst, whiteSecond);

    assertEquals(whiteSecond, reversedMatch.getWhitePlayer());
    assertEquals(blackFirst, reversedMatch.getBlackPlayer());

    assertEquals(Color.WHITE, reversedMatch.getWhitePlayer().getColor());
    assertEquals(Color.BLACK, reversedMatch.getBlackPlayer().getColor());
  }

  @Test
  @DisplayName("Winner: getWinner should return null as long as the match is not over")
  void testGetWinnerInitiallyNull() {
    assertNull(match.getWinner());
  }

  @Test
  @DisplayName("Move: a valid move should switch players")
  void testValidMove() {
    int from = CoordinateMapper.toIndex('K', 10);
    int to = CoordinateMapper.toIndex('J', 10);
    Move move = new Move(from, to, Color.WHITE);

    boolean result = match.move(move);

    assertTrue(result, "The move should be valid");
    assertTrue(match.endCalled, "endActions should have been called");
    assertEquals(
        Color.BLACK, match.getCurrentPlayer().getColor(), "The player should have changed");
  }

  @Test
  @DisplayName("Move: cannot move an opponent's piece")
  void testInvalidPieceColor() {
    int from = CoordinateMapper.toIndex('K', 7);
    int to = CoordinateMapper.toIndex('J', 7);
    Move move = new Move(from, to, Color.BLACK);

    boolean result = match.move(move);

    assertFalse(result, "It should not be possible to move opponent pieces");
    assertEquals(Color.WHITE, match.getCurrentPlayer().getColor(), "The player should not change");
  }

  @Test
  @DisplayName("Branch: move impossible if match is already FINISHED")
  void testMoveWhenFinished() {
    match.quit();

    Move move =
        new Move(CoordinateMapper.toIndex('B', 1), CoordinateMapper.toIndex('C', 1), Color.WHITE);

    boolean result = match.move(move);

    assertFalse(result, "The move should fail because the match is over");
  }

  @Test
  @DisplayName("Branch: move impossible if the piece belongs to the opponent")
  void testMoveOpponentPiece() {
    int fromBlack = CoordinateMapper.toIndex('K', 7);
    int to = CoordinateMapper.toIndex('J', 7);
    Move move = new Move(fromBlack, to, Color.WHITE);

    boolean result = match.move(move);

    assertFalse(result, "The move should fail because the piece at K7 is black");
  }

  @Test
  @DisplayName("Branch: returns false if applyMove fails")
  void testApplyMoveFails() {
    int from = CoordinateMapper.toIndex('C', 2);
    int to = CoordinateMapper.toIndex('C', 1);
    Move illegalMove = new Move(from, to, Color.WHITE);

    boolean result = match.move(illegalMove);

    assertFalse(result, "The move should return false because applyMove refused the motion");
  }

  @Test
  @DisplayName("Undo/Redo: verification of history consistency")
  void testUndoRedo() {
    match.move(
        new Move(CoordinateMapper.toIndex('B', 1), CoordinateMapper.toIndex('C', 1), Color.WHITE));
    match.move(
        new Move(CoordinateMapper.toIndex('B', 7), CoordinateMapper.toIndex('C', 7), Color.BLACK));

    boolean undoRes = match.undo();
    assertTrue(undoRes, "Undo should work after moves are played");

    boolean redoRes = match.redo();
    assertTrue(redoRes, "Redo should work if moves have been undone");
  }

  @Test
  @DisplayName("Undo: returns false if no moves have been played")
  void testUndoWithoutHistory() {
    assertFalse(match.undo());
  }

  @Test
  @DisplayName("Redo: returns false if no moves have been undone")
  void testRedoWithoutUndo() {
    assertFalse(match.redo());
  }

  @Test
  @DisplayName("Quit: the match should stop")
  void testQuit() {
    match.quit();
    assertTrue(match.isMatchOver());
    assertFalse(match.isRunning());
    assertEquals(MatchStatus.FINISHED, match.getMatchStatus());
  }

  @Test
  @DisplayName("Hint: the AI should suggest a move")
  void testHint() {
    Move hint = match.hint();
    assertNotNull(hint, "The AI should suggest a move");
    assertEquals(
        Color.WHITE, hint.getColor(), "The suggested move should be for the current player");
  }

  @Test
  @DisplayName("History: getHistory should be empty at the start")
  void testGetHistoryInitiallyEmpty() {
    assertTrue(match.getHistory().isEmpty());
  }

  @Test
  @DisplayName("History: getHistory should return MoveDTOs")
  void testGetHistory() {
    match.move(
        new Move(CoordinateMapper.toIndex('B', 1), CoordinateMapper.toIndex('C', 1), Color.WHITE));

    List<MoveDtO> history = match.getHistory();
    assertFalse(history.isEmpty());
    assertEquals("b1", history.get(0).from().toLowerCase());
  }

  @Test
  @DisplayName("Branch: the match status changes to FINISHED upon victory")
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
    Player white = new HumanPlayer("White", Color.WHITE, null);
    Player black = new HumanPlayer("Black", Color.BLACK, null);
    TestMatch customMatch = new TestMatch(customBoard, white, black);

    Move winningMove =
        new Move(CoordinateMapper.toIndex('E', 7), CoordinateMapper.toIndex('E', 6), Color.WHITE);

    customMatch.move(winningMove);

    assertEquals(
        MatchStatus.FINISHED, customMatch.getMatchStatus(), "The status should be FINISHED");
    assertTrue(customMatch.isMatchOver());
  }

  @Test
  @DisplayName("Winner: getWinner should return the winning player after a victory")
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
    Player white = new HumanPlayer("White", Color.WHITE, null);
    Player black = new HumanPlayer("Black", Color.BLACK, null);
    TestMatch customMatch = new TestMatch(customBoard, white, black);

    Move winningMove =
        new Move(CoordinateMapper.toIndex('E', 7), CoordinateMapper.toIndex('E', 6), Color.WHITE);

    boolean result = customMatch.move(winningMove);

    assertTrue(result);
    assertEquals(MatchStatus.FINISHED, customMatch.getMatchStatus());
    assertEquals(white, customMatch.getWinner());
  }

  @Test
  @DisplayName("Pause: returns false")
  void testPause() {
    assertFalse(match.pause());
  }

  @Test
  @DisplayName("Remaining time: returns null")
  void testRemainingTime() {
    assertEquals(match.getCurrentPlayerRemainingTime(), "null");
  }
}
