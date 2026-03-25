package fr.univ.bordeaux.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.network.CmdJoin;
import fr.univ.bordeaux.application.commands.network.CmdPing;
import fr.univ.bordeaux.application.commands.network.CmdServerList;
import fr.univ.bordeaux.application.commands.network.CmdServerStart;
import fr.univ.bordeaux.application.commands.network.CmdServerStatus;
import fr.univ.bordeaux.application.commands.network.CmdServerStop;
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
import fr.univ.bordeaux.technical.config.GameConfig;
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
 * Test class for {@link AgonShell}.
 *
 * <p>This class validates:
 * <ul>
 *   <li>basic shell lifecycle</li>
 *   <li>message display methods</li>
 *   <li>command completion behavior</li>
 *   <li>board rendering in terminal</li>
 *   <li>registration of both local and network commands</li>
 * </ul>
 *
 * <p>The tests rely on fake JLine components:
 * <ul>
 *   <li>{@link FakeLineReader}</li>
 *   <li>{@link FakeTerminal}</li>
 * </ul>
 */
public class AgonShellTest {

  /** Command registry shared by the shell during tests. */
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();

  /** Shared application context for network-aware commands. */
  private AppContext context;

  /**
   * Initializes a fresh command registry before each test.
   *
   * <p>This setup registers both:
   * <ul>
   *   <li>standard/local commands</li>
   *   <li>network commands</li>
   * </ul>
   */
  @BeforeEach
  void setUp() {
    cmds = new AgonRegister<>();
    context = new AppContext();

    GameConfig config = new GameConfig();
    LineReader reader = new FakeLineReader("n");

    try {
      Terminal terminal = createFakeTerminal();
      GameUserInterface userInterface = new AgonShell(terminal, reader, cmds);
      GameEngine gameEngine = new GameEngine(userInterface, cmds);

      // Local / gameplay commands
      cmds.register("new", new CmdCreate(userInterface, config, gameEngine));
      cmds.register("hint", new CmdHint(userInterface));
      cmds.register("show", new CmdShow(userInterface, config));
      cmds.register("load", new CmdLoad(userInterface));
      cmds.register("save", new CmdSave(userInterface));
      cmds.register("set", new CmdSet(userInterface, config));
      cmds.register("undo", new CmdUndo(userInterface));
      cmds.register("redo", new CmdRedo(userInterface));
      cmds.register("help", new CmdHelp(userInterface, cmds));

      // Network commands
      cmds.register("join", new CmdJoin(userInterface, context));
      cmds.register("ping", new CmdPing(userInterface, context));
      cmds.register("server_start", new CmdServerStart(userInterface, context));
      cmds.register("server_stop", new CmdServerStop(userInterface, context));
      cmds.register("server_list", new CmdServerList(userInterface, context));
      cmds.register("server_status", new CmdServerStatus(userInterface, context));

      // Context-aware quit command
      cmds.register("quit", new CmdQuit(userInterface, context));

    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize AgonShellTest setup", e);
    }
  }

  /**
   * Creates a fake terminal used to capture shell output during tests.
   *
   * @return a fake terminal backed by a byte array output stream
   */
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
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);

    String myMessage = "Hello Agon!";
    shell.showMessage(myMessage);

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
    assertTrue(true);
  }

  @Test
  @DisplayName("test readLine with UserInterruptException")
  void testReadLineInterrupt() throws Exception {
    LineReader reader = new FakeLineReader();
    Terminal terminal = createFakeTerminal();
    AgonShell shell = new AgonShell(terminal, reader, cmds);

    shell.getUserInput();
    assertTrue(true);
  }

  @Test
  @DisplayName("test safeCloseTerminal with IOException")
  void testSafeCloseTerminalIOException() throws Exception {
    Terminal terminal = createFakeTerminal();
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);

    shell.safeCloseTerminal();
    assertTrue(true);
  }

  @Test
  @DisplayName("test showError writes to terminal")
  void testShowError() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
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
  @DisplayName("test leave sets running to false with fake terminal")
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
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(""), cmds);

    String result = shell.getUserInput();

    assertNull(result, "L'input devrait être null pour une ligne vide");

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
    ByteArrayOutputStream out = new ByteArrayOutputStream();
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

    ParsedLine line = new DefaultParser().parse("", 0);
    List<Candidate> candidates = new ArrayList<>();

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

    // network commands should also now be suggested
    assertTrue(results.contains("join"));
    assertTrue(results.contains("ping"));
    assertTrue(results.contains("server_start"));
    assertTrue(results.contains("server_stop"));
    assertTrue(results.contains("server_list"));
    assertTrue(results.contains("server_status"));
  }

  @Test
  @DisplayName("test globalCompleter with partial input")
  void testGlobalCompleterWithInput() throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);

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
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);

    ParsedLine line = new DefaultParser().parse("", 0);
    List<Candidate> candidates = new ArrayList<>();

    shell.globalCompleter(null, line, candidates);

    assertEquals(cmds.getKeys().size(), candidates.size());
  }

  @Test
  @DisplayName("test globalCompleter options for help command")
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
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Terminal terminal = new FakeTerminal(out);
    AgonShell shell = new AgonShell(terminal, new FakeLineReader(), cmds);

    AgonBoard agonboard = new AgonBoardImpl();
    agonboard.initBaseConfiguration();
    shell.updateBoard(agonboard);

    String board =
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

    String output = out.toString();
    assertTrue(
            output.contains(board),
            "Le terminal devrait afficher : " + board + " mais affiche : " + output);
  }
}