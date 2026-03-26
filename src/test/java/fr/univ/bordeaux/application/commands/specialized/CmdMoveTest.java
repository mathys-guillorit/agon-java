package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.*;

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

public class CmdMoveTest {
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface gameUserInterface;
  private ByteArrayOutputStream outContent;

  @BeforeEach
  void setUp() {
    LineReader reader = new FakeLineReader("");
    try {
      outContent = new ByteArrayOutputStream();
      Terminal terminal = new FakeTerminal(outContent);
      gameUserInterface = new AgonShell(terminal, reader, cmds);
    } catch (Exception e) {
      fail("Setup failed");
    }
  }

  @Test
  @DisplayName("Vérifier qu'un mouvement valide déplace bien la pièce sur le plateau")
  void executeValidMoveTest() {
    // 1. Initialisation d'un plateau et d'un match
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration(); // Place les pièces aux positions de départ

    MatchManager match =
        new StandardMatch(
            board,
            new HumanPlayer("J1", Color.WHITE, gameUserInterface),
            new HumanPlayer("J2", Color.BLACK, gameUserInterface));

    // 2. Définition du mouvement (Exemple : déplacer un pion blanc de b1 vers c1)
    // On utilise CoordinateMapper pour être sûr des index
    int fromIndex = CoordinateMapper.toIndex('B', 1);
    int toIndex = CoordinateMapper.toIndex('C', 1);

    // On vérifie qu'il y a bien une pièce blanche au départ
    PieceType pieceToMove = board.getPieceAt(fromIndex);
    assertNotNull(pieceToMove, "Il doit y avoir une pièce en B1");
    assertEquals(Color.WHITE, pieceToMove.getColor());

    CmdAction cmdMove = new CmdMove(fromIndex, toIndex, gameUserInterface);

    Color colorBefore = match.getCurrentPlayer().getColor();
    boolean result = cmdMove.execute(match);

    // 4. VÉRIFICATIONS
    assertTrue(result, "Le mouvement valide doit renvoyer true");

    // Vérification sur le plateau
    assertNull(board.getPieceAt(fromIndex), "La case de départ doit être vide");
    assertEquals(
        pieceToMove, board.getPieceAt(toIndex), "La pièce doit être sur la case d'arrivée");
    cmdMove =
        new CmdMove(
            new Move(
                CoordinateMapper.toIndex('F', 11), CoordinateMapper.toIndex('F', 10), Color.BLACK),
            gameUserInterface);
    PieceType pieceToMove2 = board.getPieceAt(CoordinateMapper.toIndex('F', 11));
    assertNotNull(pieceToMove2, "Il doit y avoir une pièce en F11");
    assertEquals(Color.BLACK, pieceToMove2.getColor());
    result = cmdMove.execute(match);

    // 4. VÉRIFICATIONS
    assertTrue(result, "Le mouvement valide doit renvoyer true");
    assertNull(
        board.getPieceAt(CoordinateMapper.toIndex('F', 11)), "La case de départ doit être vide");
    assertEquals(
        pieceToMove2,
        board.getPieceAt(CoordinateMapper.toIndex('F', 10)),
        "La pièce doit être sur la case d'arrivée");
  }

  @Test
  @DisplayName("Vérifier qu'un mouvement invalide est refusé")
  void executeInvalidMoveTest() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(board, new HumanPlayer("J1", Color.WHITE, gameUserInterface), null);
    // Tentative de déplacer une case vide (ex: e5 qui est vide au début)
    int emptyFrom = CoordinateMapper.toIndex('E', 5);
    int to = CoordinateMapper.toIndex('E', 6);
    CmdAction cmdMove = new CmdMove(emptyFrom, to, gameUserInterface);
    boolean result = cmdMove.execute(match);

    assertFalse(result, "Un mouvement depuis une case vide doit échouer (false)");
    cmdMove = new CmdMove(emptyFrom, to, gameUserInterface);
    cmdMove.execute(null);
    String output = outContent.toString();
    assertTrue(output.contains("Invalid move attempt."));
  }

  @Test
  @DisplayName("createNew is null")
  void createNewNullTest() {
    CmdAction cmdMove = new CmdMove(0, 0, gameUserInterface);
    assertNull(cmdMove.createNew(null));
  }

  @Test
  @DisplayName("getDescription")
  void getDescritpionTest() {
    CmdAction cmdMove = new CmdMove(0, 0, gameUserInterface);
    assertNotNull(cmdMove.getDescription());
    assertTrue(
        cmdMove
            .getDescription()
            .contains(
                "Usage: move <from> <to>\nDescription: Moves a piece from one coordinate to another.\n"));
  }
}
