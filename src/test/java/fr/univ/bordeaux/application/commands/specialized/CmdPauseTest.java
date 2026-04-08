package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.BlitzMatch;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.application.match.player.Player;
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

public class CmdPauseTest {
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface gameUserInterface;
  private ByteArrayOutputStream output = new ByteArrayOutputStream();

  @BeforeEach
  void setUp() {
    LineReader reader = new FakeLineReader("n");
    try {
      Terminal terminal = new FakeTerminal(output);
      gameUserInterface = new AgonShell(terminal, reader, cmds);
    } catch (Exception e) {
      fail("Setup failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Verify that CmdPause.execute pauses the match and returns false")
  void testExecutePause() throws InterruptedException {
    CmdPause cmdPause = new CmdPause(gameUserInterface);
    cmdPause.execute(null);
    assertTrue(
        output
            .toString()
            .contains(
                "You must create a match before using this command. Type help for more informations"));
    AgonBoardImpl board = new AgonBoardImpl();
    board.initBaseConfiguration();
    Player p1 = new HumanPlayer("White", Color.WHITE, gameUserInterface);
    Player p2 = new HumanPlayer("Black", Color.BLACK, gameUserInterface);

    BlitzMatch match = new BlitzMatch(board, p1, p2, 1, new GameConfig());
    String time1 = match.getCurrentPlayerRemainingTime();
    Thread.sleep(1100);

    String time2 = match.getCurrentPlayerRemainingTime();
    assertNotEquals(time1, time2, "Time should have elapsed before the pause");

    boolean result = cmdPause.execute(match);
    assertFalse(result, "Pause command must return false to avoid skipping the turn");

    String timePaused = match.getCurrentPlayerRemainingTime();

    Thread.sleep(1100);

    String timeAfterWait = match.getCurrentPlayerRemainingTime();
    assertEquals(
        timePaused, timeAfterWait, "Time should not have elapsed during the pause");
  }

  @Test
  @DisplayName("Verify the pause command description")
  void testGetDescription() {
    CmdPause cmdPause = new CmdPause(gameUserInterface);
    assertTrue(cmdPause.getDescription().contains("Usage: pause"));
    assertTrue(cmdPause.getDescription().contains("Pauses the game timers"));
  }

  @Test
  @DisplayName("Verify createNew method")
  void testCreateNew() {
    CmdPause cmdPause = new CmdPause(gameUserInterface);
    CmdAction newAction = cmdPause.createNew(new String[] {});
    assertNotNull(newAction);
    assertInstanceOf(CmdPause.class, newAction);
  }
}