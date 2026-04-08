package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.match.StandardMatch;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.client.LocalProfile;
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
  private GameUserInterface gameUserInterface;

  @BeforeEach
  void setUp() {
    cmds = new AgonRegister<>();
    outContent = new ByteArrayOutputStream();
  }

  private GameUserInterface createUiWithInput(String input) throws Exception {
    Terminal terminal = new FakeTerminal(outContent);
    LineReader reader = new FakeLineReader(input);
    return new AgonShell(terminal, reader, cmds);
  }

  @Test
  @DisplayName("F10: Quit without saving (Answer 'n')")
  void testQuitNoSave() throws Exception {
    gameUserInterface = createUiWithInput("n");
    MatchManager match = createRealMatch(gameUserInterface);

    AppContext context = new AppContext(new LocalProfile("test"));
    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);
    boolean result = cmdQuit.execute(match);

    assertTrue(result);
    assertFalse(gameUserInterface.isRunning(), "The shell should be stopped");
    assertTrue(outContent.toString().contains("Save the game before quitting?"));
  }

  @Test
  @DisplayName("F10: Quit with successful save (Answer 'y' + name)")
  void testQuitWithSave() throws Exception {
    gameUserInterface = createUiWithInput("y");
    MatchManager match = createRealMatch(gameUserInterface);
    match.setIsSaved(false);

    AppContext context = new AppContext(new LocalProfile("test"));
    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);
    cmdQuit.execute(match);

    assertTrue(match.isSaved(), "The match should be marked as saved");
    assertFalse(gameUserInterface.isRunning());

    new File("ma_sauvegarde").delete();
  }

  @Test
  @DisplayName("F10: Save with empty name (should use default_save)")
  void testQuitWithEmptyFileName() throws Exception {
    gameUserInterface = createUiWithInput("y\n ");
    MatchManager match = createRealMatch(gameUserInterface);
    match.setIsSaved(false);

    AppContext context = new AppContext(new LocalProfile("test"));
    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);
    cmdQuit.execute(match);

    assertTrue(match.isSaved());
    File defaultSave = new File("default_save");
    assertTrue(defaultSave.exists());
    defaultSave.delete();
  }

  @Test
  @DisplayName("Quit when no match is in progress")
  void testQuitNoActiveMatch() throws Exception {
    gameUserInterface = createUiWithInput("");

    AppContext context = new AppContext(new LocalProfile("test"));
    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);
    boolean result = cmdQuit.execute(null);

    assertTrue(result);
    assertFalse(gameUserInterface.isRunning());
  }

  @Test
  @DisplayName("Quit if the match is already finished or already saved")
  void testQuitAlreadySaved() throws Exception {
    gameUserInterface = createUiWithInput("");
    MatchManager match = createRealMatch(gameUserInterface);
    match.setIsSaved(true);

    AppContext context = new AppContext(new LocalProfile("test"));
    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);
    cmdQuit.execute(match);

    assertFalse(gameUserInterface.isRunning());
    assertFalse(
        outContent.toString().contains("Save the game before quitting?"),
        "Should not prompt for save if already done");
  }

  @Test
  @DisplayName("Description verification")
  void testDescription() throws Exception {
    gameUserInterface = createUiWithInput("");

    AppContext context = new AppContext(new LocalProfile("test"));
    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context);
    assertTrue(cmdQuit.getDescription().contains("Usage: quit"));
  }

  @Test
  @DisplayName("Quit disconnects client when connected")
  void testQuitClientConnected() throws Exception {
    gameUserInterface = createUiWithInput("");

    AppContext context =
        new AppContext(new LocalProfile("test")) {
          @Override
          public AgonClient getClient() {
            return new AgonClient(getProfile()) {
              boolean connected = true;
              boolean quitCalled = false;

              @Override
              public boolean isConnected() {
                return true;
              }

              @Override
              public void quit() {
                quitCalled = true;
              }
            };
          }
        };

    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);
    boolean result = cmdQuit.execute(null);

    assertTrue(result);
    assertTrue(outContent.toString().contains("Disconnected from server"));
  }

  @Test
  @DisplayName("Quit exits application when no match and no client")
  void testQuitExitApplicationFallback() throws Exception {
    gameUserInterface = createUiWithInput("");

    AppContext context = new AppContext(new LocalProfile("test"));

    CmdAction cmdQuit = new CmdQuit(gameUserInterface, context).createNew(null);
    cmdQuit.execute(null);

    String output = outContent.toString();

    assertTrue(output.contains("Exiting application"));
    assertFalse(gameUserInterface.isRunning());
  }



  private MatchManager createRealMatch(GameUserInterface ui) {
    return new StandardMatch(
        new AgonBoardImpl(),
        new HumanPlayer("P1", Color.WHITE, ui),
        new HumanPlayer("P2", Color.BLACK, ui),
        new GameConfig());
  }

}