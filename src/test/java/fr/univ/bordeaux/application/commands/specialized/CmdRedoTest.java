package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.*;

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
      // Enregistrement du prototype
      cmds.register("redo", new CmdRedo(gameUserInterface));
    } catch (Exception e) {
      fail("Setup failed");
    }
  }

  @Test
  @DisplayName("Vérifier qu'un redo restaure bien un mouvement annulé")
  void executeRedoTest() {
    // 1. Initialisation
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(
            board,
            new HumanPlayer("J1", Color.WHITE, gameUserInterface),
            new HumanPlayer("J2", Color.BLACK, gameUserInterface));

    int from = CoordinateMapper.toIndex('B', 1);
    int to = CoordinateMapper.toIndex('C', 1);
    PieceType piece = board.getPieceAt(from);

    // 2. On joue un coup
    CmdAction move = new CmdMove(from, to, gameUserInterface);
    move.execute(match);
    assertNull(board.getPieceAt(from));
    assertEquals(piece, board.getPieceAt(to));

    // 3. On annule le coup (Undo)
    match.undo();
    assertEquals(piece, board.getPieceAt(from), "Après undo, la pièce doit être revenue au départ");
    assertNull(board.getPieceAt(to));

    // 4. On rétablit le coup (Redo via la commande)
    CmdAction cmdRedo = cmds.get("redo").get().createNew(new String[] {"3"});
    boolean result = cmdRedo.execute(match);

    // 5. VÉRIFICATIONS
    assertTrue(result);
    assertNull(board.getPieceAt(from), "Après redo, la case de départ doit être à nouveau vide");
    assertEquals(
        piece,
        board.getPieceAt(to),
        "Après redo, la pièce doit être revenue sur la case d'arrivée");
    match.undo();
    cmdRedo = cmds.get("redo").get().createNew(new String[] {});
    cmdRedo.execute(match);
    assertNull(board.getPieceAt(from), "Après redo, la case de départ doit être à nouveau vide");
    assertEquals(
        piece,
        board.getPieceAt(to),
        "Après redo, la pièce doit être revenue sur la case d'arrivée");
  }

  @Test
  @DisplayName("Vérifier que redo sans rien à rétablir ne crash pas")
  void executeRedoEmptyTest() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(board, new HumanPlayer("J1", Color.WHITE, gameUserInterface), null);

    CmdAction cmdRedo = cmds.get("redo").get().createNew(new String[] {"1"});

    // Pas de undo préalable, donc rien à redo
    boolean result = cmdRedo.execute(match);

    assertTrue(
        result,
        "La commande doit renvoyer true même s'il n'y a rien à faire (comportement standard)");
  }

  @Test
  @DisplayName("Vérifier createNew avec un nombre")
  void createNewTest() {
    CmdAction cmdRedo = cmds.get("redo").get().createNew(new String[] {"5"});
    assertNotNull(cmdRedo);
    // On vérifie indirectement que le nombre est accepté (pas d'exception)
    assertFalse(cmdRedo.execute(null)); // False car match est null
  }

  @Test
  @DisplayName("Vérifier la description")
  void getDescriptionTest() {
    CmdAction cmdRedo = cmds.get("redo").get().createNew(new String[] {});
    String desc = cmdRedo.getDescription();
    assertNotNull(desc);
    assertTrue(desc.contains("Usage: redo [N]"));
  }
}
