package fr.univ.bordeaux.application.match.player;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdQuit;
import fr.univ.bordeaux.ui.GameUserInterface;
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

  // Stub interne pour contrôler getUserInput
  private class FakeUserInterface extends AgonShell {
    private String simulatedInput;

    public FakeUserInterface() {
      super(new FakeTerminal(new ByteArrayOutputStream()), new FakeLineReader(), new AgonRegister<>());
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
    // On enregistre au moins une commande pour le test du parseur
    cmds.register("quit", new CmdQuit(fakeUi));

    humanPlayer = new HumanPlayer("Jean", Color.BLACK, fakeUi);
  }

  @Test
  @DisplayName("Test des getters basiques")
  void testGetters() {
    assertEquals("Jean", humanPlayer.getName());
    assertEquals(Color.BLACK, humanPlayer.getColor());
  }

  @Test
  @DisplayName("getAction : retourne null si l'entrée est nulle ou vide")
  void testGetActionEmptyInput() {
    // Cas null
    fakeUi.setSimulatedInput(null);
    assertNull(humanPlayer.getAction(cmds));

    // Cas vide
    fakeUi.setSimulatedInput("   ");
    assertNull(humanPlayer.getAction(cmds));
  }

  @Test
  @DisplayName("getAction : délègue au parseur quand l'entrée est valide")
  void testGetActionValidInput() {
    // On simule la saisie "quit"
    fakeUi.setSimulatedInput("quit");

    CmdAction action = humanPlayer.getAction(cmds);

    assertNotNull(action);
    assertTrue(action instanceof CmdQuit, "L'entrée 'quit' doit retourner une CmdQuit");
  }

  @Test
  @DisplayName("getAction : retourne null si la commande est inconnue (via UiPromptParser)")
  void testGetActionUnknownCommand() {
    fakeUi.setSimulatedInput("notACommand");

    // Le UiPromptParser.parse devrait retourner null (ou afficher une erreur et retourner null)
    CmdAction action = humanPlayer.getAction(cmds);
    assertNull(action);
  }
}