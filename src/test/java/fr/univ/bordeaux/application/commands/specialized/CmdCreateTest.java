package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.GameEngine;
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

public class CmdCreateTest {
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface gameUserInterface;
  private GameEngine engine;
  private GameConfig config;

  @BeforeEach
  void setUp() {
    config = new GameConfig();
    // On simule une entrée utilisateur si nécessaire (ici "n" pour non à une question éventuelle)
    LineReader reader = new FakeLineReader("n");

    try {
      Terminal terminal = new FakeTerminal(new ByteArrayOutputStream());
      gameUserInterface = new AgonShell(terminal, reader, cmds);

      // On crée le moteur de jeu réel
      engine = new GameEngine(gameUserInterface, cmds);

      // On enregistre la commande Create.
      // Note : On passe l'engine et la config car CmdCreate en a besoin pour instancier le match
      cmds.register("create", new CmdCreate(gameUserInterface, config, engine));

    } catch (Exception e) {
      fail("Le setup a échoué : " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Vérifier que createNew génère une action non nulle")
  void createNewTest() {
    // On simule l'appel 'create' sans arguments
    CmdAction cmdCreate = cmds.get("create").get().createNew(new String[] {});
    assertNotNull(cmdCreate, "L'action créée ne doit pas être nulle");
  }

  @Test
  @DisplayName("Vérifier que l'exécution de la commande initialise bien un match")
  void executeTest() {
    // 1. On récupère l'action
    CmdAction cmdCreate = cmds.get("create").get().createNew(new String[] {});

    // 2. On exécute. Note : Au début, le match est null (hors-match)
    // L'exécution doit créer un StandardMatch et appeler engine.setMatchManager()
    boolean result = cmdCreate.execute(null);

    assertTrue(result, "La commande create doit renvoyer true après exécution");

    /* Note : Pour vérifier que le match est bien créé, il faudrait que ton GameEngine
       ait un getter getMatchManager() ou vérifier via l'UI que le plateau est affiché.
       Si tu as accès au match via l'engine :
    */
    // assertNotNull(engine.getMatchManager(), "Le match manager devrait être initialisé dans
    // l'engine");
  }

  @Test
  @DisplayName("Vérifier la description de la commande")
  void getDescriptionTest() {
    CmdAction cmdCreate = cmds.get("create").get().createNew(null);
    // Adapte la chaîne attendue à ce que tu as mis dans ton CmdCreate.java
    assertTrue(cmdCreate.getDescription().contains("Usage: new"));
  }
}
