package fr.univ.bordeaux.application.network.protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommandTypeTest {

  @Test
  @DisplayName("ConvertCommandType returns UNKNOWN when text is null")
  void type_null() {
    assertEquals(CommandType.UNKNOWN, CommandType.convertCommandType(null));
  }

  @Test
  @DisplayName("ConvertCommandType returns UNKNOWN when text is blank")
  void type_blank() {
    assertEquals(CommandType.UNKNOWN, CommandType.convertCommandType("   "));
  }

  @Test
  @DisplayName("ConvertCommandType parses valid uppercase command")
  void type_valid_uppercase() {
    assertEquals(CommandType.PING, CommandType.convertCommandType("PING"));
  }

  @Test
  @DisplayName("ConvertCommandType parses valid lowercase command")
  void type_valid_lowercase() {
    assertEquals(CommandType.PING, CommandType.convertCommandType("ping"));
  }

  @Test
  @DisplayName("ConvertCommandType returns UNKNOWN for invalid command")
  void type_invalid() {
    assertEquals(CommandType.UNKNOWN, CommandType.convertCommandType("HELLO"));
  }
}
