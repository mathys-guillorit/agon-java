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
import java.nio.file.Path;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CmdSaveTest {

  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface ui;
  private MatchManager match;
  private ByteArrayOutputStream outContent;

  @TempDir Path tempDir; // Dossier temporaire pour les fichiers de sauvegarde

  @BeforeEach
  void setUp() {
    outContent = new ByteArrayOutputStream();
    LineReader reader = new FakeLineReader("");
    try {
      Terminal terminal = new FakeTerminal(outContent);
      ui = new AgonShell(terminal, reader, cmds);

      // On crée un vrai match pour avoir des données à sauvegarder
      match =
          new StandardMatch(
              new AgonBoardImpl(),
              new HumanPlayer("J1", Color.WHITE, ui),
              new HumanPlayer("J2", Color.BLACK, ui),
              new GameConfig());

      cmds.register("save", new CmdSave(ui));
    } catch (Exception e) {
      fail("Setup failed: " + e.getMessage());
    }
  }

  @Test
  @DisplayName("Sauvegarde réussie avec un nom de fichier spécifique")
  void testSaveWithFilename() throws Exception {
    // On définit un chemin dans le dossier temporaire
    String filePath = tempDir.resolve("my_save.asv").toString();

    CmdAction cmd = cmds.get("save").get().createNew(new String[] {filePath});

    // execute renvoie false dans ton code (sans doute pour ne pas passer le tour)
    boolean result = cmd.execute(match);

    assertFalse(result);

    // Vérification physique du fichier
    File file = new File(filePath);
    assertTrue(file.exists(), "Le fichier de sauvegarde devrait exister sur le disque");
    assertTrue(file.length() > 0, "Le fichier ne devrait pas être vide");
    assertTrue(match.isSaved(), "Le flag isSaved du match devrait être à true");
  }

  @Test
  @DisplayName("Sauvegarde par défaut si aucun nom n'est fourni")
  void testSaveDefaultFilename() {
    // On simule l'appel à "save" sans arguments
    CmdAction cmd = cmds.get("save").get().createNew(new String[] {});

    cmd.execute(match);
    // On vérifie qu'un fichier "default_save" a été créé
    File defaultFile = new File("default_save");
    if (defaultFile.exists()) {
      defaultFile.delete(); // Nettoyage car il n'est pas dans tempDir
    }
  }

  @Test
  @DisplayName("Gestion d'erreur lors de l'écriture (IOException)")
  void testSaveErrorHandling() {
    // On utilise un nom de fichier invalide (ex: dossier qui n'existe pas ou caractères interdits)
    // Sous Linux/Mac, "/" ou un chemin vers un dossier protégé provoquera une IOException
    String invalidPath = "/this/path/does/not/exist/save.asv";

    CmdAction cmd = cmds.get("save").get().createNew(new String[] {invalidPath});
    boolean result = cmd.execute(match);

    assertFalse(result);
    assertTrue(outContent.toString().contains("Something went wrong while saving"));
  }

  @Test
  @DisplayName("Vérification des métadonnées de la commande")
  void testCommandMetadata() {
    CmdAction prototype = cmds.get("save").get();
    assertTrue(cmds.get("save").isPresent(), "cmd must exists");
    assertEquals("save", prototype.getName());
    final String cmdDesc = prototype.getDescription();
    assertTrue(cmdDesc.contains("save [filename]"));
    var msg = new StringBuilder();
    msg.append("Description: Saves the current game state to the specified");
    msg.append(" file,if there is no filename save by ");
    assertTrue(cmdDesc.contains(msg.append("default in default_save.\n").toString()), cmdDesc);
  }
}
