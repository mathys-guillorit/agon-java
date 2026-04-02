package fr.univ.bordeaux.application.match;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdCreate;
import fr.univ.bordeaux.application.commands.specialized.CmdQuit;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GameEngineTest {

  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface gameUserInterface;
  private GameConfig config;
  private GameEngine gameEngine;

  @BeforeEach
  void setUp() {
    config = new GameConfig();
    LineReader reader = new FakeLineReader("n");

    try {
      Terminal terminal = new FakeTerminal(new ByteArrayOutputStream());
      gameUserInterface = new AgonShell(terminal, reader, cmds);

      gameEngine = new GameEngine(gameUserInterface, cmds);

      cmds.register("new", new CmdCreate(gameUserInterface, config, gameEngine));

    } catch (Exception e) {
      fail("Le setup a échoué : " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Vérifier que createNew génère une action non nulle")
  void createNewTest() {
    // On simule l'appel 'create' sans arguments
    CmdAction cmdCreate = cmds.get("new").get().createNew(new String[] {});
    cmdCreate.execute(null);
    assertNotNull(gameEngine.getMatchManager());
  }

  @Test
  @DisplayName("Test de la boucle principale : Menu -> Create -> Match")
  void loopTest() {
    // On configure le FakeLineReader pour simuler une séquence de touches :
    // 1. "create" pour lancer un match
    // 2. null ou une commande de sortie pour arrêter la boucle
    LineReader reader = new FakeLineReader("new", "quit");

    try {
      Terminal terminal = new FakeTerminal(new ByteArrayOutputStream());
      // On recrée l'interface avec ce reader spécifique
      gameUserInterface = new AgonShell(terminal, reader, cmds);
      gameEngine = new GameEngine(gameUserInterface, cmds);

      cmds.register("new", new CmdCreate(gameUserInterface, config, gameEngine));
      cmds.register("quit", new CmdQuit(gameUserInterface));
      gameEngine.start();

      assertNotNull(
          gameEngine.getMatchManager(),
          "La boucle aurait dû exécuter 'create' et initialiser le match");
      gameUserInterface.quit();
    } catch (Exception e) {
    }
  }

  /*@Test
  void stopThreadTest() {
    LineReader reader = new FakeLineReader("");
    try {
      Terminal terminal = new FakeTerminal(new ByteArrayOutputStream());
      // On recrée l'interface avec ce reader spécifique
      gameUserInterface = new AgonShell(terminal, reader, cmds);
      gameEngine = new GameEngine(gameUserInterface, cmds);
      StandardMatch matchManager = new StandardMatch(new AgonBoardImpl(),
          new HumanPlayer("J1", Color.WHITE, null), new HumanPlayer("J1", Color.BLACK, null),
          new GameConfig());
      gameEngine.setMatchManager(matchManager);
      gameEngine.start();
      matchManager.setMatchStatus(MatchStatus.FINISHED);

    } catch (Exception e) {

    }
  }*/

  @Test
  void stopTest() throws Exception {
    AtomicBoolean interruptedReceived = new AtomicBoolean(false);

    Player slowPlayer =
        new HumanPlayer("Slow", Color.WHITE, null) {
          @Override
          public CmdAction getAction(AgonRegister<CmdAction> cmds) {
            try {
              Thread.sleep(5000);
            } catch (InterruptedException e) {
              interruptedReceived.set(true);
              return null;
            }
            return null;
          }
        };

    // 2. Initialisation du match et de l'engine
    StandardMatch match =
        new StandardMatch(
            new AgonBoardImpl(),
            slowPlayer,
            new HumanPlayer("J2", Color.BLACK, null),
            new GameConfig());
    gameEngine.setMatchManager(match);

    // 3. On lance l'engine dans un thread à part pour ne pas bloquer le TEST
    Thread engineThread = new Thread(() -> gameEngine.start());
    engineThread.start();

    // 4. On attend 200ms pour être SÛR que l'engine est entré dans la phase FutureAction.get()
    Thread.sleep(200);

    // 5. ON COUPE LE MATCH
    match.setMatchStatus(MatchStatus.FINISHED);

    // 6. On attend que l'engine traite l'info
    Thread.sleep(500);

    // 7. VERIFICATION
    assertTrue(
        interruptedReceived.get(),
        "Le thread du joueur aurait dû être interrompu par futureAction.cancel(true)");

    // Nettoyage
    gameUserInterface.quit();
    engineThread.join(1000);
  }
}
