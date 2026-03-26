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
    LineReader reader = new FakeLineReader("n");

    try {
      Terminal terminal = new FakeTerminal(new ByteArrayOutputStream());
      gameUserInterface = new AgonShell(terminal, reader, cmds);

      engine = new GameEngine(gameUserInterface, cmds);

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

    CmdAction cmdCreate = cmds.get("create").get().createNew(new String[] {});

    boolean result = cmdCreate.execute(null);

    assertTrue(result, "La commande create doit renvoyer true après exécution");
  }

  @Test
  @DisplayName("Vérifier la description de la commande")
  void getDescriptionTest() {
    CmdAction cmdCreate = cmds.get("create").get().createNew(null);
    assertTrue(cmdCreate.getDescription().contains("Usage: new"));
  }

  @Test
  @DisplayName("Vérifier les options de configuration des joueurs")
  void testPlayerOptions() {
    CmdAction cmd =
        cmds.get("create").get().createNew(new String[] {"-p1Ia", "true", "-p2Ia", "false"});
    cmd.execute(null);
    assertTrue(config.isWhiteAi());
    assertFalse(config.isBlackAi());

    cmd =
        cmds.get("create")
            .get()
            .createNew(
                new String[] {
                  "-p1Color", "black", "-p1Ia", "true", "-p2Color", "white", "-p2Ia", "false"
                });
    cmd.execute(null);
    assertTrue(config.isBlackAi());
    assertFalse(config.isWhiteAi());
  }

  @Test
  @DisplayName("Vérifier les options du mode blitz et la fonction checkBlitzMode")
  void testBlitzOptions() {
    CmdAction cmd = cmds.get("create").get().createNew(new String[] {});
    cmd.execute(null);
    assertFalse(config.isBlitzMode());
    assertEquals(30, config.getTimeout());

    cmd = cmds.get("create").get().createNew(new String[] {"-b"});
    cmd.execute(null);
    assertTrue(config.isBlitzMode());
    assertEquals(30, config.getTimeout());

    cmd = cmds.get("create").get().createNew(new String[] {"-b", "-t", "60"});
    cmd.execute(null);
    assertTrue(config.isBlitzMode());
    assertEquals(60, config.getTimeout());

    cmd = cmds.get("create").get().createNew(new String[] {"-t", "45"});
    cmd.execute(null);
    assertFalse(config.isBlitzMode());
    assertEquals(45, config.getTimeout());
  }

  @Test
  @DisplayName("Vérifier les options de couleur avancées (p2Color et erreurs)")
  void testColorOptionsAdvanced() {
    // Cas 1 : Utilisation de p2Color pour déduire p1Color
    // Si p2 est blanc, p1 doit être noir
    CmdAction cmd = cmds.get("create").get().createNew(new String[] {"-p2Color", "white"});
    cmd.execute(null);
    // Par défaut p1IsAi=false, p2IsAi=true.
    // Si p1 est noir, config.setWhiteAI(p2IsAi) -> true, config.setBlackAI(p1IsAi) -> false
    assertTrue(config.isWhiteAi());
    assertFalse(config.isBlackAi());

    // Si p2 est noir, p1 doit être blanc
    cmd = cmds.get("create").get().createNew(new String[] {"-p2Color", "black"});
    cmd.execute(null);
    // Si p1 est blanc, config.setWhiteAI(p1IsAi) -> false, config.setBlackAI(p2IsAi) -> true
    assertFalse(config.isWhiteAi());
    assertTrue(config.isBlackAi());

    // Cas 2 : Couleur invalide (doit renvoyer false et ne pas crash)
    cmd = cmds.get("create").get().createNew(new String[] {"-p1Color", "RED"});
    boolean result = cmd.execute(null);
    assertFalse(result, "La commande doit échouer avec une couleur invalide");

    // Cas 3 : Option inconnue (ParseException)
    cmd = cmds.get("create").get().createNew(new String[] {"--unknown"});
    result = cmd.execute(null);
    assertFalse(result, "La commande doit échouer avec une option inconnue");
  }

  @Test
  @DisplayName("Couverture : Erreur si P1 et P2 ont la même couleur")
  void testSameColorError() {
    // On force les deux à WHITE
    CmdAction cmd =
        cmds.get("create").get().createNew(new String[] {"-p1Color", "white", "-p2Color", "white"});
    boolean result = cmd.execute(null);

    assertFalse(result, "La commande devrait échouer car les couleurs sont identiques");
    // Optionnel : vérifier que le message d'erreur est bien envoyé à l'UI
  }

  @Test
  @DisplayName("Couverture : Seul P1 est défini (déduction de P2)")
  void testOnlyP1Defined() {
    // Si P1 est BLACK, P2 doit être WHITE par déduction
    // P1 (Noir) est Humain (false), P2 (Blanc) est IA (true) par défaut.
    CmdAction cmd = cmds.get("create").get().createNew(new String[] {"-p1Color", "black"});
    boolean result = cmd.execute(null);

    assertTrue(result);
    assertTrue(config.isWhiteAi(), "P2 (Blanc) devrait être l'IA par défaut");
    assertFalse(config.isBlackAi(), "P1 (Noir) devrait être l'Humain par défaut");
    cmd = cmds.get("create").get().createNew(new String[] {"-p1Color", "white"});
    result = cmd.execute(null);
    assertTrue(config.isBlackAi());
  }

  @Test
  @DisplayName("Couverture : checkBlitzMode avec Warning (Time sans Blitz)")
  void testBlitzWarning() {
    // Cas où on met -t mais pas -b : cela doit déclencher le showWarn()
    CmdAction cmd = cmds.get("create").get().createNew(new String[] {"-t", "10"});
    boolean result = cmd.execute(null);

    assertTrue(result);
    assertFalse(config.isBlitzMode(), "Le mode blitz ne doit pas être actif si -b est absent");
    assertEquals(10, config.getTimeout(), "Le timeout doit quand même être mis à jour");
  }
}
