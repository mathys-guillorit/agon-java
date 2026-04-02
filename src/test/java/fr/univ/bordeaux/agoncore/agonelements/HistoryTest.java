package fr.univ.bordeaux.agoncore.agonelements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.agoncore.history.History;
import fr.univ.bordeaux.agoncore.history.HistoryInformations;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HistoryTest {

  private History history;
  private HistoryInformations move1;
  private HistoryInformations move2;
  private HistoryInformations move3;

  @BeforeEach
  void setUp() {
    history = new History();

    // Initialisation du premier coup (Blanc)
    List<Move> moves1 = new ArrayList<>();
    moves1.add(new Move(0, 1, Color.WHITE, PieceType.WHITE_QUEEN));
    move1 = new HistoryInformations(moves1, PieceType.WHITE_QUEEN, Color.WHITE);

    // Initialisation du deuxième coup (Noir)
    List<Move> moves2 = new ArrayList<>();
    moves2.add(new Move(2, 3, Color.BLACK, PieceType.BLACK_QUEEN));
    move2 = new HistoryInformations(moves2, PieceType.BLACK_QUEEN, Color.BLACK);

    List<Move> moves3 = new ArrayList<>();
    moves3.add(new Move(0, 1, Color.WHITE, PieceType.WHITE_PAWN));
    move3 = new HistoryInformations(moves1, PieceType.WHITE_PAWN, Color.WHITE);
  }

  @Test
  @DisplayName("Should add HistoryInformations to undo stack")
  void testAddMove() {
    history.add(move1);
    assertFalse(history.isEmptyUndo());
    assertEquals(move1, history.getHeadUndo());
  }

  @Test
  @DisplayName("Should move HistoryInformations from undo to redo stack on undo")
  void testUndo() {
    history.add(move1);
    // On récupère l'objet complet qui contient la liste des mouvements
    HistoryInformations undoneInfo = history.undo();

    assertEquals(move1, undoneInfo);
    assertTrue(history.isEmptyUndo());
    assertFalse(history.isEmptyRedo());
    assertEquals(move1, history.getHeadRedo());
  }

  @Test
  @DisplayName("Should move HistoryInformations from redo back to undo on redo")
  void testRedo() {
    history.add(move1);
    history.undo();

    HistoryInformations redoneInfo = history.redo();

    assertEquals(move1, redoneInfo);
    assertFalse(history.isEmptyUndo());
    assertTrue(history.isEmptyRedo());
    assertEquals(move1, history.getHeadUndo());
  }

  @Test
  @DisplayName("Should return null when undoing an empty history")
  void testUndoEmpty() {
    assertNull(history.undo());
    assertTrue(history.isEmptyUndo());
  }

  @Test
  @DisplayName("Should handle multiple moves correctly (LIFO)")
  void testMultipleMoves() {
    history.add(move1);
    history.add(move2);
    history.add(move3);
    assertEquals(move3, history.getHeadUndo());

    history.undo();
    history.undo();
    history.undo();
    assertTrue(history.isEmptyUndo());
    assertFalse(history.isEmptyRedo());
    history.redo();
    assertEquals(move1, history.getHeadUndo());
    history.redo();
    assertEquals(move2, history.getHeadUndo());
    history.redo();
    assertEquals(move3, history.getHeadUndo());
  }

  @Test
  @DisplayName("Check empty status")
  void testEmptyStates() {
    assertTrue(history.isEmptyUndo());
    assertTrue(history.isEmptyRedo());

    history.add(move1);
    assertFalse(history.isEmptyUndo());

    history.undo();
    assertFalse(history.isEmptyRedo());
  }

  @Test
  @DisplayName("Should correctly export history to ABA-pro text format (including Queens)")
  void testToTextList() {
    history.add(move1);
    history.add(move2);

    List<String> textList = history.toTextList();

    assertNotNull(textList, "The exported list should not be null");
    assertEquals(2, textList.size(), "The exported list should contain exactly 2 moves");

    assertEquals("Q a1 a2", textList.get(0), "First move should be formatted as 'Q a1 a2'");
    assertEquals("q a3 a4", textList.get(1), "Second move should be formatted as 'q a3 a4'");
  }

  @Test
  @DisplayName("Should correctly import history from ABA-pro text format")
  void testConstructorFromTextList() {
    List<String> savedTextMoves = new ArrayList<>();
    savedTextMoves.add("O a1 a2");
    savedTextMoves.add("X c3 c5");

    History loadedHistory = new History(savedTextMoves);

    assertFalse(loadedHistory.isEmptyUndo(), "Undo stack should not be empty after loading");

    HistoryInformations lastMove = loadedHistory.getHeadUndo();
    assertEquals(Color.BLACK, lastMove.getColor(), "Last move should be played by Black");
    assertEquals(
        24, lastMove.getMoves().get(0).getFrom(), "Last move 'from' index should be 24 (c3)");
    assertEquals(
        26, lastMove.getMoves().get(0).getDestination(), "Last move 'to' index should be 26 (c5)");

    loadedHistory.undo();
    HistoryInformations firstMove = loadedHistory.getHeadUndo();
    assertEquals(Color.WHITE, firstMove.getColor(), "First move should be played by White");
    assertEquals(
        0, firstMove.getMoves().get(0).getFrom(), "First move 'from' index should be 0 (a1)");
    assertEquals(
        1, firstMove.getMoves().get(0).getDestination(), "First move 'to' index should be 1 (a2)");
  }

  @Test
  @DisplayName("Should correctly parse history with complex capture notation")
  void testConstructorFromTextListWithCaptures() {
    List<String> savedTextMoves = new ArrayList<>();
    savedTextMoves.add("X c3 c2 (O c1, q d5)");

    History loadedHistory = new History(savedTextMoves);
    HistoryInformations turn = loadedHistory.getHeadUndo();

    assertNotNull(turn, "Turn should have been parsed");
    List<Move> parsedMoves = turn.getMoves();

    assertEquals(3, parsedMoves.size(), "Turn should contain 1 main move and 2 captures");

    assertEquals(Color.BLACK, parsedMoves.get(0).getColor());

    assertEquals(Color.WHITE, parsedMoves.get(1).getColor(), "First capture should be White");

    assertEquals(Color.BLACK, parsedMoves.get(2).getColor(), "Second capture should be Black");
  }

  @Test
  @DisplayName("Should correctly export a turn with multiple captures to text format")
  void testToTextListWithCaptures() {
    History complexHistory = new History();

    List<Move> multiMoves = new ArrayList<>();
    multiMoves.add(new Move(0, 1, Color.WHITE, PieceType.WHITE_PAWN));
    multiMoves.add(new Move(24, 99, Color.BLACK, PieceType.BLACK_PAWN));

    HistoryInformations complexTurn =
        new HistoryInformations(multiMoves, PieceType.WHITE_PAWN, Color.WHITE);
    complexHistory.add(complexTurn);

    List<String> textList = complexHistory.toTextList();

    assertEquals(1, textList.size(), "Should export 1 turn");

    String exportedMove = textList.get(0);
    assertTrue(
        exportedMove.startsWith("O a1 a2 ("), "Should start with main move and open parenthesis");
    assertTrue(exportedMove.contains("X"), "Should contain the captured black guard 'X'");
    assertTrue(exportedMove.endsWith(")"), "Should close the parenthesis");
  }
}
