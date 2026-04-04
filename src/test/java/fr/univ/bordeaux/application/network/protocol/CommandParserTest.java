package fr.univ.bordeaux.application.network.protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommandParserTest {

  @Test
  @DisplayName("Parse returns UNKNOWN command when line is null")
  void parse_null() {
    Command c = CommandParser.parse(null);

    assertEquals(CommandType.UNKNOWN, c.getType());
    assertTrue(c.getArgs().isEmpty());
    assertNull(c.getRawArgument());
  }

  @Test
  @DisplayName("Parse returns UNKNOWN command when line is blank")
  void parse_blank() {
    Command c = CommandParser.parse("   ");

    assertEquals(CommandType.UNKNOWN, c.getType());
    assertTrue(c.getArgs().isEmpty());
    assertNull(c.getRawArgument());
  }

  @Test
  @DisplayName("Parse handles command without arguments")
  void parse_no_args() {
    Command c = CommandParser.parse("PING");

    assertEquals(CommandType.PING, c.getType());
    assertTrue(c.getArgs().isEmpty());
    assertNull(c.getArg("x"));
    assertNull(c.getRawArgument());
  }

  @Test
  @DisplayName("Parse handles key value arguments correctly")
  void parse_key_value_args() {
    Command c = CommandParser.parse("LOGIN NAME=Alice CLIENT_ID=abc123");

    assertEquals(CommandType.LOGIN, c.getType());
    assertEquals("Alice", c.getArg("NAME"));
    assertEquals("abc123", c.getArg("CLIENT_ID"));
    assertNull(c.getRawArgument());
  }

  @Test
  @DisplayName("Parse stores first non key value token as raw argument")
  void parse_raw_argument() {
    Command c = CommandParser.parse("MOVE e2e4");

    assertEquals(CommandType.MOVE, c.getType());
    assertEquals("e2e4", c.getRawArgument());
    assertTrue(c.getArgs().isEmpty());
  }

  @Test
  @DisplayName("Parse supports both key value args and raw argument")
  void parse_args_and_raw_argument() {
    Command c = CommandParser.parse("NEW PLAYER_ID=5 extraToken");

    assertEquals(CommandType.NEW, c.getType());
    assertEquals("5", c.getArg("PLAYER_ID"));
    assertEquals("extraToken", c.getRawArgument());
  }

  @Test
  @DisplayName("Parse ignores malformed tokens and keeps valid key value args")
  void parse_good_and_bad_tokens() {
    Command c = CommandParser.parse("PONG a=1 bad =x b=2");

    assertEquals(CommandType.PONG, c.getType());
    assertEquals("1", c.getArg("a"));
    assertEquals("2", c.getArg("b"));
    assertNull(c.getArg("bad"));
    assertNull(c.getArg(""));
    assertEquals("bad", c.getRawArgument());
  }

  @Test
  @DisplayName("Parse keeps only first raw argument")
  void parse_only_first_raw_argument() {
    Command c = CommandParser.parse("MOVE e2e4 anotherToken lastToken");

    assertEquals(CommandType.MOVE, c.getType());
    assertEquals("e2e4", c.getRawArgument());
  }

  @Test
  @DisplayName("Parse supports trimmed input")
  void parse_trimmed_input() {
    Command c = CommandParser.parse("   STATUS   ");

    assertEquals(CommandType.STATUS, c.getType());
    assertTrue(c.getArgs().isEmpty());
  }

  @Test
  @DisplayName("Parse returns UNKNOWN when command keyword is invalid")
  void parse_invalid_command() {
    Command c = CommandParser.parse("HELLO x=1");

    assertEquals(CommandType.UNKNOWN, c.getType());
    assertEquals("1", c.getArg("x"));
  }
}
