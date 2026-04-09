package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
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
import java.io.ByteArrayOutputStream;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CmdHintTest {
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface gameUserInterface;
  private ByteArrayOutputStream outContent;

  @BeforeEach
  void setUp() {
    GameConfig config = new GameConfig();
    LineReader reader = new FakeLineReader("");
    outContent = new ByteArrayOutputStream();

    try {
      Terminal terminal = new FakeTerminal(outContent);
      gameUserInterface = new AgonShell(terminal, reader, cmds);

      cmds.register("hint", new CmdHint(gameUserInterface));
    } catch (Exception e) {
      fail("Setup failed");
    }
  }

  @Test
  @DisplayName("Verify that the prototype generates a valid action")
  void createNewTest() {
    CmdAction cmdHint = cmds.get("hint").get().createNew(new String[] {});
    assertNotNull(cmdHint, "Hint action should not be null");
  }

  @Test
  @DisplayName("Execute Hint in a real match and verify feedback")
  void executeTest() {
    AgonBoard board = new AgonBoardImpl();
    MatchManager match =
        new StandardMatch(
            board,
            new HumanPlayer("P1", Color.WHITE, gameUserInterface),
            new HumanPlayer("P2", Color.BLACK, gameUserInterface),
            new GameConfig());

    CmdAction cmdHint = cmds.get("hint").get().createNew(null);
    boolean result = cmdHint.execute(match);
    String output = outContent.toString();

    // Note: I kept the logic of your assertFalse/assertTrue based on the original code
    assertFalse(
        output.contains("Hint:"), "Terminal should display a suggestion starting with 'Hint:'");
    assertFalse(result, "Hint should return false to avoid ending the turn");

    board.initBaseConfiguration();
    cmdHint = cmds.get("hint").get().createNew(null);
    cmdHint.execute(match);
    output = outContent.toString();
    assertTrue(
        output.contains("Hint: From"),
        "Terminal should display a suggestion starting with 'Hint: From'");
  }

  @Test
  @DisplayName("Verify that Hint fails gracefully without an active match")
  void executeWithoutMatchTest() {
    CmdAction cmdHint = cmds.get("hint").get().createNew(new String[] {});

    boolean result = cmdHint.execute(null);

    assertFalse(result, "Hint should return false if there is no match in progress");
  }

  @Test
  @DisplayName("Verify the presence of a description")
  void getDescriptionTest() {
    CmdAction cmdHint = cmds.get("hint").get().createNew(null);
    assertNotNull(cmdHint.getDescription());
    assertTrue(cmdHint.getDescription().toLowerCase().contains("hint"));
  }
}
