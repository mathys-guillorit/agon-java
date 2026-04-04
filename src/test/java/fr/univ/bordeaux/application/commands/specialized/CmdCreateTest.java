package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.GameEngine;
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

public class CmdCreateTest {

  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface ui;
  private GameEngine engine;
  private GameConfig globalConfig;

  @BeforeEach
  void setUp() {
    // La config globale de l'application (le "Template")
    globalConfig = new GameConfig();
    globalConfig.setBlitzMode(false);
    globalConfig.setTimeout(30);

    LineReader reader = new FakeLineReader("n");
    try {
      Terminal terminal = new FakeTerminal(new ByteArrayOutputStream());
      ui = new AgonShell(terminal, reader, cmds);
      engine = new GameEngine(ui, cmds);

      // On enregistre la commande avec la config globale
      cmds.register("create", new CmdCreate(ui, globalConfig, engine));
    } catch (Exception e) {
      fail("Le setup a échoué : " + e.getMessage());
    }
  }

  @Test
  @DisplayName("F15 : Vérifier qu'un match est bien créé dans l'engine")
  void testExecuteCreatesMatch() {
    CmdAction cmd = cmds.get("create").get().createNew(new String[] {});
    boolean result = cmd.execute(null);

    assertTrue(result);
    assertNotNull(engine.getMatchManager(), "Un MatchManager doit être créé après execute");
  }

  @Test
  @DisplayName("Isolation : La config globale ne doit pas être modifiée par un match")
  void testConfigIsolation() {
    // On lance un match en mode Blitz
    CmdAction cmd = cmds.get("create").get().createNew(new String[] {"-b", "-t", "60"});
    cmd.execute(null);

    // 1. On vérifie que le match actuel est BIEN en blitz 60s
    GameConfig matchConfig = engine.getMatchManager().getGameConfig();
    assertTrue(matchConfig.isBlitzMode());
    assertEquals(60, matchConfig.getTimeout());

    // 2. On vérifie que la config globale est RESTÉE à ses valeurs par défaut
    assertFalse(globalConfig.isBlitzMode(), "La config globale a été polluée !");
    assertEquals(30, globalConfig.getTimeout(), "La config globale a été polluée !");
  }

  @Test
  @DisplayName("F8 : Vérifier l'activation de l'IA via la commande")
  void testAiOption() {
    // On active l'IA pour le blanc
    CmdAction cmd = cmds.get("create").get().createNew(new String[] {"-a", "white"});
    cmd.execute(null);

    GameConfig matchConfig = engine.getMatchManager().getGameConfig();
    assertTrue(matchConfig.isWhiteAi());
    assertFalse(matchConfig.isBlackAi());
  }

  @Test
  @DisplayName("Gestion d'erreur : Option inconnue")
  void testUnknownOption() {
    CmdAction cmd = cmds.get("create").get().createNew(new String[] {"--voldemort"});
    boolean result = cmd.execute(null);

    // Doit renvoyer false à cause du ParseException catché dans CmdCreate
    assertFalse(result, "La commande devrait échouer avec une option inconnue");
  }
}
