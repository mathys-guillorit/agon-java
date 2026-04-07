package fr.univ.bordeaux.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AgonRegisterTest {

  @Test
  @DisplayName("Test if the add works in register")
  void testAdd2RegisterTest() {
    CmdAction cmd = new DummyCmd();
    AgonRegister<CmdAction> cmdRegister = new AgonRegister<>();
    cmdRegister.register("test", cmd);
    assertFalse(cmdRegister.isEmpty());
    assertFalse(cmdRegister.get("test").isEmpty());
    assertEquals(cmdRegister.get("test").get(), cmd);
  }

  @Test
  @DisplayName("if the remove and add many works even with different cass")
  void testRemove2RegisterTest() {
    CmdAction cmd = new DummyCmd();
    String cmdName1 = "test";
    AgonRegister<CmdAction> cmdRegister = new AgonRegister<>();
    cmdRegister.register(cmdName1, cmd);
    cmdRegister.register(cmdName1, cmd);
    assertFalse(cmdRegister.get(cmdName1).isEmpty());
    assertEquals(cmdRegister.get(cmdName1).get(), cmd);
    assertFalse(cmdRegister.get("tEsT").isEmpty());
    assertEquals(cmdRegister.get("tEsT").get(), cmd);
    assertTrue(cmdRegister.get("something").isEmpty());
    assertThrows(
        NoSuchElementException.class,
        () -> {
          cmdRegister.get("something").get();
        });
    CmdAction cmd2 = new TestCmd();
    String cmdName2 = "TeSt2";
    cmdRegister.register(cmdName2, cmd2);
    assertFalse(cmdRegister.isEmpty());
    assertTrue(cmdRegister.get(cmdName2).isPresent());
    assertEquals(cmd2, cmdRegister.get(cmdName2).get());
    assertTrue(cmdRegister.get(cmdName2).isPresent());
    assertEquals(cmd, cmdRegister.get(cmdName1).get());
  }

  @Test
  @DisplayName("remove to register")
  void remove2RegisterTest() {
    CmdAction cmd = new DummyCmd();
    String cmdName1 = "test";
    CmdAction cmd2 = new TestCmd();
    String cmdName2 = "different";
    AgonRegister<CmdAction> cmdRegister = new AgonRegister<>();
    cmdRegister.register(cmdName1, cmd);
    cmdRegister.register(cmdName1, cmd2);
    assertTrue(cmdRegister.get(cmdName1).isPresent());
    // CmdRegister["test"]=cmd
    assertFalse(cmdRegister.get(cmdName2).isPresent());
    assertThrows(
        NoSuchElementException.class,
        () -> {
          cmdRegister.get(cmdName2).get();
        });
    assertFalse(cmdRegister.get(cmdName2).isPresent());
    cmdRegister.register(cmdName2, cmd2);
    assertTrue(cmdRegister.get(cmdName2).isPresent());
    assertEquals(cmdRegister.get(cmdName1).get(), cmd2);
    assertEquals(cmdRegister.get(cmdName2).get(), cmd2);
    cmdRegister.remove(cmdName1);
    assertFalse(cmdRegister.get(cmdName1).isPresent());
    assertTrue(cmdRegister.get(cmdName2).isPresent());
    assertEquals(cmdRegister.get(cmdName2).get(), cmd2);
  }

  @Test
  @DisplayName("remove with different character case")
  void removeCaseSensitiveTest() {
    CmdAction cmd = new DummyCmd();
    String cmdName1 = "test";
    CmdAction cmd2 = new TestCmd();
    String cmdName2 = "different";
    AgonRegister<CmdAction> cmdRegister = new AgonRegister<>(true);
    cmdRegister.register(cmdName1, cmd);
    cmdRegister.register(cmdName1, cmd2);
    assertEquals(1, cmdRegister.size());
    cmdRegister.remove("tesT");
    assertEquals(1, cmdRegister.size());
  }

  @Test
  @DisplayName("reset to 0 CmdRegister")
  void resetRegisterTest() {
    final CmdAction cmd = new DummyCmd();
    final String cmdName1 = "test";
    final CmdAction cmd2 = new TestCmd();
    final String cmdName2 = "different";
    AgonRegister<CmdAction> cmdRegister = new AgonRegister<>();
    cmdRegister.register(cmdName1, cmd);
    cmdRegister.register(cmdName2, cmd2);
    assertEquals(cmd, cmdRegister.get(cmdName1).get());
    assertTrue(cmdRegister.get(cmdName1).isPresent());
    assertTrue(cmdRegister.get(cmdName2).isPresent());
    cmdRegister.reset();
    assertFalse(cmdRegister.get(cmdName1).isPresent());
    assertFalse(cmdRegister.get(cmdName2).isPresent());
    assertTrue(cmdRegister.get(cmdName1).isEmpty());
  }

  @Test
  @DisplayName("content test")
  void contentTest() {
    final CmdAction cmd = new DummyCmd();
    final String cmdName1 = "test";
    final CmdAction cmd2 = new TestCmd();
    final String cmdName2 = "different";
    AgonRegister<CmdAction> cmdRegister = new AgonRegister<>();
    cmdRegister.register(cmdName1, cmd);
    cmdRegister.register(cmdName2, cmd2);
    assertEquals(2, cmdRegister.size());
    Set<String> container = new HashSet<>();
    container.add(cmdName1);
    container.add(cmdName2);
    assertEquals(cmdRegister.getKeys(), container);
  }

  @Test
  @DisplayName("case sensitive test")
  void caseSensitiveTest() {
    final CmdAction cmd = new DummyCmd();
    final String cmdName1 = "teSt";
    final CmdAction cmd2 = new TestCmd();
    final String cmdName2 = "test";
    AgonRegister<CmdAction> cmdRegister = new AgonRegister<>(true);
    cmdRegister.register(cmdName1, cmd);
    cmdRegister.register(cmdName2, cmd2);
    assertTrue(cmdRegister.get(cmdName1).isPresent());
    assertTrue(cmdRegister.get(cmdName2).isPresent());
    cmdRegister.reset();
    assertFalse(cmdRegister.get(cmdName1).isPresent());
    assertFalse(cmdRegister.get(cmdName2).isPresent());
    assertTrue(cmdRegister.get(cmdName1).isEmpty());
  }

  @Test
  @DisplayName("check behavior when key is null")
  void nullKeyName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new AgonRegister<Integer>().register(null, 5),
        "null name should throw an exception because is not @Nullable");
  }

  private static class DummyCmd implements CmdAction {
    @Override
    public boolean execute(MatchManager match) {
      return true;
    }

    @Override
    public CmdAction createNew(String[] args) {
      return this;
    }

    @Override
    public String getDescription() {
      return "dummy";
    }

    @Nonnull
    @Override
    public Completer getAutoCompleter() {
      return null;
    }

    @Override
    public String getName() {
      return "dummy";
    }

    @Override
    public Options getOptions() {
      return null;
    }

    @Override
    public String getHelp() {
      return "";
    }
  }

  private static class TestCmd implements CmdAction {
    @Override
    public boolean execute(MatchManager match) {
      return true;
    }

    @Override
    public CmdAction createNew(String[] args) {
      return this;
    }

    @Override
    public String getDescription() {
      return "test";
    }

    @Nonnull
    @Override
    public Completer getAutoCompleter() {
      return null;
    }

    @Override
    public String getName() {
      return "test";
    }

    @Override
    public Options getOptions() {
      return null;
    }

    @Override
    public String getHelp() {
      return "";
    }
  }
}
