package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
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

public class CmdRedoTest {
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface gameUserInterface;

  @BeforeEach
  void setUp() {
    LineReader reader = new FakeLineReader("");
    try {
      Terminal terminal = new FakeTerminal(new ByteArrayOutputStream());
      gameUserInterface = new AgonShell(terminal, reader, cmds);
      cmds.register("redo", new CmdRedo(gameUserInterface));
    } catch (Exception e) {
      fail("Setup failed");
    }
  }

  @Test
  @DisplayName("Verify that a redo correctly restores an undone move")
  void executeRedoTest() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(
            board,
            new HumanPlayer("P1", Color.WHITE, gameUserInterface),
            new HumanPlayer("P2", Color.BLACK, gameUserInterface),
            new GameConfig());

    int fromW = CoordinateMapper.toIndex('B', 1);
    int toW = CoordinateMapper.toIndex('C', 1);
    PieceType whiteP = board.getPieceAt(fromW);
    new CmdMove(fromW, toW, gameUserInterface).execute(match);

    int fromB = CoordinateMapper.toIndex('F', 11);
    int toB = CoordinateMapper.toIndex('F', 10);
    PieceType blackQ = board.getPieceAt(fromB);
    new CmdMove(fromB, toB, gameUserInterface).execute(match);
    match.undo();

    assertEquals(whiteP, board.getPieceAt(fromW), "White pawn should be at B1");
    assertEquals(blackQ, board.getPieceAt(fromB), "Black queen should be at F11");
    assertNull(board.getPieceAt(toW));
    assertNull(board.getPieceAt(toB));

    CmdAction cmdRedo = cmds.get("redo").get().createNew(new String[] {});
    cmdRedo.execute(match);

    assertNull(board.getPieceAt(fromW));
    assertEquals(whiteP, board.getPieceAt(toW), "White pawn should have returned to C1");

    assertNull(board.getPieceAt(fromB));
    assertEquals(blackQ, board.getPieceAt(toB), "Black queen should have returned to F10");
  }

  @Test
  @DisplayName("Verify that redo without anything to restore does not crash")
  void executeRedoEmptyTest() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(
            board, new HumanPlayer("P1", Color.WHITE, gameUserInterface), null, new GameConfig());

    CmdAction cmdRedo = cmds.get("redo").get().createNew(new String[] {"1"});
    boolean result = cmdRedo.execute(match);

    assertFalse(result);
  }

  @Test
  @DisplayName("Verify createNew with a number argument")
  void createNewTest() {
    CmdAction cmdRedo = cmds.get("redo").get().createNew(new String[] {"5"});
    assertNotNull(cmdRedo);
    assertFalse(cmdRedo.execute(null));
  }

  @Test
  @DisplayName("Verify description content")
  void getDescriptionTest() {
    CmdAction cmdRedo = cmds.get("redo").get().createNew(new String[] {});
    String desc = cmdRedo.getDescription();
    assertNotNull(desc);
    assertTrue(desc.contains("Usage: redo [N]"));
  }

  @Test
  @DisplayName("Coverage: Redo when there are no more moves to restore (Warning)")
  void testRedoMoreThanPossible() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(
            board,
            new HumanPlayer("P1", Color.WHITE, gameUserInterface),
            new HumanPlayer("P2", Color.BLACK, gameUserInterface),
            new GameConfig());

    match.move(
        new Move(CoordinateMapper.toIndex('B', 1), CoordinateMapper.toIndex('C', 1), Color.WHITE));
    match.undo();

    CmdAction cmdRedo = cmds.get("redo").get().createNew(new String[] {"5"});
    boolean result = cmdRedo.execute(match);

    assertFalse(result);
  }

  @Test
  @DisplayName("Coverage: Invalid number format for redo")
  void testRedoInvalidFormat() {
    CmdAction cmd = cmds.get("redo").get().createNew(new String[] {"abc"});
    assertNotNull(cmd);
  }

  @Test
  @DisplayName("Coverage: Reach 100% on execute (if !match.redo branches)")
  void testExecuteBranches() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(
            board,
            new HumanPlayer("P1", Color.WHITE, gameUserInterface),
            new HumanPlayer("P2", Color.BLACK, gameUserInterface),
            new GameConfig());

    assertTrue(
        match.move(
            new Move(
                CoordinateMapper.toIndex('F', 1), CoordinateMapper.toIndex('F', 2), Color.WHITE)));
    assertTrue(
        match.move(
            new Move(
                CoordinateMapper.toIndex('F', 11),
                CoordinateMapper.toIndex('F', 10),
                Color.BLACK)));
    assertTrue(
        match.move(
            new Move(
                CoordinateMapper.toIndex('F', 2), CoordinateMapper.toIndex('F', 3), Color.WHITE)));
    assertTrue(
        match.move(
            new Move(
                CoordinateMapper.toIndex('F', 10), CoordinateMapper.toIndex('F', 9), Color.BLACK)));
    match.undo();
    match.undo();

    CmdAction cmd = cmds.get("redo").get().createNew(new String[] {"3"});
    boolean result = cmd.execute(match);

    assertFalse(result);
  }
}