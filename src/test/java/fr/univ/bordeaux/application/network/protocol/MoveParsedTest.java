package fr.univ.bordeaux.application.network.protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoveParsedTest {

  @Test
  @DisplayName("MoveParsed stores full move values correctly")
  void constructor_full_move() {
    MoveParsed move = new MoveParsed("e2", "e4", 10, 20);

    assertEquals("e2", move.getSourceText());
    assertEquals("e4", move.getDestinationText());
    assertEquals(10, move.getFromIndex());
    assertEquals(20, move.getToIndex());
    assertTrue(move.hasSource());
  }

  @Test
  @DisplayName("MoveParsed stores replacement move values correctly")
  void constructor_short_move() {
    MoveParsed move = new MoveParsed(null, "e4", -1, 20);

    assertNull(move.getSourceText());
    assertEquals("e4", move.getDestinationText());
    assertEquals(-1, move.getFromIndex());
    assertEquals(20, move.getToIndex());
    assertFalse(move.hasSource());
  }

  @Test
  @DisplayName("HasSource returns false when source text is blank")
  void has_source_blank_text() {
    MoveParsed move = new MoveParsed("   ", "e4", 10, 20);

    assertFalse(move.hasSource());
  }

  @Test
  @DisplayName("HasSource returns false when from index is negative")
  void has_source_negative_index() {
    MoveParsed move = new MoveParsed("e2", "e4", -1, 20);

    assertFalse(move.hasSource());
  }
}
