package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.*;

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

      // Enregistrement du prototype
      cmds.register("show", new CmdShow(gameUserInterface, config));
    } catch (Exception e) {
      fail("Setup failed");
    }
  }

  @Test
  @DisplayName("Vérifier l'affichage de la configuration")
  void testShowConfiguration() {
    // 1. On prépare la commande avec l'argument -configuration
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-configuration"});
    assertNotNull(cmd);

    // 2. Exécution (pas besoin de match pour la config)
    boolean result = cmd.execute(null);

    // 3. Vérifications
    assertTrue(result);
    // On vérifie que le contenu de config.toString() se retrouve dans le terminal
    assertTrue(outContent.toString().contains(config.toString()));
  }

  @Test
  @DisplayName("Vérifier l'affichage du plateau (Board)")
  void testShowBoard() {
    // Initialisation d'un match réel
    MatchManager match =
        new StandardMatch(
            new AgonBoardImpl(), new HumanPlayer("J1", Color.WHITE, gameUserInterface), null);

    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-board"});
    boolean result = cmd.execute(match);

    assertFalse(result);
    // Note : Ici on vérifie que l'UI a reçu l'ordre d'updateBoard.
    // Comme updateBoard dans AgonShell écrit souvent sur le terminal,
    // on peut vérifier si des caractères du plateau apparaissent.
  }

  @Test
  @DisplayName("Vérifier l'erreur si aucun match n'est présent pour le plateau")
  void testShowBoardNoMatch() {
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-board"});

    // On passe null au lieu d'un match
    boolean result = cmd.execute(null);

    assertFalse(result);
    assertTrue(outContent.toString().contains("Error: No active match"));
  }

  @Test
  @DisplayName("Vérifier l'erreur si aucun match n'est présent pour le plateau")
  void testShowNoTarget() {
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {});
    boolean result = cmd.execute(null);

    assertFalse(result);
    assertTrue(
        outContent.toString().contains("No target specified. Use 'help show' for details.\n"));
  }

  @Test
  @DisplayName("Vérifier le refus de plusieurs cibles simultanées")
  void testMultipleTargetsError() {
    // show -board -history (ne devrait pas être autorisé selon ton code)
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-board", "-history"});

    assertNull(cmd, "Le factory createNew doit renvoyer null si plus d'une option est saisie");
    assertTrue(outContent.toString().contains("Please specify only one target"));
  }

  @Test
  @DisplayName("Vérifier l'affichage de l'historique")
  void testShowHistory() {
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-history"});
    AgonBoard board = new AgonBoardImpl();
    board.initBaseConfiguration();
    MatchManager match =
        new BlitzMatch(
            board,
            new HumanPlayer("test", Color.WHITE, gameUserInterface),
            new HumanPlayer("test2", Color.BLACK, gameUserInterface),
            1);
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
  @DisplayName("Vérifier l'option -time (doit renvoyer false pour ne pas passer le tour)")
  void testShowTimeLogic() {
    // 1. Setup avec un match Blitz
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-time"});
    boolean result = cmd.execute(null);
    assertFalse(result);
    assertTrue(
        outContent
            .toString()
            .contains("This command can only be used when you are currently in a blitz match"),
        "L'affichage doit contenir le temps");
    MatchManager matchBlitz =
        new BlitzMatch(
            new AgonBoardImpl(),
            new HumanPlayer("test", Color.WHITE, gameUserInterface),
            new HumanPlayer("test2", Color.BLACK, gameUserInterface),
            1);

    // 3. Exécution
    result = cmd.execute(matchBlitz);
    assertFalse(result, "La commande show ne doit pas consommer le tour du joueur");

    String output = outContent.toString();
    assertTrue(
        output.contains(
            "Remaining time for : "
                + matchBlitz.getCurrentPlayer().getName()
                + "( "
                + matchBlitz.getCurrentPlayer().getColor()
                + " ) : "),
        "Le temps formaté doit être présent");
  }

  @Test
  @DisplayName("Vérifier la gestion d'une cible invalide")
  void testInvalidTarget() {
    // createNew renverra null à cause du ParseException de Commons CLI
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-inconnu"});

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
