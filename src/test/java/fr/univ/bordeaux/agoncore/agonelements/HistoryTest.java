package fr.univ.bordeaux.agoncore.agonelements;

class HistoryTest {
  /*
  private History history;
  private Move move1;
  private Move move2;

  @BeforeEach
  void setUp() {
    history = new History();
    move1 = new Move(10, 20, Color.WHITE);
    move2 = new Move(20, 30, Color.BLACK);
  }

  @Test
  @DisplayName("Should add moves to undo stack")
  void testAddMove() {
    history.add(move1);
    assertFalse(history.isEmptyUndo());
    assertEquals(move1, history.getHeadUndo());
  }

  @Test
  @DisplayName("Should move item from undo to redo stack on undo")
  void testUndo() {
    history.add(move1);
    Move undoneMove = history.undo();

    assertEquals(move1, undoneMove);
    assertTrue(history.isEmptyUndo());
    assertFalse(history.isEmptyRedo());
    assertEquals(move1, history.getHeadRedo());
  }

  @Test
  @DisplayName("Should move item from redo back to undo on redo")
  void testRedo() {
    history.add(move1);
    history.undo();

    Move redoneMove = history.redo();

    assertEquals(move1, redoneMove);
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

    assertEquals(move2, history.getHeadUndo(), "The last move added should be at the head");

    history.undo();
    assertEquals(move1, history.getHeadUndo(), "After one undo, the first move should be at the head");
    assertEquals(move2, history.getHeadRedo(), "The undone move should be in the redo stack");
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
  }*/
}
