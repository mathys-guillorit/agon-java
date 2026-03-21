package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.match.StandardMatch;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.technical.config.GameConfig;
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

public class CmdHintTest {
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface gameUserInterface;
  private ByteArrayOutputStream outContent; // Pour lire ce qui est écrit au terminal

  @BeforeEach
  void setUp() {
    GameConfig config = new GameConfig();
    LineReader reader = new FakeLineReader("");
    outContent = new ByteArrayOutputStream(); // On initialise le buffer

    try {
      // On lie le FakeTerminal à notre ByteArrayOutputStream
      Terminal terminal = new FakeTerminal(outContent);
      gameUserInterface = new AgonShell(terminal, reader, cmds);

      cmds.register("hint", new CmdHint(gameUserInterface));
    } catch (Exception e) {
      fail("Setup failed");
    }
  }

  @Test
  @DisplayName("Vérifier que le prototype génère une action valide")
  void createNewTest() {
    CmdAction cmdHint = cmds.get("hint").get().createNew(new String[] {});
    assertNotNull(cmdHint, "L'action Hint ne doit pas être nulle");
  }

  @Test
  @DisplayName("Exécuter Hint dans un match réel et vérifier le feedback")
  void executeTest() {
    // 1. Initialisation du match
    AgonBoard board = new AgonBoardImpl();
    MatchManager match =
        new StandardMatch(
            board,
            new HumanPlayer("J1", Color.WHITE, gameUserInterface),
            new HumanPlayer("J2", Color.BLACK, gameUserInterface));

    // 2. Exécution de la commande
    CmdAction cmdHint = cmds.get("hint").get().createNew(null);
    boolean result = cmdHint.execute(match);
    String output = outContent.toString();
    assertFalse(
        output.contains("Hint:"),
        "Le terminal devrait afficher une suggestion commençant par 'Hint:'");
    assertFalse(result, "Hint doit renvoyer false pour ne pas passer le tour");
    board.initBaseConfiguration();
    cmdHint = cmds.get("hint").get().createNew(null);
    cmdHint.execute(match);
    output = outContent.toString();
    assertTrue(
        output.contains("Hint: From"),
        "Le terminal devrait afficher une suggestion commençant par 'Hint:'");
  }

  @Test
  @DisplayName("Vérifier que Hint échoue proprement sans match actif")
  void executeWithoutMatchTest() {
    CmdAction cmdHint = cmds.get("hint").get().createNew(new String[] {});

    // Exécution hors-match
    boolean result = cmdHint.execute(null);

    assertFalse(result, "Hint devrait renvoyer false s'il n'y a pas de match en cours");
  }

  @Test
  @DisplayName("Vérifier la présence d'une description")
  void getDescriptionTest() {
    CmdAction cmdHint = cmds.get("hint").get().createNew(null);
    assertNotNull(cmdHint.getDescription());
    assertTrue(cmdHint.getDescription().toLowerCase().contains("hint"));
  }
}
