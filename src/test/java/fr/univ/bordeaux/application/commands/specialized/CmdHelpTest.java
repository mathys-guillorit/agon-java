package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import java.io.ByteArrayOutputStream;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CmdHelpTest {
  private AgonRegister<CmdAction> cmds;
  private GameUserInterface gameUserInterface;
  private ByteArrayOutputStream outContent;

  @BeforeEach
  void setUp() {
    cmds = new AgonRegister<>();
    outContent = new ByteArrayOutputStream();
    LineReader reader = new FakeLineReader("");
    try {
      Terminal terminal = new FakeTerminal(outContent);
      gameUserInterface = new AgonShell(terminal, reader, cmds);

      AppContext context = new AppContext(new LocalProfile("test"));
      cmds.register("help", new CmdHelp(gameUserInterface, cmds));
      cmds.register("quit", new CmdQuit(gameUserInterface, context));

    } catch (Exception e) {
      fail("Setup failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Vérifier l'aide globale avec la liste des commandes")
  void testGlobalHelp() {

    CmdAction cmd = cmds.get("help").get().createNew(new String[] {});
    boolean result = cmd.execute(null);

    assertTrue(result);
    String output = outContent.toString();

    assertTrue(output.contains("AVAILABLE COMMANDS"));
    assertTrue(output.contains("help"));
    assertTrue(output.contains("quit"));
  }

  @Test
  @DisplayName("Vérifier l'aide ciblée pour une commande existante (quit)")
  void testTargetedHelpSuccess() {
    CmdAction cmd = cmds.get("help").get().createNew(new String[] {"quit"});
    boolean result = cmd.execute(null);

    assertTrue(result);
    String output = outContent.toString();

    assertTrue(output.contains("HELP: QUIT"));
    assertTrue(output.contains("Usage: quit"));
    assertTrue(output.contains("disconnects from it"));
  }

  @Test
  @DisplayName("Vérifier le message d'erreur pour une commande qui n'existe pas")
  void testTargetedHelpFailure() {

    CmdAction cmd = cmds.get("help").get().createNew(new String[] {"nimportequoi"});
    boolean result = cmd.execute(null);

    assertTrue(result);
    assertTrue(outContent.toString().contains("Unknown command: nimportequoi"));
  }

  @Test
  @DisplayName("Vérifier les métadonnées de la commande help")
  void testMetadata() {
    CmdAction cmd = cmds.get("help").get();
    assertEquals("help", cmd.getName());
    assertNotNull(cmd.getDescription());
  }
}
