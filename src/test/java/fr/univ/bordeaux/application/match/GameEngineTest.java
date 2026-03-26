package fr.univ.bordeaux.application.match;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdCreate;
import fr.univ.bordeaux.application.commands.specialized.CmdQuit;
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
}
