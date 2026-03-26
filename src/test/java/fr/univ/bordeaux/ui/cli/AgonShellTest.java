package fr.univ.bordeaux.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdCreate;
import fr.univ.bordeaux.application.commands.specialized.CmdHelp;
import fr.univ.bordeaux.application.commands.specialized.CmdHint;
import fr.univ.bordeaux.application.commands.specialized.CmdLoad;
import fr.univ.bordeaux.application.commands.specialized.CmdQuit;
import fr.univ.bordeaux.application.commands.specialized.CmdRedo;
import fr.univ.bordeaux.application.commands.specialized.CmdSave;
import fr.univ.bordeaux.application.commands.specialized.CmdSet;
import fr.univ.bordeaux.application.commands.specialized.CmdShow;
import fr.univ.bordeaux.application.commands.specialized.CmdUndo;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * class to test AgonShell
 *
 * <p>requirements : - overvrite 2 classes from Jline (LineReader and Terminal)
 */
public class AgonShellTest {
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();

  @BeforeEach
  void setUp() {
    GameConfig config = new GameConfig();
    LineReader reader = new FakeLineReader("n");
    try {
      Terminal terminal = createFakeTerminal();
      GameUserInterface userInterface = new AgonShell(terminal, reader, cmds);
      GameEngine gameEngine = new GameEngine(userInterface, cmds);
      cmds.register("new", new CmdCreate(userInterface, config, gameEngine));
      cmds.register("quit", new CmdQuit(userInterface));
      cmds.register("hint", new CmdHint(userInterface));
      cmds.register("show", new CmdShow(userInterface, config));
      cmds.register("load", new CmdLoad(userInterface));
      cmds.register("save", new CmdSave(userInterface));
      cmds.register("set", new CmdSet(userInterface, config));
      cmds.register("undo", new CmdUndo(userInterface));
      cmds.register("redo", new CmdRedo(userInterface));
      cmds.register("help", new CmdHelp(userInterface, cmds));
    } catch (Exception e) {
    }
  }

  private Terminal createFakeTerminal() {
    return new FakeTerminal(new ByteArrayOutputStream());
  }

  @Test
  @DisplayName("resource with URL and '/' path notation")
  void debugResourcePath() {
    URL url = getClass().getResource("/cmdsInformations/agonShellMenu.txt");
    assertNotNull(url, () -> "Resource URL =" + url);
  }

  @Test
  @DisplayName("test quitGame with 'n' (no save)")
  void testQuitGameNoSave() throws Exception {
    LineReader reader = new FakeLineReader("n");
    Terminal terminal = createFakeTerminal();
    AgonShell shell = new AgonShell(terminal, reader, cmds);
    assertTrue(shell.isRunning());
    shell.quit();
    assertFalse(shell.getDebugMode().get());
  }

  @Test
  @DisplayName("test quitGame with 'y' (save)")
  void testQuitGameWithSave() throws Exception {
    LineReader reader = new FakeLineReader("y");
    Terminal terminal = createFakeTerminal();
    AgonShell shell = new AgonShell(terminal, reader, cmds);
    shell.quit();
    assertFalse(shell.getDebugMode().get());
  }

  @Test
  @DisplayName("test showMessage writes raw content to terminal")
  void testShowMessage() throws Exception {
    // 1. On prépare la capture de la sortie
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // On utilise soit TerminalBuilder soit ton FakeTerminal
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);

    // 2. On appelle la méthode
    String myMessage = "Hello Agon!";
    shell.showMessage(myMessage);

