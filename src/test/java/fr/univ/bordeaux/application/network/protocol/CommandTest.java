package fr.univ.bordeaux.application.network.protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommandTest {

  @Test
  @DisplayName("Command constructor without raw argument stores type and args")
  void constructor_without_raw_arg() {
    Map<String, String> args = new HashMap<>();
    args.put("NAME", "Alice");

    Command command = new Command(CommandType.LOGIN, args);

    assertEquals(CommandType.LOGIN, command.getType());
    assertEquals("Alice", command.getArg("NAME"));
    assertEquals(args, command.getArgs());
    assertNull(command.getRawArgument());
  }

  @Test
  @DisplayName("Command constructor with raw argument stores all values")
  void constructor_with_raw_arg() {
    Map<String, String> args = new HashMap<>();
    args.put("PLAYER_ID", "4");

    Command command = new Command(CommandType.NEW, args, "e2e4");

    assertEquals(CommandType.NEW, command.getType());
    assertEquals("4", command.getArg("PLAYER_ID"));
    assertEquals("e2e4", command.getRawArgument());
  }

  @Test
  @DisplayName("GetArg returns null when key does not exist")
  void get_arg_unknown_key() {
    Command command = new Command(CommandType.PING, Map.of());

    assertNull(command.getArg("missing"));
  }

  @Test
  @DisplayName("GetArgs returns stored map")
  void get_args() {
    Map<String, String> args = new HashMap<>();
    args.put("A", "1");
    args.put("B", "2");

    Command command = new Command(CommandType.STATUS, args);

    assertEquals(2, command.getArgs().size());
    assertEquals("1", command.getArgs().get("A"));
    assertEquals("2", command.getArgs().get("B"));
  }
}
