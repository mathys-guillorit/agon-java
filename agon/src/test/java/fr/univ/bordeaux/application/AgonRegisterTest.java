package fr.univ.bordeaux.application;

import fr.univ.bordeaux.application.commands.CmdAction;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class AgonRegisterTest {

  @Test
  @DisplayName("Test if the add works in register")
  void testAdd2RegisterTest() {
//    CmdAction cmd = new DummyCmd();;
//    AgonRegister<CmdAction> cmdRegister = new AgonRegister<>();
//    cmdRegister.register("test", cmd);
//    assertFalse(cmdRegister.isEmpty());
//    assertFalse(cmdRegister.get("test").isEmpty());
//    assertEquals(cmdRegister.get("test").get(), cmd);
  }

  @Test
  @DisplayName("if the remove and add many works even with different cass")
  void testRemove2RegisterTest() {
//    CmdAction cmd = new DummyCmd();
//    String cmdName1 = "test";
//    CmdRegister cmdRegister = CmdRegister.getInstance();
//    cmdRegister.register(cmdName1, cmd);
//    cmdRegister.register(cmdName1, cmd);
//    assertFalse(cmdRegister.get(cmdName1).isEmpty());
//    assertEquals(cmdRegister.get(cmdName1).get(), cmd);
//    assertFalse(cmdRegister.get("tEsT").isEmpty());
//    assertEquals(cmdRegister.get("tEsT").get(), cmd);
//    assertTrue(cmdRegister.get("something").isEmpty());
//    assertThrows(NoSuchElementException.class, () -> {
//      cmdRegister.get("something").get();
//    });
//    CmdAction cmd2 = new TestCmd();
//    String cmdName2 = "TeSt2";
//    cmdRegister.register(cmdName2, cmd2);
//    assertFalse(cmdRegister.isEmpty());
//    assertTrue(cmdRegister.get(cmdName2).isPresent());
//    assertEquals(cmd2, cmdRegister.get(cmdName2).get());
//    assertTrue(cmdRegister.get(cmdName2).isPresent());
//    assertEquals(cmd, cmdRegister.get(cmdName1).get());
//    assertTrue(CmdRegister.getInstance().get(cmdName1).isPresent());
//    assertEquals(cmd, CmdRegister.getInstance().get(cmdName1).get());
  }

  @Test
  @DisplayName("remove to register")
  void remove2RegisterTest() {
//    CmdAction cmd = new DummyCmd();
//    String cmdName1 = "test";
//    CmdAction cmd2 = new TestCmd();
//    String cmdName2 = "different";
//    CmdRegister cmdRegister = CmdRegister.getInstance();
//    cmdRegister.register(cmdName1, cmd);
//    cmdRegister.register(cmdName1, cmd2);
//    assertTrue(cmdRegister.get(cmdName1).isPresent());
//    // CmdRegister["test"]=cmd
//    assertFalse(cmdRegister.get(cmdName2).isPresent());
//    assertThrows(NoSuchElementException.class, () -> {
//      cmdRegister.get(cmdName2).get();
//    });
//    assertFalse(cmdRegister.get(cmdName2).isPresent());
//    cmdRegister.register(cmdName2, cmd2);
//    assertTrue(cmdRegister.get(cmdName2).isPresent());
//    assertEquals(cmdRegister.get(cmdName1).get(), cmd2);
//    assertEquals(cmdRegister.get(cmdName2).get(), cmd2);
//    cmdRegister.remove(cmdName1);
//    assertFalse(cmdRegister.get(cmdName1).isPresent());
//    assertTrue(cmdRegister.get(cmdName2).isPresent());
//    assertEquals(cmdRegister.get(cmdName2).get(), cmd2);
  }

  @Test
  @DisplayName("reset to 0 CmdRegister")
  void resetRegisterTest() {
//    CmdRegister.getInstance().reset();
//    final CmdAction cmd = new DummyCmd();
//    final String cmdName1 = "test";
//    final CmdAction cmd2 = new TestCmd();
//    final String cmdName2 = "different";
//    CmdRegister cmdRegister = CmdRegister.getInstance();
//    cmdRegister.register(cmdName1, cmd);
//    cmdRegister.register(cmdName2, cmd2);
//    assertTrue(cmdRegister.get(cmdName1).isPresent());
//    assertTrue(cmdRegister.get(cmdName2).isPresent());
//    cmdRegister.reset();
//    assertFalse(cmdRegister.get(cmdName1).isPresent());
//    assertFalse(cmdRegister.get(cmdName2).isPresent());
//    assertTrue(cmdRegister.get(cmdName1).isEmpty());
  }

  /**
   * fake commands to test
   * CANNOT USE EXTERN CONTEXT (the only way to test CmdRegister is private static class)
   */
  private static class DummyCmd implements CmdAction {
    @Override public void execute() {

    }
    @Override public void showHelp() {

    }

    @Nonnull
    @Override
    public Completer getAutoCompleter() {
      return null;
    }

    @Override
    public String getName() {
      return "";
    }

    @Override
    public Options getOptions() {
      return null;
    }
  }
  private static class TestCmd implements CmdAction {
    @Override public void execute() {

    }
    @Override public void showHelp() {

    }

    @Nonnull
    @Override
    public Completer getAutoCompleter() {
      return null;
    }

    @Override
    public String getName() {
      return "";
    }

    @Override
    public Options getOptions() {
      return null;
    }
  }


}
