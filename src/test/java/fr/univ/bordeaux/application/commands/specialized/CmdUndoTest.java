package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
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

public class CmdUndoTest {
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface gameUserInterface;

  @BeforeEach
  void setUp() {
    LineReader reader = new FakeLineReader("");
    try {
      Terminal terminal = new FakeTerminal(new ByteArrayOutputStream());
      gameUserInterface = new AgonShell(terminal, reader, cmds);
      cmds.register("undo", new CmdUndo(gameUserInterface));
    } catch (Exception e) {
      fail("Setup failed");
    }
  }

  @Test
  @DisplayName("Verify that undo correctly reverses a move on the board")
  void executeUndoTest() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(
            board,
            new HumanPlayer("P1", Color.WHITE, gameUserInterface),
            new HumanPlayer("P2", Color.BLACK, gameUserInterface),
            new GameConfig());

    int from = CoordinateMapper.toIndex('B', 1);
    int to = CoordinateMapper.toIndex('C', 1);
    PieceType piece = board.getPieceAt(from);

    CmdAction move = new CmdMove(from, to, gameUserInterface);
    move.execute(match);
    assertNull(board.getPieceAt(from));
    assertEquals(piece, board.getPieceAt(to));
    new CmdMove(
        CoordinateMapper.toIndex('F', 11), CoordinateMapper.toIndex('F', 10), gameUserInterface)
        .execute(match);

    CmdAction cmdUndo = cmds.get("undo").get().createNew(new String[] {"1"});
    boolean result = cmdUndo.execute(match);

    assertTrue(result, "Undo execution should return true");
    assertEquals(piece, board.getPieceAt(from), "The piece should be back at its starting position");
    assertNull(board.getPieceAt(to), "The destination square should be empty after undo");
    assertEquals(
        Color.WHITE,
        match.getCurrentPlayer().getColor(),
        "The turn should have returned to the white player");
  }

  @Test
  @DisplayName("Verify that multiple undo cancels several moves")
  void executeMultipleUndoTest() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(
            board,
            new HumanPlayer("P1", Color.WHITE, gameUserInterface),
            new HumanPlayer("P2", Color.BLACK, gameUserInterface),
            new GameConfig());

    new CmdMove(
        CoordinateMapper.toIndex('B', 1), CoordinateMapper.toIndex('C', 1), gameUserInterface)
        .execute(match);
    new CmdMove(
        CoordinateMapper.toIndex('F', 11), CoordinateMapper.toIndex('F', 10), gameUserInterface)
        .execute(match);
    new CmdMove(
        CoordinateMapper.toIndex('F', 1), CoordinateMapper.toIndex('F', 2), gameUserInterface)
        .execute(match);
    new CmdMove(
        CoordinateMapper.toIndex('A', 2), CoordinateMapper.toIndex('A', 3), gameUserInterface)
        .execute(match);

    CmdAction cmdUndo = cmds.get("undo").get().createNew(new String[] {"2"});
    cmdUndo.execute(match);

    assertNotNull(board.getPieceAt(CoordinateMapper.toIndex('B', 1)));
    assertNotNull(board.getPieceAt(CoordinateMapper.toIndex('F', 1)));
  }

  @Test
  @DisplayName("Verify that undo without history does not crash and stops via break")
  void executeUndoEmptyTest() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(
            board, new HumanPlayer("P1", Color.WHITE, gameUserInterface), null, new GameConfig());

    CmdAction cmdUndo = cmds.get("undo").get().createNew(new String[] {"1"});
    boolean result = cmdUndo.execute(match);

    assertTrue(result);
  }

  @Test
  @DisplayName("Verify createNew with different argument formats")
  void createNewTest() {

    CmdAction cmdUndo1 = cmds.get("undo").get().createNew(new String[] {"2"});
    assertNotNull(cmdUndo1);

    CmdAction cmdUndo2 = cmds.get("undo").get().createNew(new String[] {"-n", "3"});
    assertNotNull(cmdUndo2);

    CmdAction cmdUndoErr = cmds.get("undo").get().createNew(new String[] {"abc"});
    assertNull(cmdUndoErr);
  }

  @Test
  @DisplayName("Verify undo description")
  void getDescriptionTest() {
    CmdAction cmdUndo = cmds.get("undo").get().createNew(new String[] {});
    assertTrue(cmdUndo.getDescription().contains("Usage: undo [N]"));
  }
}