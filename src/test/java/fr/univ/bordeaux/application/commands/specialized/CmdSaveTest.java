package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.match.StandardMatch;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
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
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CmdSaveTest {

  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface ui;
  private MatchManager match;
  private ByteArrayOutputStream outContent;

  @TempDir Path tempDir;

  @BeforeEach
  void setUp() {
    outContent = new ByteArrayOutputStream();
    LineReader reader = new FakeLineReader("");
    try {
      Terminal terminal = new FakeTerminal(outContent);
      ui = new AgonShell(terminal, reader, cmds);

      match =
          new StandardMatch(
              new AgonBoardImpl(),
              new HumanPlayer("P1", Color.WHITE, ui),
              new HumanPlayer("P2", Color.BLACK, ui),
              new GameConfig());

      cmds.register("save", new CmdSave(ui));
    } catch (Exception e) {
      fail("Setup failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Successful save with a specific filename")
  void testSaveWithFilename() throws Exception {

    String filePath = tempDir.resolve("my_save.asv").toString();

    CmdAction cmd = cmds.get("save").get().createNew(new String[] {filePath});

    boolean result = cmd.execute(match);

    // Assuming the command returns false to not end the turn
    assertFalse(result);

    File file = new File(filePath);
    assertTrue(file.exists(), "The save file should exist on disk");
    assertTrue(file.length() > 0, "The file should not be empty");
    assertTrue(match.isSaved(), "The match isSaved flag should be true");
  }

  @Test
  @DisplayName("Default save if no filename is provided")
  void testSaveDefaultFilename() {

    CmdAction cmd = cmds.get("save").get().createNew(new String[] {});

    cmd.execute(match);

    File defaultFile = new File("default_save");
    if (defaultFile.exists()) {
      defaultFile.delete();
    }
  }

  @Test
  @DisplayName("Error handling during writing (IOException)")
  void testSaveErrorHandling() {

    String invalidPath = "/this/path/does/not/exist/save.asv";

    CmdAction cmd = cmds.get("save").get().createNew(new String[] {invalidPath});
    boolean result = cmd.execute(match);

    assertFalse(result);
    assertTrue(outContent.toString().contains("Something went wrong while saving"));
  }

  @Test
  @DisplayName("Verification of command metadata")
  void testCommandMetadata() {
    CmdAction prototype = cmds.get("save").get();
    assertTrue(cmds.get("save").isPresent(), "cmd must exists");
    assertEquals("save", prototype.getName());
    final String cmdDesc = prototype.getDescription();
    assertTrue(cmdDesc.contains("save [filename]"));
    var msg = new StringBuilder();
    msg.append("Description: Saves the current game state to the specified");
    msg.append(" file,if there is no filename save by ");
    assertTrue(cmdDesc.contains(msg.append("default in default_save.\n").toString()), cmdDesc);
  }
}
