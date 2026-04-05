package fr.univ.bordeaux.ui.gui;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.BlitzMatch;
import fr.univ.bordeaux.application.match.MoveDtO;
import fr.univ.bordeaux.application.match.ReadOnlyMatch;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.gui.components.HexagonCanvas;
import fr.univ.bordeaux.ui.gui.controllers.GameViewController;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Test class for AgonGui achieving 100% coverage without using Mockito. Uses manual Stub/Fake
 * classes to simulate the GameEngine states.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AgonGuiTest {

  private AgonGui agonGui;
  private FakeGameViewController fakeController;

  static class FakeGameViewController extends GameViewController {
    public String lastMessage = "";
    public String lastInfo = "";
    public String lastError = "";
    public String lastWarn = "";
    public boolean helpCalled = false;

    @Override
    public void routeMessage(String m) {
      if (m != null && (m.contains("Current Player") || m.contains("Time left"))) {
        lastMessage = m;
      } else {
        lastInfo = m;
      }
    }

    @Override
    public void showInfo(String m) {
      lastInfo = m;
    }

    @Override
    public void showError(String m) {
      lastError = m;
    }

    @Override
    public void showWarn(String m) {
      lastWarn = m;
    }

    @Override
    public void showHelp() {
      helpCalled = true;
    }

    @Override
    public HexagonCanvas getHexCanvas() {
      return null;
    }
  }

  static class DummyPlayer implements Player {
    private final Color color;

    public DummyPlayer(Color c) {
      super();
      this.color = c;
    }

    @Override
    public Color getColor() {
      return color;
    }

    @Override
    public CmdAction getAction(AgonRegister<CmdAction> cmds) {
      return null;
    }

    @Override
    public String getName() {
      return "";
    }
  }

  static class FakeMatch implements ReadOnlyMatch {
    public boolean isOver = false;
    public Color winnerColor = null;

    @Override
    public AgonBoardImpl getAgonBoard() {
      return new AgonBoardImpl();
    }

    @Override
    public boolean isSaved() {
      return false;
    }

    @Override
    public boolean isMatchOver() {
      return isOver;
    }

    @Override
    public Player getCurrentPlayer() {
      return new DummyPlayer(Color.WHITE);
    }

    @Override
    public Player getWinner() {
      return winnerColor == null ? null : new DummyPlayer(winnerColor);
    }

    @Override
    public GameConfig getGameConfig() {
      return null;
    }

    @Override
    public Move hint() {
      return null;
    }

    @Override
    public List<MoveDtO> getHistory() {
      return null;
    }

    @Override
    public String getRemainingTime() {
      return "";
    }
  }

  static class FakeBlitzMatch extends BlitzMatch {
    public boolean isOver = false;
    public Color winnerColor = null;

    public FakeBlitzMatch() {
      super(
          new AgonBoardImpl(),
          new DummyPlayer(Color.WHITE),
          new DummyPlayer(Color.BLACK),
          10,
          new GameConfig(),
          Color.WHITE);
    }

    @Override
    public AgonBoardImpl getAgonBoard() {
      return new AgonBoardImpl();
    }

    @Override
    public boolean isMatchOver() {
      return isOver;
    }

    @Override
    public Player getCurrentPlayer() {
      return new DummyPlayer(Color.BLACK);
    }

    @Override
    public Player getWinner() {
      return winnerColor == null ? null : new DummyPlayer(winnerColor);
    }

    @Override
    public String getRemainingTime() {
      return "04:59";
    }
  }

  @BeforeAll
  static void initJFX() throws InterruptedException {
    System.setProperty("IS_TEST_ENV", "true");
    CountDownLatch latch = new CountDownLatch(1);
    try {
      Platform.startup(
          () -> {
            Platform.setImplicitExit(false);
            latch.countDown();
          });
    } catch (IllegalStateException e) {
      Platform.runLater(
          () -> {
            Platform.setImplicitExit(false);
            latch.countDown();
          });
    }
    latch.await(2, TimeUnit.SECONDS);
  }

  @BeforeEach
  void setUp() throws Exception {
    agonGui = new AgonGui(new GameConfig());
    fakeController = new FakeGameViewController();

    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          try {
            Field controllerField = AgonApp.class.getDeclaredField("controller");
            controllerField.setAccessible(true);
            controllerField.set(null, fakeController);
          } catch (Exception e) {
            e.printStackTrace();
          }
          latch.countDown();
        });
    latch.await(2, TimeUnit.SECONDS);
  }

  @AfterEach
  void tearDown() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          try {
            Field controllerField = AgonApp.class.getDeclaredField("controller");
            controllerField.setAccessible(true);
            controllerField.set(null, null);

            for (javafx.stage.Window window :
                new java.util.ArrayList<>(javafx.stage.Window.getWindows())) {
              if (window instanceof javafx.stage.Stage) {
                ((javafx.stage.Stage) window).close();
              }
            }
          } catch (Exception e) {
          }
          latch.countDown();
        });
    latch.await(2, java.util.concurrent.TimeUnit.SECONDS);
  }

  private void waitForRunLater() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(latch::countDown);
    latch.await(2, TimeUnit.SECONDS);
  }

  @Test
  @Order(1)
  void testDisplayHistory() throws InterruptedException {
    agonGui.displayHistory(new ArrayList<>());
    waitForRunLater();
    assertEquals("The history is currently empty.", fakeController.lastInfo);

    MoveDtO m1 = new MoveDtO("F6", "G7", "white");
    MoveDtO m2 = new MoveDtO("A1", "A2", "black");
    agonGui.displayHistory(List.of(m1, m2));
    waitForRunLater();

    assertTrue(fakeController.lastInfo.contains("1. White : F6 -> G7"));
    assertTrue(fakeController.lastInfo.contains("1. Black : A1 -> A2"));
  }

  @Test
  @Order(2)
  void testOnMatchUpdate_NullMatch() {
    assertDoesNotThrow(() -> agonGui.onMatchUpdate(null));
  }

  @Test
  @Order(3)
  void testOnMatchUpdate_StandardMatch() throws InterruptedException {
    FakeMatch match = new FakeMatch();

    agonGui.onMatchUpdate(match);
    waitForRunLater();
    assertTrue(fakeController.lastMessage.contains("Current Player: WHITE"));

    match.isOver = true;
    agonGui.onMatchUpdate(match);
    waitForRunLater();
    assertTrue(fakeController.lastInfo.contains("Winner: None"));

    match.winnerColor = Color.WHITE;
    agonGui.onMatchUpdate(match);
    waitForRunLater();
    assertTrue(fakeController.lastInfo.contains("Winner: WHITE"));
  }

  @Test
  @Order(4)
  void testOnMatchUpdate_BlitzMatch_Timeline() throws InterruptedException {
    FakeBlitzMatch blitz = new FakeBlitzMatch();

    agonGui.onMatchUpdate(blitz);
    waitForRunLater();
    assertTrue(fakeController.lastMessage.contains("Time left: 04:59"));

    agonGui.onMatchUpdate(blitz);
    waitForRunLater();

    blitz.isOver = true;
    blitz.winnerColor = Color.BLACK;
    agonGui.onMatchUpdate(blitz);
    waitForRunLater();
    assertTrue(fakeController.lastInfo.contains("Winner: BLACK"));
  }

  @Test
  @Order(5)
  void testGetConfig() {
    assertNotNull(agonGui.getConfig());
  }

  @Test
  @Order(6)
  void testCommandQueue_SendAndReceive() {
    agonGui.sendCommand("new --blitz true");
    String receivedCommand = agonGui.getUserInput();
    assertEquals("new --blitz true", receivedCommand);
  }

  @Test
  @Order(7)
  void testIsRunning() {
    assertTrue(agonGui.isRunning());
  }

  @Test
  @Order(8)
  void testGettersAndSetters() {
    assertFalse(agonGui.getDebugMode().get());
    assertDoesNotThrow(() -> agonGui.setVerbose(true));
  }

  @Test
  @Order(9)
  void testShowMethods_WithController() throws InterruptedException {
    agonGui.showError("A test error");
    agonGui.showWarn("A test warning");
    agonGui.showHelp();

    waitForRunLater();

    assertEquals("A test error", fakeController.lastError);
    assertEquals("A test warning", fakeController.lastWarn);
    assertTrue(fakeController.helpCalled);
  }

  @Test
  @Order(10)
  void testShowMethods_NullController() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          try {
            Field controllerField = AgonApp.class.getDeclaredField("controller");
            controllerField.setAccessible(true);
            controllerField.set(null, null);
          } catch (Exception e) {
          }
          latch.countDown();
        });
    latch.await(2, TimeUnit.SECONDS);

    assertDoesNotThrow(
        () -> {
          agonGui.showError("Silent error");
          agonGui.showWarn("Silent warn");
          agonGui.showHelp();
        });

    waitForRunLater();
  }

  @Test
  @Order(11)
  void testUpdateStatusMessage_Branches() throws Exception {

    java.lang.reflect.Method method = AgonGui.class.getDeclaredMethod("updateStatusMessage");
    method.setAccessible(true);

    Field matchField = AgonGui.class.getDeclaredField("currentMatch");
    matchField.setAccessible(true);

    matchField.set(agonGui, null);
    assertDoesNotThrow(() -> method.invoke(agonGui));

    FakeMatch finishedMatch = new FakeMatch();
    finishedMatch.isOver = true;
    matchField.set(agonGui, finishedMatch);
    assertDoesNotThrow(() -> method.invoke(agonGui));
  }

  @Test
  @Order(12)
  void testQuit_SurefireBranch() {

    String originalCp = System.getProperty("java.class.path");
    try {
      System.setProperty("java.class.path", "dummy_path_surefire_test");
      agonGui.quit();
      assertFalse(agonGui.isRunning());
    } finally {
      if (originalCp != null) {
        System.setProperty("java.class.path", originalCp);
      }
    }
  }

  @Test
  @Order(13)
  void testShowMethods_NullMessages() throws InterruptedException {
    agonGui.showInfo(null);
    agonGui.showMessage(null);

    waitForRunLater();

    assertEquals("", fakeController.lastInfo);
    assertEquals("", fakeController.lastMessage);
  }

  @Test
  @Order(14)
  void testUpdateBoard_Branches() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          try {
            GameViewController validCanvasController =
                new GameViewController() {
                  HexagonCanvas realCanvas = new HexagonCanvas();

                  @Override
                  public HexagonCanvas getHexCanvas() {
                    return realCanvas;
                  }
                };
            Field controllerField = AgonApp.class.getDeclaredField("controller");
            controllerField.setAccessible(true);
            controllerField.set(null, validCanvasController);
            agonGui.updateBoard(new AgonBoardImpl());
            controllerField.set(null, null);
            agonGui.updateBoard(new AgonBoardImpl());

          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            latch.countDown();
          }
        });
    latch.await(2, TimeUnit.SECONDS);
  }

  @Test
  @Order(100)
  void testQuit() {
    assertTrue(agonGui.isRunning());
    agonGui.quit();
    assertFalse(agonGui.isRunning());
  }
}
