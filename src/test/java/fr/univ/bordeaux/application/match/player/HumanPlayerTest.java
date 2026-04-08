package fr.univ.bordeaux.application.match.player;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdQuit;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HumanPlayerTest {

  private HumanPlayer humanPlayer;
  private AgonRegister<CmdAction> cmds;
  private FakeUserInterface fakeUi;

  private class FakeUserInterface extends AgonShell {
    private String simulatedInput;

    public FakeUserInterface() {
      super(
          new FakeTerminal(new ByteArrayOutputStream()),
          new FakeLineReader(),
          new AgonRegister<>());
    }

    @Override
    public String getUserInput() {
      return simulatedInput;
    }

    public void setSimulatedInput(String input) {
      this.simulatedInput = input;
    }
  }

  @BeforeEach
  void setUp() {
    fakeUi = new FakeUserInterface();
    cmds = new AgonRegister<>();

    AppContext context = new AppContext(new LocalProfile("test"));
    cmds.register("quit", new CmdQuit(fakeUi, context));

    humanPlayer = new HumanPlayer("Jean", Color.BLACK, fakeUi);
  }

  @Test
  @DisplayName("Test basic getters")
  void testGetters() {
    assertEquals("Jean", humanPlayer.getName());
    assertEquals(Color.BLACK, humanPlayer.getColor());
  }

  @Test
  @DisplayName("getAction: returns null if input is null or empty")
  void testGetActionEmptyInput() {
    fakeUi.setSimulatedInput(null);
    assertNull(humanPlayer.getAction(cmds));

    fakeUi.setSimulatedInput("   ");
    assertNull(humanPlayer.getAction(cmds));
  }

  @Test
  @DisplayName("getAction: delegates to parser when input is valid")
  void testGetActionValidInput() {
    fakeUi.setSimulatedInput("quit");

    CmdAction action = humanPlayer.getAction(cmds);

    assertNotNull(action);
    assertTrue(action instanceof CmdQuit, "Input 'quit' should return a CmdQuit instance");
  }

  @Test
  @DisplayName("getAction: returns null if command is unknown")
  void testGetActionUnknownCommand() {
    fakeUi.setSimulatedInput("notACommand");

    CmdAction action = humanPlayer.getAction(cmds);
    assertNull(action);
  }
}