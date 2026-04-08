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
      // Enregistrement du prototype de la commande undo
      cmds.register("undo", new CmdUndo(gameUserInterface));
    } catch (Exception e) {
      fail("Setup failed");
    }
  }

  @Test
  @DisplayName("Vérifier qu'un undo annule bien un mouvement sur le plateau")
  void executeUndoTest() {
    // 1. Initialisation du match
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

    // 2. On effectue un mouvement
    CmdAction move = new CmdMove(from, to, gameUserInterface);
    move.execute(match);
    assertNull(board.getPieceAt(from));
    assertEquals(piece, board.getPieceAt(to));
    new CmdMove(
            CoordinateMapper.toIndex('F', 11), CoordinateMapper.toIndex('F', 10), gameUserInterface)
        .execute(match);
    // 3. On exécute la commande Undo via le registre
    CmdAction cmdUndo = cmds.get("undo").get().createNew(new String[] {"1"});
    boolean result = cmdUndo.execute(match);

    // 4. VÉRIFICATIONS
    assertTrue(result, "L'exécution de undo doit renvoyer true");
    assertEquals(piece, board.getPieceAt(from), "La pièce doit être revenue à sa case de départ");
    assertNull(board.getPieceAt(to), "La case d'arrivée doit être vide après l'undo");
    assertEquals(
        Color.WHITE,
        match.getCurrentPlayer().getColor(),
        "Le tour doit être revenu au joueur blanc");
  }

  @Test
  @DisplayName("Vérifier que undo multiple annule plusieurs coups")
  void executeMultipleUndoTest() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(
            board,
            new HumanPlayer("J1", Color.WHITE, gameUserInterface),
            new HumanPlayer("J2", Color.BLACK, gameUserInterface),
            new GameConfig());

    // On joue deux coups
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

    // On demande un undo de 2 coups
    CmdAction cmdUndo = cmds.get("undo").get().createNew(new String[] {"2"});
    cmdUndo.execute(match);

    // Vérification que les deux pièces sont revenues (B1 et B2 ne sont plus vides)
    assertNotNull(board.getPieceAt(CoordinateMapper.toIndex('B', 1)));
    assertNotNull(board.getPieceAt(CoordinateMapper.toIndex('F', 1)));
  }

  @Test
  @DisplayName("Vérifier que undo sans historique ne crash pas et s'arrête via le break")
  void executeUndoEmptyTest() {
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new StandardMatch(
            board, new HumanPlayer("J1", Color.WHITE, gameUserInterface), null, new GameConfig());

    // Pas de coups joués, on tente un undo
    CmdAction cmdUndo = cmds.get("undo").get().createNew(new String[] {"1"});
    boolean result = cmdUndo.execute(match);

    assertTrue(result);
  }

  @Test
  @DisplayName("Vérifier createNew avec différents formats d'arguments")
  void createNewTest() {
    // Test format positionnel: undo 2
    CmdAction cmdUndo1 = cmds.get("undo").get().createNew(new String[] {"2"});
    assertNotNull(cmdUndo1);

    // Test format flag: undo -n 3
    CmdAction cmdUndo2 = cmds.get("undo").get().createNew(new String[] {"-n", "3"});
    assertNotNull(cmdUndo2);

    // Test erreur format: doit logger une erreur et renvoyer null (selon ton code)
    CmdAction cmdUndoErr = cmds.get("undo").get().createNew(new String[] {"abc"});
    assertNull(cmdUndoErr);
  }

  @Test
  @DisplayName("Vérifier la description de undo")
  void getDescriptionTest() {
    CmdAction cmdUndo = cmds.get("undo").get().createNew(new String[] {});
    assertTrue(
        cmdUndo.getDescription().contains("Description: Cancels the last N played turns."),
        "result : \"" + cmdUndo.getDescription() + "\"");
  }
}
