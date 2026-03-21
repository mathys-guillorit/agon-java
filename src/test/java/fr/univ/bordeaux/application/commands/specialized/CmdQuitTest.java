package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.match.StandardMatch;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.technical.config.GameConfig;
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

public class CmdQuitTest {
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface gameUserInterface;

  @BeforeEach
  void setUp() {
    GameConfig config = new GameConfig();
    LineReader reader = new FakeLineReader("n");
    try {
      Terminal terminal = createFakeTerminal();
      gameUserInterface = new AgonShell(terminal, reader, cmds);
      cmds.register("quit", new CmdQuit(gameUserInterface));
    } catch (Exception e) {
    }
  }

  private Terminal createFakeTerminal() {
    return new FakeTerminal(new ByteArrayOutputStream());
  }

  @Test
  @DisplayName("try to create new using prototype")
  void createNewTest() {
    CmdAction cmdQuit = cmds.get("quit").get().createNew(null);
    assertTrue(cmdQuit != null);
  }

  @Test
  @DisplayName("try to create new using prototype")
  void executeTest() {
    MatchManager match =
        new StandardMatch(
            new AgonBoardImpl(),
            new HumanPlayer("test", Color.WHITE, gameUserInterface),
            new HumanPlayer("test", Color.WHITE, gameUserInterface));
    CmdAction cmdQuit = cmds.get("quit").get().createNew(null);
    cmdQuit.execute(match);
    assertFalse(gameUserInterface.isRunning());
  }

  @Test
  @DisplayName("try to create new using prototype")
  void getDescriptionTest() {
    CmdAction cmdQuit = cmds.get("quit").get().createNew(null);
    String expected =
        "Usage: quit (or Ctrl+C)\n"
            + "Description: Exits the game. You will be prompted to save your current progress before leaving.\n";
    assertEquals(expected, cmdQuit.getDescription());
  }
}
