package fr.univ.bordeaux.agonCore.agonElements;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agonCore.history.History;
import fr.univ.bordeaux.agonCore.history.HistoryInformations;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HistoryTest {

  private History history;
  private HistoryInformations move1;
  private HistoryInformations move2;

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

    // Le dernier ajouté (move2) doit être au sommet de la pile
    assertEquals(move2, history.getHeadUndo());

    history.undo();
    // Après un undo, c'est move1 qui revient au sommet
    assertEquals(move1, history.getHeadUndo());
    assertEquals(move2, history.getHeadRedo());
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
}