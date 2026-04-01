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
            new HumanPlayer("J2", Color.BLACK, gameUserInterface),
            new GameConfig());

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
    assertFalse(result, "Redo doit renvoyer false pour ne pas passer le tour"); // CHANGÉ ICI
    assertNull(board.getPieceAt(from));
    assertEquals(piece, board.getPieceAt(to));

    match.undo();
    cmdRedo = cmds.get("redo").get().createNew(new String[] {});
    boolean result2 = cmdRedo.execute(match);
    assertFalse(result2, "Redo doit renvoyer false");
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
        new StandardMatch(
            board, new HumanPlayer("J1", Color.WHITE, gameUserInterface), null, new GameConfig());

    CmdAction cmdRedo = cmds.get("redo").get().createNew(new String[] {"1"});
    boolean result = cmdRedo.execute(match);

    assertFalse(result);
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

  @Test
  @DisplayName("Couverture : Redo quand il n'y a plus de coups à rétablir (Warning)")
  void testRedoMoreThanPossible() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(
            board,
            new HumanPlayer("J1", Color.WHITE, gameUserInterface),
            new HumanPlayer("J2", Color.BLACK, gameUserInterface),
            new GameConfig());

    match.move(
        new Move(CoordinateMapper.toIndex('B', 1), CoordinateMapper.toIndex('C', 1), Color.WHITE));
    match.undo();

    CmdAction cmdRedo = cmds.get("redo").get().createNew(new String[] {"5"});
    boolean result = cmdRedo.execute(match);

    assertFalse(result);
  }

  @Test
  @DisplayName("Couverture : Format de nombre invalide")
  void testRedoInvalidFormat() {
    CmdAction cmd = cmds.get("redo").get().createNew(new String[] {"abc"});
    assertNotNull(cmd);
  }

  @Test
  @DisplayName("Couverture : Atteindre 100% sur execute (branches if !match.redo)")
  void testExecuteBranches() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(
            board,
            new HumanPlayer("J1", Color.WHITE, gameUserInterface),
            new HumanPlayer("J2", Color.BLACK, gameUserInterface),
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