    // 3. On vérifie que le message est bien présent dans le flux
    String output = out.toString();
    assertTrue(output.contains(myMessage), "Le terminal devrait afficher : " + myMessage);
  }

  @Test
  @DisplayName("test readLine with empty input")
  void testReadLineEmpty() throws Exception {
    LineReader reader = new FakeLineReader("");
    Terminal terminal = createFakeTerminal();
    AgonShell shell = new AgonShell(terminal, reader, cmds);
    shell.getUserInput();
    // no except thrown
    assertTrue(true);
  }

  @Test
  @DisplayName("test readLine with UserInterruptException")
  void testReadLineInterrupt() throws Exception {
    LineReader reader = new FakeLineReader();
    Terminal terminal = createFakeTerminal();
    AgonShell shell = new AgonShell(terminal, reader, cmds);
    shell.getUserInput();
    assertTrue(true); // no except thrown
  }

  @Test
  @DisplayName("test safeCloseTerminal with IOException")
  void testSafeCloseTerminalIOException() throws Exception {
    Terminal terminal = createFakeTerminal();
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);
    shell.safeCloseTerminal();
    assertTrue(true); // no throws before
  }

  @Test
  @DisplayName("test showError writes to terminal")
  void testShowError() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // REMPLACEMENT : On utilise ton FakeTerminal au lieu du Builder
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);

    shell.showError("test error");

    String output = out.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("AGON"));
  }

  @Test
  @DisplayName("test showInfo writes to terminal")
  void testShowInfo() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // REMPLACEMENT
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);

    shell.showInfo("test info");

    String output = out.toString();
    assertTrue(output.contains("INFO"));
  }

  @Test
  @DisplayName("test showWarn writes to terminal")
  void testShowWarn() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // REMPLACEMENT
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);

    shell.showWarn("test warn");

    String output = out.toString();
    assertTrue(output.contains("WARNING"));
  }

  @Test
  @DisplayName("test leave sets running to false")
  void testLeave() throws Exception {
    Terminal terminal = createFakeTerminal();
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);
    shell.leave();
    assertFalse(shell.getDebugMode().get());
  }

  @Test
  @DisplayName("test leave sets running to false")
  void testLeaveWithFakeTerminal() {
    Terminal terminal = createFakeTerminal();
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(""), cmds);
    shell.leave();
    assertFalse(shell.getRunning().get());
  }

  @Test
  @DisplayName("test setVerbose toggles state")
  void testSetVerbose() throws Exception {
    Terminal terminal = createFakeTerminal();
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);
    shell.setVerbose(true);
    assertTrue(shell.getVerbose());
    shell.setVerbose(false);
    assertFalse(shell.getVerbose());
  }

  @Test
  @DisplayName("test empty input does nothing (no error message)")
  void testEmptyInputDoesNothing() {
    var out = new ByteArrayOutputStream();
    Terminal terminal = new FakeTerminal(out);
    // On simule une entrée vide
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(""), cmds);

    String result = shell.getUserInput();

    // 1. On vérifie que le résultat est null (ton if (line.isEmpty()))
    assertNull(result, "L'input devrait être null pour une ligne vide");

    // 2. On vérifie que rien n'a été écrit dans la console (pas d'erreur)
    String cliOutput = out.toString();
    assertFalse(cliOutput.contains("No Command"), "Le shell ne doit pas afficher d'erreur");
  }

  @Test
  @DisplayName("test init sets default values")
  void testInit() {

    Terminal terminal = createFakeTerminal();
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(""), cmds);

    assertFalse(shell.getVerbose());
    assertFalse(shell.getDebugMode().get());
    assertTrue(shell.getRunning().get());
  }

  @Test
  @DisplayName("test loadMainMenu updates displayed menu")
  void testLoadMainMenu() {
    var out = new ByteArrayOutputStream();
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(""), cmds);
    String newMenu =
        "    undo [N] : cancel the last turn (or the N lasts)\n"
            + "    redo [N] : replay the last canceled turn (or the N lasts)\n"
            + "    show";
    shell.loadMainMenu(newMenu);
    shell.showHelp();
    String output = out.toString();
    assertTrue(output.contains("undo [N]"), "Menu should contain 'undo [N]'");
    assertTrue(output.contains("redo [N]"), "Menu should contain 'redo [N]'");
    assertTrue(output.contains("show"), "Menu should contain 'show'");
  }

  @Test
  @DisplayName("test globalCompleter with empty input suggests all commands")
  void testGlobalCompleterEmptyInput() throws Exception {

    Terminal terminal = createFakeTerminal();
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);

    // Une ligne vide signifie que l'utilisateur n'a encore rien tapé
    ParsedLine line = new DefaultParser().parse("", 0);
    List<Candidate> candidates = new ArrayList<>();

    // 3. Appel de la méthode
    shell.globalCompleter(null, line, candidates);

    List<String> results = candidates.stream().map(Candidate::value).toList();
    assertTrue(results.contains("help"));
    assertTrue(results.contains("quit"));
    assertTrue(results.contains("new"));
    assertTrue(results.contains("hint"));
    assertTrue(results.contains("show"));
    assertTrue(results.contains("load"));
    assertTrue(results.contains("set"));
    assertTrue(results.contains("save"));
    assertTrue(results.contains("undo"));
    assertTrue(results.contains("redo"));
  }

  @Test
  @DisplayName("test globalCompleter with empty input suggests all commands")
  void testGlobalCompleterWithInput() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // On utilise soit TerminalBuilder soit ton FakeTerminal
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);

    // L'utilisateur a tapé "h"
    ParsedLine line = new DefaultParser().parse("h", 1);
    List<Candidate> candidates = new ArrayList<>();

    shell.globalCompleter(null, line, candidates);

    List<String> results = candidates.stream().map(Candidate::value).toList();
    assertTrue(results.contains("help"));
    assertTrue(results.contains("hint"));
    assertFalse(results.contains("quit"));

    candidates.clear();
    line = new DefaultParser().parse("z", 1);
    shell.globalCompleter(null, line, candidates);
    results = candidates.stream().map(Candidate::value).toList();
    assertTrue(results.isEmpty());
  }

  @Test
  void testFirstIfExecution() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // On utilise soit TerminalBuilder soit ton FakeTerminal
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);
    // On crée une ligne sans aucun texte
    ParsedLine line = new DefaultParser().parse("", 0);
    List<Candidate> candidates = new ArrayList<>();

    shell.globalCompleter(null, line, candidates);

    // Si on est entré dans le premier IF, on a toutes les commandes
    assertEquals(cmds.getKeys().size(), candidates.size());
  }

  @Test
  @DisplayName("test globalCompleter with empty input suggests all commands")
  void testGlobalCompleterOptions() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);
    ParsedLine line = new DefaultParser().parse("help ", 6);
    List<Candidate> candidates = new ArrayList<>();
    shell.globalCompleter(null, line, candidates);
    List<String> results = candidates.stream().map(Candidate::value).toList();
    assertTrue(results.contains("new"));
  }

  @Test
  @DisplayName("Test display the board in the shell")
  void testUpdateBoard() {
    // 1. On prépare la capture de la sortie
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    // On utilise soit TerminalBuilder soit ton FakeTerminal
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);
    AgonBoard agonboard = new AgonBoardImpl();
    agonboard.initBaseConfiguration();
    shell.updateBoard(agonboard);
    String Board =
        "     K /. X . . O .\\\n"
            + "    J /O . . . . . X\\\n"
            + "   I /. . . . . . . .\\\n"
            + "  H /X . . . . . . . O\\\n"
            + " G /. . . . . . . . . .\\\n"
            + "F |Q . . . . + . . . . q| \n"
            + " E \\. . . . . . . . . ./ 11\n"
            + "  D \\X . . . . . . . O/ 10\n"
            + "   C \\. . . . . . . ./ 9\n"
            + "    B \\O . . . . . X/ 8\n"
            + "     A \\. X . . O ./ 7\n"
            + "        1 2 3 4 5 6";
    // 3. On vérifie que le message est bien présent dans le flux
    String output = out.toString();
    assertTrue(
        output.contains(Board),
        "Le terminal devrait afficher : " + Board + "mais affiche : " + output);
  }
}
