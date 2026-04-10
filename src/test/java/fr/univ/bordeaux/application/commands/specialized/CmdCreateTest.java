package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class CmdCreateTest {

  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface ui;
  private GameEngine engine;
  private GameConfig globalConfig;

  @BeforeEach
  void setUp() {
    globalConfig = new GameConfig();
    globalConfig.setBlitzMode(false);
    globalConfig.setTimeout(30);

    LineReader reader = new FakeLineReader("n");
    try {
      Terminal terminal = new FakeTerminal(new ByteArrayOutputStream());
      ui = new AgonShell(terminal, reader, cmds);
      engine = new GameEngine(ui, cmds);
      cmds.register("create", new CmdCreate(ui, globalConfig, engine));
    } catch (Exception e) {
      fail("Setup failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("F15: Verify that a match is correctly created in the engine")
  void testExecuteCreatesMatch() {
    CmdAction cmd = cmds.get("create").get().createNew(new String[] {});
    boolean result = cmd.execute(null);

    assertTrue(result);
    assertNotNull(engine.getMatchManager(), "A MatchManager should be created after execution");
  }

  @Test
  @DisplayName("Isolation: Global config should not be modified by a match")
  void testConfigIsolation() {
    CmdAction cmd = cmds.get("create").get().createNew(new String[] {"-b", "-t", "60"});
    cmd.execute(null);

    GameConfig matchConfig = engine.getMatchManager().getGameConfig();
    assertTrue(matchConfig.isBlitzMode());
    assertEquals(60, matchConfig.getTimeout());
    assertFalse(globalConfig.isBlitzMode(), "Global config was polluted!");
    assertEquals(30, globalConfig.getTimeout(), "Global config was polluted!");
  }

  @Test
  @DisplayName("F8: Verify AI activation via command")
  void testAiOption() {
    CmdAction cmd = cmds.get("create").get().createNew(new String[] {"-a", "white"});
    cmd.execute(null);

    GameConfig matchConfig = engine.getMatchManager().getGameConfig();
    assertTrue(matchConfig.isWhiteAi());
    assertFalse(matchConfig.isBlackAi());
  }

  @Test
  @DisplayName("Error handling: Unknown option")
  void testUnknownOption() {
    CmdAction cmd = cmds.get("create").get().createNew(new String[] {"--voldemort"});
    boolean result = cmd.execute(null);

    assertFalse(result, "Command should fail with an unknown option");
  }

  @Test
  void descriptionTest(){
    assertTrue(cmds.get("create").isPresent());
    CmdAction cmd = cmds.get("create").get().createNew(new String[] {"--Lukas_joke"});
    assertTrue(cmd.getDescription().contains("Description: Starts a new Agon game session"),
            cmd.getDescription()
    );
    assertTrue(cmd.getDescription().contains("ai-mode ai-time ai-minimax-depth"),
            cmd.getDescription()
    );
  }
}
