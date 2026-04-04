package fr.univ.bordeaux.application.network.protocol;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoveProtocolParserTest {

  @Test
  @DisplayName("Parse returns null when move is null")
  void parse_null() {
    assertNull(MoveProtocolParser.parse(null));
  }

  @Test
  @DisplayName("Parse returns null when move is blank")
  void parse_blank() {
    assertNull(MoveProtocolParser.parse("   "));
  }

  @Test
  @DisplayName("Parse handles valid full move")
  void parse_valid_full_move() {
    MoveParsed move = MoveProtocolParser.parse("e2e4");

    assertNotNull(move);
    assertEquals("e2", move.getSourceText());
    assertEquals("e4", move.getDestinationText());
    assertEquals(CoordinateMapper.toIndex('E', 2), move.getFromIndex());
    assertEquals(CoordinateMapper.toIndex('E', 4), move.getToIndex());
    assertTrue(move.hasSource());
  }

  @Test
  @DisplayName("Parse handles valid full move with uppercase letters")
  void parse_valid_full_move_uppercase() {
    MoveParsed move = MoveProtocolParser.parse("E2F3");

    assertNotNull(move);
    assertEquals("e2", move.getSourceText());
    assertEquals("f3", move.getDestinationText());
    assertTrue(move.hasSource());
  }

  @Test
  @DisplayName("Parse handles valid full move with two digit rows")
  void parse_valid_full_move_two_digits() {
    MoveParsed move = MoveProtocolParser.parse("e10f11");

    assertNotNull(move);
    assertEquals("e10", move.getSourceText());
    assertEquals("f11", move.getDestinationText());
    assertTrue(move.hasSource());
  }

  @Test
  @DisplayName("Parse handles valid short replacement move")
  void parse_valid_short_move() {
    MoveParsed move = MoveProtocolParser.parse("e3");

    assertNotNull(move);
    assertNull(move.getSourceText());
    assertEquals("e3", move.getDestinationText());
    assertEquals(-1, move.getFromIndex());
    assertEquals(CoordinateMapper.toIndex('E', 3), move.getToIndex());
    assertFalse(move.hasSource());
  }

  @Test
  @DisplayName("Parse handles valid short move with uppercase letter")
  void parse_valid_short_move_uppercase() {
    MoveParsed move = MoveProtocolParser.parse("K11");

    assertNotNull(move);
    assertEquals("k11", move.getDestinationText());
    assertFalse(move.hasSource());
  }

  @Test
  @DisplayName("Parse returns null for invalid format")
  void parse_invalid_format() {
    assertNull(MoveProtocolParser.parse("invalid"));
    assertNull(MoveProtocolParser.parse("e2-e4"));
    assertNull(MoveProtocolParser.parse("1234"));
  }

  @Test
  @DisplayName("Parse returns null for invalid source row")
  void parse_invalid_source_row() {
    assertNull(MoveProtocolParser.parse("e0e4"));
    assertNull(MoveProtocolParser.parse("e12e4"));
  }

  @Test
  @DisplayName("Parse returns null for invalid destination row")
  void parse_invalid_destination_row() {
    assertNull(MoveProtocolParser.parse("e2e0"));
    assertNull(MoveProtocolParser.parse("e2e12"));
  }

  @Test
  @DisplayName("Parse returns null for invalid short move row")
  void parse_invalid_short_row() {
    assertNull(MoveProtocolParser.parse("e0"));
    assertNull(MoveProtocolParser.parse("e12"));
  }

  @Test
  @DisplayName("Parse returns null for invalid column")
  void parse_invalid_column() {
    assertNull(MoveProtocolParser.parse("l2e4"));
    assertNull(MoveProtocolParser.parse("e2l4"));
    assertNull(MoveProtocolParser.parse("l3"));
  }

  @Test
  @DisplayName("Parse supports trimmed move text")
  void parse_trimmed_move() {
    MoveParsed move = MoveProtocolParser.parse("  e2e4  ");

    assertNotNull(move);
    assertEquals("e2", move.getSourceText());
    assertEquals("e4", move.getDestinationText());
  }
}
