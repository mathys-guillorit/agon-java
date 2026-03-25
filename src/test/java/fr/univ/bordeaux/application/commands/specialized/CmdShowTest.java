package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
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

    assertTrue(result);
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
  @DisplayName("Vérifier le refus de plusieurs cibles simultanées")
  void testMultipleTargetsError() {
    // show -board -history (ne devrait pas être autorisé selon ton code)
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-board", "-history"});

    assertNull(cmd, "Le factory createNew doit renvoyer null si plus d'une option est saisie");
    assertTrue(outContent.toString().contains("Please specify only one target"));
  }

  @Test
  @DisplayName("Vérifier l'affichage de l'historique (cas non implémenté)")
  void testShowHistory() {
    CmdAction cmd = cmds.get("show").get().createNew(new String[] {"-history"});
    boolean result = cmd.execute(null);

    assertTrue(result);
    assertTrue(
        outContent.toString().contains("History command recognized but not yet implemented"));
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
