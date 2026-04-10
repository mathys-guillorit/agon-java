package fr.univ.bordeaux.application.commands.specialized;

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
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class CmdMoveTest {

  private AgonRegister<CmdAction> cmds;
  private GameUserInterface gameUserInterface;
  private ByteArrayOutputStream outContent;

  @BeforeEach
  void setUp() {
    cmds = new AgonRegister<>();
    LineReader reader = new FakeLineReader("");

    try {
      outContent = new ByteArrayOutputStream();
      Terminal terminal = new FakeTerminal(outContent);
      gameUserInterface = new AgonShell(terminal, reader, cmds);
    } catch (Exception e) {
      fail("Setup failed");
    }
  }

  private MatchManager newStandardMatch() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();

    return new StandardMatch(
        board,
        new HumanPlayer("J1", Color.WHITE, gameUserInterface),
        new HumanPlayer("J2", Color.BLACK, gameUserInterface),
        new GameConfig());
  }

  @Test
  @DisplayName("execute returns false and shows error when match is null")
  void execute_returns_false_and_shows_error_when_match_is_null() {
    CmdMove cmdMove = new CmdMove(0, 1, gameUserInterface);

    boolean result = cmdMove.execute(null);

    assertFalse(result);
    String output = outContent.toString();
    assertTrue(output.contains("No active match to execute move."));
  }

  @Test
  @DisplayName("execute applies a valid move when command is built from coordinates")
  void execute_applies_a_valid_move_when_command_is_built_from_coordinates() {
    MatchManager match = newStandardMatch();
    AgonBoard board = match.getAgonBoard();

    int fromIndex = CoordinateMapper.toIndex('B', 1);
    int toIndex = CoordinateMapper.toIndex('C', 1);

    PieceType pieceToMove = board.getPieceAt(fromIndex);
    assertNotNull(pieceToMove);
    assertEquals(Color.WHITE, pieceToMove.getColor());

    CmdMove cmdMove = new CmdMove(fromIndex, toIndex, gameUserInterface);

    boolean result = cmdMove.execute(match);

    assertTrue(result);
    assertNull(board.getPieceAt(fromIndex));
    assertEquals(pieceToMove, board.getPieceAt(toIndex));
  }

  @Test
  @DisplayName("execute applies a valid move when command is built from a Move object")
  void execute_applies_a_valid_move_when_command_is_built_from_a_move_object() {
    MatchManager match = newStandardMatch();
    AgonBoard board = match.getAgonBoard();

    int fromIndex = CoordinateMapper.toIndex('B', 1);
    int toIndex = CoordinateMapper.toIndex('C', 1);

    PieceType pieceToMove = board.getPieceAt(fromIndex);
    assertNotNull(pieceToMove);
    assertEquals(Color.WHITE, pieceToMove.getColor());

    CmdMove cmdMove = new CmdMove(new Move(fromIndex, toIndex, Color.WHITE), gameUserInterface);

    boolean result = cmdMove.execute(match);

    assertTrue(result);
    assertNull(board.getPieceAt(fromIndex));
    assertEquals(pieceToMove, board.getPieceAt(toIndex));
  }

  @Test
  @DisplayName("execute shows warning when move is invalid")
  void execute_shows_warning_when_move_is_invalid() {
    MatchManager match = newStandardMatch();

    int emptyFrom = CoordinateMapper.toIndex('E', 5);
    int to = CoordinateMapper.toIndex('E', 6);

    CmdMove cmdMove = new CmdMove(emptyFrom, to, gameUserInterface);

    boolean result = cmdMove.execute(match);

    assertFalse(result);
    String output = outContent.toString();
    assertTrue(output.contains("Invalid move attempt."));
  }

  @Test
  @DisplayName("getFrom returns raw source index when command is built from coordinates")
  void get_from_returns_raw_source_index_when_command_is_built_from_coordinates() {
    CmdMove cmdMove = new CmdMove(12, 34, gameUserInterface);

    assertEquals(12, cmdMove.getFrom());
  }

  @Test
  @DisplayName("getFrom returns move source index when command is built from Move object")
  void get_from_returns_move_source_index_when_command_is_built_from_move_object() {
    Move move = new Move(21, 42, Color.WHITE);
    CmdMove cmdMove = new CmdMove(move, gameUserInterface);

    assertEquals(21, cmdMove.getFrom());
  }

  @Test
  @DisplayName(
      "getDestination returns raw destination index when command is built from coordinates")
  void get_destination_returns_raw_destination_index_when_command_is_built_from_coordinates() {
    CmdMove cmdMove = new CmdMove(12, 34, gameUserInterface);

    assertEquals(34, cmdMove.getDestination());
  }

  @Test
  @DisplayName(
      "getDestination returns move destination index when command is built from Move object")
  void get_destination_returns_move_destination_index_when_command_is_built_from_move_object() {
    Move move = new Move(21, 42, Color.WHITE);
    CmdMove cmdMove = new CmdMove(move, gameUserInterface);

    assertEquals(42, cmdMove.getDestination());
  }

  @Test
  @DisplayName("createNew returns null")
  void create_new_returns_null() {
    CmdMove cmdMove = new CmdMove(0, 0, gameUserInterface);

    assertNull(cmdMove.createNew(null));
    assertNull(cmdMove.createNew(new String[0]));
    assertNull(cmdMove.createNew(new String[] {"A1", "B1"}));
  }

  @Test
  @DisplayName("getDescription returns move command description")
  void get_description_returns_move_command_description() {
    CmdMove cmdMove = new CmdMove(0, 0, gameUserInterface);

    assertNotNull(cmdMove.getDescription());
    assertTrue(
        cmdMove
            .getDescription()
            .contains(
                "move <from> <to>\nDescription: Moves a piece from one coordinate to another.\n"),
        cmdMove.getDescription());
  }

  @Test
  @DisplayName("getAutoCompleter delegates to parent implementation")
  void get_auto_completer_delegates_to_parent_implementation() {
    CmdMove cmdMove = new CmdMove(0, 0, gameUserInterface);

    Completer completer = cmdMove.getAutoCompleter();

    assertNotNull(completer);
  }
}
