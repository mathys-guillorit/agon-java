package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.BlitzMatch;
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

public class CmdShowTest {
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

      // Registering the prototype
      cmds.register("show", new CmdShow(gameUserInterface, config));
    } catch (Exception e) {
      fail("Setup failed");
    }
  }

  @Test
  @DisplayName("Verify configuration display")
  void testShowConfiguration() {
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-configuration"});
    assertNotNull(cmd);

    boolean result = cmd.execute(null);

    assertTrue(result);
    assertTrue(outContent.toString().contains(config.toString()));
  }

  @Test
  @DisplayName("Verify board display")
  void testShowBoard() {
    MatchManager match =
        new StandardMatch(
            new AgonBoardImpl(),
            new HumanPlayer("P1", Color.WHITE, gameUserInterface),
            null,
            new GameConfig());

    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-board"});
    boolean result = cmd.execute(match);

    assertFalse(result);
  }

  @Test
  @DisplayName("Verify error when no match is present for board display")
  void testShowBoardNoMatch() {
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-board"});

    boolean result = cmd.execute(null);

    assertFalse(result);
    assertTrue(outContent.toString().contains("Error: No active match"));
  }

  @Test
  @DisplayName("Verify error when no target is specified")
  void testShowNoTarget() {
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {});
    boolean result = cmd.execute(null);

    assertFalse(result);
    assertTrue(
        outContent.toString().contains("No target specified. Use 'help show' for details.\n"));
  }

  @Test
  @DisplayName("Verify rejection of multiple simultaneous targets")
  void testMultipleTargetsError() {
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-board", "-history"});

    assertNull(cmd, "The factory createNew should return null if more than one option is provided");
    assertTrue(outContent.toString().contains("Please specify only one target"));
  }

  @Test
  @DisplayName("Verify history display")
  void testShowHistory() {
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-history"});
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new BlitzMatch(
            board,
            new HumanPlayer("test", Color.WHITE, gameUserInterface),
            new HumanPlayer("test2", Color.BLACK, gameUserInterface),
            1,
            new GameConfig());
    match.move(
        new Move(
            CoordinateMapper.toIndex('F', 1),
            CoordinateMapper.toIndex('F', 2),
            match.getCurrentPlayer().getColor()));
    match.move(
        new Move(
            CoordinateMapper.toIndex('F', 11),
            CoordinateMapper.toIndex('F', 10),
            match.getCurrentPlayer().getColor()));
    boolean result = cmd.execute(match);
    assertFalse(result);
    assertTrue(outContent.toString().contains("[history]\nO f1 f2; X f11 f10;\n"));
  }

  @Test
  @DisplayName("Verify -time option (should return false to not skip turn)")
  void testShowTimeLogic() {
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-time"});
    boolean result = cmd.execute(null);
    assertFalse(result);
    assertTrue(
        outContent
            .toString()
            .contains("This command can only be used when you are currently in a blitz match"),
        "The display should inform that it's for blitz matches only");

    MatchManager matchBlitz =
        new BlitzMatch(
            new AgonBoardImpl(),
            new HumanPlayer("test", Color.WHITE, gameUserInterface),
            new HumanPlayer("test2", Color.BLACK, gameUserInterface),
            1,
            new GameConfig());
    result = cmd.execute(matchBlitz);
    assertFalse(result, "The show command should not consume the player's turn");

    String output = outContent.toString();
    assertTrue(
        output.contains(
            "Remaining time for : "
                + matchBlitz.getCurrentPlayer().getName()
                + "( "
                + matchBlitz.getCurrentPlayer().getColor()
                + " ) : "),
        "The formatted time should be present in output");
  }

  @Test
  @DisplayName("Verify handling of invalid target")
  void testInvalidTarget() {
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-unknown"});

    assertNull(cmd);
    assertTrue(outContent.toString().contains("Invalid show command"));
  }

  @Test
  @DisplayName("Check getDescription")
  void testGetDescription() {
    String expected =
        "Usage: show [target]\n"
            + "Description: Displays specific information about the current game state.\n"
            + "Available targets:\n"
            + "  -board         : Shows the current hexagonal board state.\n"
            + "  -history       : Shows the history of all played turns.\n"
            + "  -time          : Shows the remaining time for each player.\n"
            + "  -configuration : Shows the current game settings.\n";

    assertEquals(expected, cmds.get("show").get().getDescription());
  }
}
