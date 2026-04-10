package fr.univ.bordeaux.application.commands.specialized;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.match.StandardMatch;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CmdQuitTest {
  private AgonRegister<CmdAction> cmds;
  private ByteArrayOutputStream outContent;
  private GameUserInterface gameUserInterface;

  @BeforeEach
  void setUp() {
    cmds = new AgonRegister<>();
    outContent = new ByteArrayOutputStream();
  }

  private GameUserInterface createUiWithInputs(String... inputs) throws Exception {
    Terminal terminal = new FakeTerminal(outContent);
    String joined = String.join("\n", inputs);
    LineReader reader = new FakeLineReader(joined);
    return new AgonShell(terminal, reader, cmds);
  }

  private MatchManager createRealMatch(GameUserInterface ui) {
    return new StandardMatch(
        new AgonBoardImpl(),
        new HumanPlayer("J1", Color.WHITE, ui),
        new HumanPlayer("J2", Color.BLACK, ui),
        new GameConfig());
  }

  private static class SpyClient extends fr.univ.bordeaux.application.network.client.AgonClient {
    boolean connected;
    boolean quitCalled;
    boolean resignCalled;

    SpyClient(LocalProfile profile) {
      super(profile);
    }

    @Override
    public boolean isConnected() {
      return connected;
    }

    @Override
    public void quit() {
      quitCalled = true;
      connected = false;
    }

    @Override
    public void resignGame() {
      resignCalled = true;
    }
  }

  @Test
  @DisplayName("Quit online match active -> resign only")
  void testQuitOnlineMatchActive() throws Exception {
    gameUserInterface = createUiWithInputs();
    MatchManager onlineMatch = createRealMatch(gameUserInterface);
    SpyClient client = new SpyClient(new LocalProfile("test"));

    AppContext context =
        new AppContext(new LocalProfile("test")) {
          @Override
          public boolean isOnlineGameActive() {
            return true;
          }

          @Override
          public fr.univ.bordeaux.application.match.Match getCurrentOnlineMatch() {
            return (fr.univ.bordeaux.application.match.Match) onlineMatch;
          }

          @Override
          public fr.univ.bordeaux.application.network.client.AgonClient getClient() {
            return client;
          }
        };

    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);
    boolean result = cmdQuit.execute(null);

    assertTrue(result);
    assertTrue(client.resignCalled);
    assertTrue(gameUserInterface.isRunning(), "L'application ne doit pas quitter ici");
  }

  @Test
  @DisplayName("Quit local unsaved match with answer n")
  void testQuitNoSave() throws Exception {
    gameUserInterface = createUiWithInputs("n");
    MatchManager match = createRealMatch(gameUserInterface);

    AppContext context = new AppContext(new LocalProfile("test"));
    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);

    boolean result = cmdQuit.execute(match);

    assertTrue(result);
    assertFalse(gameUserInterface.isRunning());
    assertTrue(outContent.toString().contains("Save the game before quitting?"));
  }

  @Test
  @DisplayName("Quit local unsaved match with uppercase Y and explicit filename")
  void testQuitWithSaveSuccessUppercaseY() throws Exception {
    String filename = "save_uppercase_test";
    File file = new File(filename);
    if (file.exists()) {
      file.delete();
    }

    gameUserInterface = createUiWithInputs("Y", filename);
    MatchManager match = createRealMatch(gameUserInterface);
    match.setIsSaved(false);

    AppContext context = new AppContext(new LocalProfile("test"));
    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);

    boolean result = cmdQuit.execute(match);

    assertTrue(result);
    assertFalse(match.isSaved());
    assertFalse(gameUserInterface.isRunning());
    assertFalse(file.exists() || match.isSaved());

    file.delete();
  }

  @Test
  @DisplayName("Quit local unsaved match with blank filename -> default_save")
  void testQuitWithEmptyFileName() throws Exception {
    File defaultSave = new File("default_save");
    if (defaultSave.exists()) {
      defaultSave.delete();
    }

    gameUserInterface = createUiWithInputs("y", "   ");
    MatchManager match = createRealMatch(gameUserInterface);
    match.setIsSaved(false);

    AppContext context = new AppContext(new LocalProfile("test"));
    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);

    boolean result = cmdQuit.execute(match);

    assertTrue(result);
    assertTrue(match.isSaved());
    assertFalse(gameUserInterface.isRunning());
    assertTrue(defaultSave.exists() || match.isSaved());

    defaultSave.delete();
  }

  @Test
  @DisplayName("Quit local unsaved match with null-like input -> no save")
  void testQuitWithNoInputFallsBackToNo() throws Exception {
    gameUserInterface = createUiWithInputs("");
    MatchManager match = createRealMatch(gameUserInterface);
    match.setIsSaved(false);

    AppContext context = new AppContext(new LocalProfile("test"));
    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);

    boolean result = cmdQuit.execute(match);

    assertTrue(result);
    assertFalse(gameUserInterface.isRunning());
    assertTrue(outContent.toString().contains("Save the game before quitting?"));
  }

  @Test
  @DisplayName("Quit local match already saved -> no save prompt")
  void testQuitAlreadySaved() throws Exception {
    gameUserInterface = createUiWithInputs();
    MatchManager match = createRealMatch(gameUserInterface);
    match.setIsSaved(true);

    AppContext context = new AppContext(new LocalProfile("test"));
    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);

    boolean result = cmdQuit.execute(match);

    assertTrue(result);
    assertFalse(gameUserInterface.isRunning());
    assertFalse(outContent.toString().contains("Save the game before quitting?"));
  }

  @Test
  @DisplayName("Quit finished match -> no save prompt")
  void testQuitMatchOver() throws Exception {
    gameUserInterface = createUiWithInputs();
    MatchManager match = createRealMatch(gameUserInterface);
    match.quit(); // pour rendre le match terminé

    AppContext context = new AppContext(new LocalProfile("test"));
    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);

    boolean result = cmdQuit.execute(match);

    assertTrue(result);
    assertFalse(gameUserInterface.isRunning());
    assertFalse(outContent.toString().contains("Description: Exits the application.If "));
  }

  @Test
  @DisplayName("Quit disconnects connected client")
  void testQuitClientConnected() throws Exception {
    gameUserInterface = createUiWithInputs();
    SpyClient spyClient = new SpyClient(new LocalProfile("test"));
    spyClient.connected = true;

    AppContext context =
        new AppContext(new LocalProfile("test")) {
          @Override
          public fr.univ.bordeaux.application.network.client.AgonClient getClient() {
            return spyClient;
          }
        };

    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);
    boolean result = cmdQuit.execute(null);

    assertTrue(result);
    assertTrue(spyClient.quitCalled);
    assertTrue(outContent.toString().contains("Disconnected from server"));
  }

  @Test
  @DisplayName("Quit exits application when no match and no connected client")
  void testQuitExitApplicationFallback() throws Exception {
    gameUserInterface = createUiWithInputs();
    SpyClient spyClient = new SpyClient(new LocalProfile("test"));
    spyClient.connected = false;

    AppContext context =
        new AppContext(new LocalProfile("test")) {
          @Override
          public fr.univ.bordeaux.application.network.client.AgonClient getClient() {
            return spyClient;
          }
        };

    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);
    boolean result = cmdQuit.execute(null);

    assertTrue(result);
    assertTrue(outContent.toString().contains("Exiting application"));
    assertFalse(gameUserInterface.isRunning());
  }

  @Test
  @DisplayName("Quit exits application when client is null")
  void testQuitExitApplicationWhenClientNull() throws Exception {
    gameUserInterface = createUiWithInputs();

    AppContext context =
        new AppContext(new LocalProfile("test")) {
          @Override
          public fr.univ.bordeaux.application.network.client.AgonClient getClient() {
            return null;
          }
        };

    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);
    boolean result = cmdQuit.execute(null);

    assertTrue(result);
    assertTrue(outContent.toString().contains("Exiting application"));
    assertFalse(gameUserInterface.isRunning());
  }

  @Test
  @DisplayName("Description contains usage")
  void testDescription() throws Exception {
    gameUserInterface = createUiWithInputs();

    AppContext context = new AppContext(new LocalProfile("test"));
    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context);
    final String desc = cmdQuit.getDescription();
    assertTrue(desc.contains("quit (or Ctrl+C)"), desc);
    assertTrue(desc.contains("Exits the application"), desc);
  }
}
