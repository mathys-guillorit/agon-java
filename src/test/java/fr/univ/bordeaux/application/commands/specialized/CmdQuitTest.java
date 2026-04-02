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
import java.io.File;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CmdQuitTest {
  private AgonRegister<CmdAction> cmds;
  private ByteArrayOutputStream outContent;

  @BeforeEach
  void setUp() {
    cmds = new AgonRegister<>();
    outContent = new ByteArrayOutputStream();
  }

  // Helper pour créer l'interface avec une réponse prédéfinie
  private GameUserInterface createUiWithInput(String input) throws Exception {
    Terminal terminal = new FakeTerminal(outContent);
    LineReader reader = new FakeLineReader(input);
    return new AgonShell(terminal, reader, cmds);
  }

  @Test
  @DisplayName("F10 : Quitter sans sauvegarder (Réponse 'n')")
  void testQuitNoSave() throws Exception {
    gameUserInterface = createUiWithInput("n");
    MatchManager match = createRealMatch(gameUserInterface);

    CmdAction cmdQuit = new CmdQuit(gameUserInterface).createNew(null);
    boolean result = cmdQuit.execute(match);

    assertTrue(result);
    assertFalse(gameUserInterface.isRunning(), "Le shell devrait être arrêté");
    assertTrue(outContent.toString().contains("Save the game before quitting?"));
  }

  @Test
  @DisplayName("F10 : Quitter avec sauvegarde réussie (Réponse 'y' + nom)")
  void testQuitWithSave() throws Exception {
    // On simule : "y" pour sauvegarder, puis "ma_sauvegarde" pour le nom
    gameUserInterface = createUiWithInput("y");
    MatchManager match = createRealMatch(gameUserInterface);
    match.setIsSaved(false);

    CmdAction cmdQuit = new CmdQuit(gameUserInterface).createNew(null);
    cmdQuit.execute(match);

    assertTrue(match.isSaved(), "Le match devrait être marqué comme sauvegardé");
    assertFalse(gameUserInterface.isRunning());

    // Nettoyage du fichier créé par CmdSave
    new File("ma_sauvegarde").delete();
  }

  @Test
  @DisplayName("F10 : Sauvegarde avec nom vide (doit utiliser default_save)")
  void testQuitWithEmptyFileName() throws Exception {
    // "y" pour oui, puis "" (entrée vide) pour le nom
    gameUserInterface = createUiWithInput("y\n ");
    MatchManager match = createRealMatch(gameUserInterface);
    match.setIsSaved(false);

    CmdAction cmdQuit = new CmdQuit(gameUserInterface).createNew(null);
    cmdQuit.execute(match);

    assertTrue(match.isSaved());
    File defaultSave = new File("default_save");
    assertTrue(defaultSave.exists());
    defaultSave.delete();
  }

  @Test
  @DisplayName("Quitter alors qu'aucun match n'est en cours")
  void testQuitNoActiveMatch() throws Exception {
    gameUserInterface = createUiWithInput("");
    CmdAction cmdQuit = new CmdQuit(gameUserInterface).createNew(null);

    // Si match est null, on quitte directement (branche if(match != null) sautée)
    boolean result = cmdQuit.execute(null);

    assertTrue(result);
    assertFalse(gameUserInterface.isRunning());
  }

  @Test
  @DisplayName("Quitter si le match est déjà fini ou déjà sauvegardé")
  void testQuitAlreadySaved() throws Exception {
    gameUserInterface = createUiWithInput("");
    MatchManager match = createRealMatch(gameUserInterface);
    match.setIsSaved(true); // Déjà sauvegardé, ne doit pas demander

    CmdAction cmdQuit = new CmdQuit(gameUserInterface).createNew(null);
    cmdQuit.execute(match);

    assertFalse(gameUserInterface.isRunning());
    assertFalse(
        outContent.toString().contains("Save the game before quitting?"),
        "Ne devrait pas demander de sauvegarde si déjà fait");
  }

  @Test
  @DisplayName("Vérification de la description")
  void testDescription() throws Exception {
    gameUserInterface = createUiWithInput("");
    CmdAction cmdQuit = new CmdQuit(gameUserInterface);
    assertTrue(cmdQuit.getDescription().contains("Usage: quit"));
  }

  // --- Helpers ---

  private MatchManager createRealMatch(GameUserInterface ui) {
    return new StandardMatch(
        new AgonBoardImpl(),
        new HumanPlayer("J1", Color.WHITE, ui),
        new HumanPlayer("J2", Color.BLACK, ui),
        new GameConfig());
  }

  private GameUserInterface gameUserInterface;
}
