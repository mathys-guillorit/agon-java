package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CmdLoadTest {

  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface ui;
  private GameEngine engine;
  private ByteArrayOutputStream outContent;

  @TempDir Path tempDir;

  @BeforeEach
  void setUp() {
    outContent = new ByteArrayOutputStream();
    LineReader reader = new FakeLineReader("");
    try {
      Terminal terminal = new FakeTerminal(outContent);
      ui = new AgonShell(terminal, reader, cmds);
      engine = new GameEngine(ui, cmds);

      cmds.register("load", new CmdLoad(ui, engine));
    } catch (Exception e) {
      fail("Setup failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Verify error if no filename is provided")
  void testCreateNewNoArgs() {
    CmdAction cmd = cmds.get("load").get().createNew(new String[] {});

    assertNull(cmd);
    assertTrue(outContent.toString().contains("Error: Please provide a filename."));
  }

  @Test
  @DisplayName("Successful loading of a valid save file")
  void testExecuteLoadSuccess() throws Exception {
    String saveContent =
        "[settings]\n"
            + "verbose = false\n"
            + "debug = false\n"
            + "placement = false\n"
            + "blitz = false\n"
            + "timeout = 30\n"
            + "ai = true\n"
            + "ai_color = NONE\n"
            + "ai_mode = minimax\n"
            + "ai_depth = 4\n"
            + "ai_time_limit = 5\n"
            + "ai_iterative_deepening = true\n"
            + "ai_heuristic = mixed\n"
            + "\n"
            + "[game]\n"
            + "O\n"
            + "     . X . . O .\n"
            + "    O . . . . . X\n"
            + "   . . . . . . . .\n"
            + "  X . . . . . . . O\n"
            + " . . . . . . . . . .\n"
            + "Q . . . . . . . . . q\n"
            + " . . . . . . . . . .\n"
            + "  X . . . . . . . O\n"
            + "   . . . . . . . .\n"
            + "    O . . . . . X\n"
            + "     . X . . O .\n"
            + "\n"
            + "[history]\n";

    Path saveFile = tempDir.resolve("test_save.asv");
    Files.writeString(saveFile, saveContent);

    CmdAction cmd = cmds.get("load").get().createNew(new String[] {saveFile.toString()});
    assertNotNull(cmd);

    boolean result = cmd.execute(null);

    assertTrue(result, "Load execution should succeed");
    assertNotNull(engine.getMatchManager(), "A MatchManager must be present in the engine");
    assertTrue(outContent.toString().contains("Game successfully loaded from:"));
  }

  @Test
  @DisplayName("Error when loading a non-existent file")
  void testExecuteLoadFileNotFound() {
    String fakePath = tempDir.resolve("ghost.asv").toString();
    CmdAction cmd = cmds.get("load").get().createNew(new String[] {fakePath});

    boolean result = cmd.execute(null);

    assertFalse(result);
    assertTrue(
        outContent.toString().contains("Failed to load game"),
        "The UI must display a read error");
  }

  @Test
  @DisplayName("Error when loading a corrupted file")
  void testExecuteLoadCorruptedFile() throws Exception {
    Path corruptedFile = tempDir.resolve("corrupt.asv");
    Files.writeString(corruptedFile, "[invalid_header]\nnonsense content");

    CmdAction cmd = cmds.get("load").get().createNew(new String[] {corruptedFile.toString()});
    boolean result = cmd.execute(null);

    assertFalse(result);
    assertTrue(outContent.toString().contains("Failed to load game"));
  }

  @Test
  @DisplayName("Check getName and getDescription")
  void testBasics() {
    CmdLoad prototype = (CmdLoad) cmds.get("load").get();
    assertEquals("load", prototype.getName());
    assertTrue(prototype.getDescription().contains("Usage: load [filename]"));
    assertNotNull(prototype.getOptions());
  }
}