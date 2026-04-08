package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.BlitzMatch;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.application.match.player.Player;
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

public class CmdPauseTest {
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface gameUserInterface;
  private ByteArrayOutputStream output = new ByteArrayOutputStream();

  @BeforeEach
  void setUp() {
    LineReader reader = new FakeLineReader("n");
    try {
      Terminal terminal = new FakeTerminal(output);
      gameUserInterface = new AgonShell(terminal, reader, cmds);
    } catch (Exception e) {
      fail("Le setup a échoué : " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Vérifier que CmdPause.execute met le match en pause et renvoie false")
  void testExecutePause() throws InterruptedException {
    CmdPause cmdPause = new CmdPause(gameUserInterface);
    cmdPause.execute(null);
    assertTrue(
        output
            .toString()
            .contains(
                "You must create a match before using this command. Type help for more informations"));
    AgonBoardImpl board = new AgonBoardImpl();
    board.initBaseConfiguration();
    Player p1 = new HumanPlayer("White", Color.WHITE, gameUserInterface);
    Player p2 = new HumanPlayer("Black", Color.BLACK, gameUserInterface);

    // On crée un BlitzMatch avec 1 minute pour que ça soit facile à tester
    BlitzMatch match = new BlitzMatch(board, p1, p2, 1, new GameConfig());

    // Au début le timer tourne (lancé par le constructeur de BlitzMatch)
    String time1 = match.getCurrentPlayerRemainingTime();

    // On attend un peu pour que le temps s'écoule (au moins 1 seconde)
    Thread.sleep(1100);

    String time2 = match.getCurrentPlayerRemainingTime();
    assertNotEquals(time1, time2, "Le temps devrait s'être écoulé avant la pause");

    // On exécute la pause
    boolean result = cmdPause.execute(match);
    assertFalse(result, "La commande pause doit renvoyer false pour ne pas passer le tour");

    // On récupère le temps au moment de la pause
    String timePaused = match.getCurrentPlayerRemainingTime();

    // On attend encore
    Thread.sleep(1100);

    String timeAfterWait = match.getCurrentPlayerRemainingTime();
    assertEquals(
        timePaused, timeAfterWait, "Le temps ne devrait pas s'être écoulé pendant la pause");
  }

  @Test
  @DisplayName("Vérifier la description de la commande pause")
  void testGetDescription() {
    CmdPause cmdPause = new CmdPause(gameUserInterface);
    assertTrue(cmdPause.getDescription().contains("pause"), cmdPause.getDescription());
    assertTrue(
        cmdPause.getDescription().contains("Description: Pauses the game timers in Blitz mode."));
  }

  @Test
  @DisplayName("Vérifier createNew")
  void testCreateNew() {
    CmdPause cmdPause = new CmdPause(gameUserInterface);
    CmdAction newAction = cmdPause.createNew(new String[] {});
    assertNotNull(newAction);
    assertInstanceOf(CmdPause.class, newAction);
  }
}
