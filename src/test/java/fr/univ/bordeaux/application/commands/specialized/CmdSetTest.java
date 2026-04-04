package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.technical.io.config.GameConfig;
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

public class CmdSetTest {
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface gameUserInterface;
  private GameConfig config;
  private ByteArrayOutputStream outContent;

  @BeforeEach
  void setUp() {
    config = new GameConfig();
    outContent = new ByteArrayOutputStream();
    LineReader reader = new FakeLineReader("");
    try {
      Terminal terminal = new FakeTerminal(outContent);
      gameUserInterface = new AgonShell(terminal, reader, cmds);

      // Enregistrement du prototype avec la config réelle
      cmds.register("set", new CmdSet(gameUserInterface, config));
    } catch (Exception e) {
      fail("Setup failed");
    }
  }

  @Test
  @DisplayName("Vérifier la modification des paramètres système (verbose/debug)")
  void testSetSystemParams() {
    // Initialement à false (supposé)
    config.setVerbose(false);

    // On simule : set -verbose true -debug true
    CmdAction cmd =
        cmds.get("set").get().createNew(new String[] {"-verbose", "true", "-debug", "true"});
    boolean result = cmd.execute(null);

    assertTrue(result);
    assertTrue(config.isVerbose());
    assertTrue(config.isDebug());
    assertTrue(outContent.toString().contains("Verbose: true"));
  }

  @Test
  @DisplayName("Vérifier la modification des paramètres IA (profondeur/mode)")
  void testSetAIParams() {
    // On simule : set -aiDepth 8 -aiMode minimax
    CmdAction cmd =
        cmds.get("set")
            .get()
            .createNew(
                new String[] {
                  "-aiDepth",
                  "8",
                  "-aiMode",
                  "minimax",
                  "-aiTimeLimit",
                  "1800",
                  "-aiIterativeDeepening",
                  "true",
                  "-aiHeuristic",
                  "mixed",
                  "-blitzmode",
                  "true",
                  "-aiActive",
                  "true",
                  "-timeout",
                  "10"
                });
    cmd.execute(null);

    assertEquals(8, config.getAiDepth());
    assertEquals("minimax", config.getAiMode());
    assertTrue(outContent.toString().contains("AI Depth: 8"));
  }

  @Test
  @DisplayName("Vérifier la gestion des erreurs de format (NumberFormatException)")
  void testSetInvalidNumber() {
    // On passe une chaîne au lieu d'un nombre pour le timeout
    CmdAction cmd = cmds.get("set").get().createNew(new String[] {"-timeout", "pas_un_nombre"});
    boolean result = cmd.execute(null);

    assertFalse(result, "La commande doit échouer avec un mauvais format de nombre");
    assertTrue(outContent.toString().contains("Error: Value must be a number"));
  }

  @Test
  @DisplayName("Vérifier la gestion des erreurs de syntaxe CLI")
  void testSetInvalidSyntax() {
    // Argument inconnu ou mal formé
    CmdAction cmd = cmds.get("set").get().createNew(new String[] {"-paramInconnu", "value"});
    boolean result = cmd.execute(null);

    assertFalse(result, "La commande doit échouer si l'option n'existe pas");
    assertTrue(outContent.toString().contains("Syntax error"));
  }

  @Test
  @DisplayName("Vérifier l'affectation des joueurs IA")
  void testSetPlayerAI() {
    // set -whiteIsAI true -blackIsAI false
    CmdAction cmd =
        cmds.get("set").get().createNew(new String[] {"-whiteIsAI", "true", "-blackIsAI", "false"});
    cmd.execute(null);

    assertTrue(config.isWhiteAi());
    assertFalse(config.isBlackAi());
  }

  @Test
  @DisplayName("Vérifier la description de la commande")
  void testDescription() {
    CmdAction cmd = cmds.get("set").get().createNew(new String[] {});
    String desc = cmd.getDescription();
    assertTrue(desc.contains("Usage: set -PARAM VALUE"));
    assertTrue(desc.contains("Example: set -aiDepth 5"));
  }
}
