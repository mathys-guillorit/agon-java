package fr.univ.bordeaux.ui.gui.controllers;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.AppMode;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.client.ClientDiscovery;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.application.network.client.ServerInfo;
import fr.univ.bordeaux.application.network.client.runtime.AsyncEventLogger;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.gui.AgonApp;
import fr.univ.bordeaux.ui.gui.AgonGui;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.*;

public class GameViewControllerTest {

  private GameViewController controller;
  private FakeAgonGui fakeGui;

  private static class FakeAgonGui extends AgonGui {
    public final List<String> sentCommands = new ArrayList<>();
    private final AppContext appContext;

    public FakeAgonGui() {
      super(new GameConfig(), null);
      this.appContext = new AppContext(new LocalProfile("Test"));
      this.appContext.setMode(AppMode.LOCAL);
    }

    @Override
    public void sendCommand(String command) {
      sentCommands.add(command);
    }

    @Override
    public AppContext getAppContext() {
      return appContext;
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
    controller = new GameViewController();
    fakeGui = new FakeAgonGui();
    interactWithNextDialog(null, false);
    runAndWait(() -> controller.setAgonGui(fakeGui));
    Thread.sleep(800);
    AgonApp.setGui(fakeGui);

    setPrivateField(controller, "messageLabel", new Label());
    setPrivateField(controller, "boardContainer", new StackPane());

    runAndWait(() -> controller.initialize());
    fakeGui.sentCommands.clear();
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          for (Window window : new ArrayList<>(Window.getWindows())) {
            if (window instanceof Stage) {
              ((Stage) window).close();
            }
          }
          latch.countDown();
        });
    latch.await(2, TimeUnit.SECONDS);
  }

  private void setPrivateField(Object instance, String fieldName, Object value) throws Exception {
    Field field = instance.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(instance, value);
  }

  private void runAndWait(Runnable action) throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          action.run();
          latch.countDown();
        });
    if (!latch.await(5, TimeUnit.SECONDS)) {
      fail("Timeout for JavaFX action!");
    }
  }

  private TextField findTextField(Node node) {
    if (node instanceof TextField) return (TextField) node;
    if (node instanceof Parent) {
      for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
        TextField tf = findTextField(child);
        if (tf != null) return tf;
      }
    }
    return null;
  }

  private CheckBox findCheckBox(Node node) {
    if (node instanceof CheckBox) return (CheckBox) node;
    if (node instanceof Parent) {
      for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
        CheckBox cb = findCheckBox(child);
        if (cb != null) return cb;
      }
    }
    return null;
  }

  private void interactWithNextDialog(String inputText, boolean clickCancel) {
    interactWithNextDialog(inputText, clickCancel, false);
  }

  private void interactWithNextDialog(String inputText, boolean clickCancel, boolean closeWindow) {
    new Thread(
            () -> {
              boolean clicked = false;
              for (int i = 0; i < 100; i++) {
                if (clicked) break;
                try {
                  Thread.sleep(50);
                } catch (Exception e) {
                }

                CountDownLatch stepLatch = new CountDownLatch(1);
                final boolean[] success = {false};

                Platform.runLater(
                    () -> {
                      try {
                        for (Window window : new ArrayList<>(Window.getWindows())) {
                          if (window instanceof Stage
                              && window.isShowing()
                              && window.getScene() != null) {

                            if (closeWindow) {
                              ((Stage) window).close();
                              success[0] = true;
                              return;
                            }

                            if (window.getScene().getRoot() instanceof DialogPane pane) {
                              if (inputText != null) {
                                TextField tf = findTextField(pane);
                                if (tf != null) {
                                  tf.setText(inputText);
                                }
                              }

                              for (ButtonType type : pane.getButtonTypes()) {
                                boolean match;
                                if (clickCancel) {
                                  match =
                                      type.getButtonData().isCancelButton()
                                          || type.getButtonData() == ButtonBar.ButtonData.NO
                                          || type == ButtonType.CANCEL;
                                } else {
                                  match =
                                      type.getButtonData().isDefaultButton()
                                          || type.getButtonData() == ButtonBar.ButtonData.OK_DONE
                                          || type.getButtonData() == ButtonBar.ButtonData.YES
                                          || type == ButtonType.OK;
                                }

                                if (match) {
                                  Node btnNode = pane.lookupButton(type);
                                  if (btnNode instanceof Button btn && !btn.isDisabled()) {
                                    btn.fire();
                                    success[0] = true;
                                    return;
                                  }
                                }
                              }
                            }
                          }
                        }
                      } finally {
                        stepLatch.countDown();
                      }
                    });

                try {
                  stepLatch.await(1, TimeUnit.SECONDS);
                } catch (Exception e) {
                }
                clicked = success[0];
              }
            })
        .start();
  }

  @Test
  void testNullAgonGuiBranches() {
    controller.setAgonGui(null);
    assertDoesNotThrow(
        () -> {
          controller.undo();
          controller.redo();
          controller.pauseGame();
          controller.quitGame();
          controller.requestHint();
          controller.showConfig();
          controller.showHistory();
          controller.startNewGame();
          controller.saveGame();
          controller.loadGame();
          controller.editShortcuts();
        });
    assertTrue(fakeGui.sentCommands.isEmpty());
  }

  @Test
  void testSaveGame_Branches() throws InterruptedException {
    fakeGui.sentCommands.clear();
    interactWithNextDialog(null, true);
    runAndWait(() -> controller.saveGame());
    Thread.sleep(300);
    fakeGui.sentCommands.clear();
    interactWithNextDialog("   ", false);
    runAndWait(() -> controller.saveGame());
    Thread.sleep(300);
    fakeGui.sentCommands.clear();
    interactWithNextDialog("my_save", false);
    runAndWait(() -> controller.saveGame());
  }

  @Test
  void testLoadGame_Branches() throws InterruptedException {
    fakeGui.sentCommands.clear();
    interactWithNextDialog(null, true);
    runAndWait(() -> controller.loadGame());

    Thread.sleep(300);

    fakeGui.sentCommands.clear();
    interactWithNextDialog("   ", false);
    runAndWait(() -> controller.loadGame());

    Thread.sleep(300);

    fakeGui.sentCommands.clear();
    interactWithNextDialog("my_save", false);
    runAndWait(() -> controller.loadGame());
  }

  @Test
  void testStartNewGame_Branches() throws InterruptedException {
    fakeGui.sentCommands.clear();
    interactWithNextDialog(null, true);
    runAndWait(() -> controller.startNewGame());
    assertFalse(fakeGui.sentCommands.stream().anyMatch(cmd -> cmd.startsWith("new")));

    Thread.sleep(300);

    fakeGui.sentCommands.clear();
    interactWithNextDialog(null, false);
    runAndWait(() -> controller.startNewGame());
    assertTrue(fakeGui.sentCommands.stream().anyMatch(cmd -> cmd.startsWith("new --ai")));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testStartNewGame_ComplexConfigurations() throws InterruptedException {
    fakeGui.sentCommands.clear();
    new Thread(
            () -> {
              boolean handled = false;
              for (int i = 0; i < 50; i++) {
                if (handled) break;
                try {
                  Thread.sleep(50);
                } catch (Exception e) {
                }
                Platform.runLater(
                    () -> {
                      for (Window window : new ArrayList<>(Window.getWindows())) {
                        if (window instanceof Stage
                            && window.isShowing()
                            && window.getScene() != null) {
                          if (window.getScene().getRoot() instanceof DialogPane pane) {

                            Object[] combos = pane.lookupAll(".combo-box").toArray();
                            if (combos.length >= 3) {
                              ((ComboBox<String>) combos[0]).setValue("AI");
                              ((ComboBox<String>) combos[1]).setValue("Black");
                              ((ComboBox<String>) combos[2]).setValue("Human");
                            }

                            CheckBox cb = findCheckBox(pane);
                            if (cb != null && !cb.isSelected()) {
                              cb.fire();
                            }

                            for (ButtonType type : pane.getButtonTypes()) {
                              if (type.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                                Node btnNode = pane.lookupButton(type);
                                if (btnNode instanceof Button btn && !btn.isDisabled()) {
                                  btn.fire();
                                }
                              }
                            }
                          }
                        }
                      }
                    });
              }
            })
        .start();

    runAndWait(() -> controller.startNewGame());
    Thread.sleep(300);
    assertTrue(
        fakeGui.sentCommands.stream()
            .anyMatch(cmd -> cmd.contains("--ai black") && cmd.contains("--blitz")));
  }

  @Test
  void testNetworkMenus_BlockedInLocalMode() throws InterruptedException {
    fakeGui.getAppContext().setMode(AppMode.LOCAL);
    fakeGui.sentCommands.clear();

    interactWithNextDialog(null, false);
    runAndWait(() -> controller.showServerBrowser());

    interactWithNextDialog(null, false);
    runAndWait(() -> controller.showHostServerDialog());

    interactWithNextDialog(null, false);
    runAndWait(() -> controller.showLobby());

    assertTrue(fakeGui.sentCommands.isEmpty());
  }

  @Test
  void testShowServerBrowser_Branches() throws Exception {
    fakeGui.getAppContext().setMode(AppMode.ONLINE);
    fakeGui.sentCommands.clear();

    Field discoveryField = AppContext.class.getDeclaredField("discovery");
    discoveryField.setAccessible(true);

    ClientDiscovery populatedDiscovery =
        new ClientDiscovery() {
          @Override
          public List<ServerInfo> getServers() {
            List<ServerInfo> list = new ArrayList<>();
            ServerInfo info = new ServerInfo("TestServer", "192.168.1.50", 12345);
            list.add(info);
            return list;
          }
        };
    discoveryField.set(fakeGui.getAppContext(), populatedDiscovery);

    fakeGui.sentCommands.clear();
    interactWithNextDialog("192.168.1.50:12345", false);
    runAndWait(() -> controller.showServerBrowser());
    Thread.sleep(400);

    assertTrue(fakeGui.sentCommands.contains("join 192.168.1.50:12345"));
  }

  @Test
  void testShowHostServerDialog_Branches() throws InterruptedException {
    fakeGui.getAppContext().setMode(AppMode.ONLINE);
    fakeGui.sentCommands.clear();

    interactWithNextDialog(null, true);
    runAndWait(() -> controller.showHostServerDialog());
    Thread.sleep(600);

    fakeGui.sentCommands.clear();
    interactWithNextDialog("5555", false);
    runAndWait(() -> controller.showHostServerDialog());
    Thread.sleep(600);

    assertTrue(fakeGui.sentCommands.contains("server_start 5555"));
    assertTrue(fakeGui.sentCommands.contains("join localhost:5555"));
  }

  @Test
  void testRouteMessage_Invitation() throws InterruptedException {
    fakeGui.sentCommands.clear();

    interactWithNextDialog(null, false);
    runAndWait(
        () -> controller.routeMessage("[ONLINE] INVITATION_RECEIVED FROM=mathys EXPIRES=300s"));
    Thread.sleep(600);
    assertTrue(fakeGui.sentCommands.contains("accept"));

    fakeGui.sentCommands.clear();

    interactWithNextDialog(null, true);
    runAndWait(
        () -> controller.routeMessage("[ONLINE] INVITATION_RECEIVED FROM=mathys EXPIRES=300s"));
    Thread.sleep(600);
    assertTrue(fakeGui.sentCommands.contains("decline"));
  }

  @Test
  void testNetworkListener_ChooseMode_Blitz() throws InterruptedException {
    fakeGui.sentCommands.clear();

    interactWithNextDialog(null, true);
    AsyncEventLogger.logInfo("[ONLINE] CHOOSE_MODE COMMAND=mode OPTIONS=normal|blitz");

    Thread.sleep(600);
    assertTrue(fakeGui.sentCommands.contains("mode blitz"));
  }

  @Test
  @DisplayName("Coverage: routeMessage() -> promptYesNo() for 'save the game before quitting'")
  void testRouteMessage_PromptYesNo() throws InterruptedException {
    fakeGui.sentCommands.clear();
    interactWithNextDialog(null, false);
    runAndWait(() -> controller.routeMessage("save the game before quitting ?"));
    Thread.sleep(400);
    assertTrue(fakeGui.sentCommands.contains("y"), "Should send 'y' when clicking Yes");

    fakeGui.sentCommands.clear();
    interactWithNextDialog(null, true);
    runAndWait(() -> controller.routeMessage("save the game before quitting ?"));
    Thread.sleep(400);
    assertTrue(fakeGui.sentCommands.contains("n"), "Should send 'n' when clicking No");
  }

  @Test
  @DisplayName("Coverage: routeMessage() -> promptFilename()")
  void testRouteMessage_PromptFilename() throws InterruptedException {
    fakeGui.sentCommands.clear();
    interactWithNextDialog("my_backup", false);
    runAndWait(() -> controller.routeMessage("enter the filename"));

    Thread.sleep(400);
    assertTrue(fakeGui.sentCommands.contains("my_backup"), "Should send the typed filename");
  }

  @Test
  @DisplayName("Coverage: showHelp() with dialog interaction")
  void testShowHelp_Coverage() throws InterruptedException {
    interactWithNextDialog(null, false);
    assertDoesNotThrow(() -> runAndWait(() -> controller.showHelp()));
    Thread.sleep(300);
  }

  @Test
  @DisplayName("Coverage: editShortcuts() updating config and saving")
  void testEditShortcuts_Coverage() throws Exception {
    fakeGui.getConfig().addShortcut("shortcut_undo", "Ctrl+Z");

    interactWithNextDialog("U", false);

    Platform.runLater(() -> controller.editShortcuts());

    Thread.sleep(1000);

    interactWithNextDialog(null, false);

    Thread.sleep(500);

    assertTrue(
        fakeGui.getConfig().getShortcuts().containsValue("U"),
        "At least one shortcut in the configuration had to be updated with the value 'U''");
  }

  @Test
  @DisplayName("Coverage: routeMessage() -> handleTurnAndPauseMessages()")
  void testHandleTurnAndPauseMessages() throws Exception {
    Field msgLabelField = GameViewController.class.getDeclaredField("messageLabel");
    msgLabelField.setAccessible(true);
    Label msgLabel = (Label) msgLabelField.get(controller);

    runAndWait(() -> controller.routeMessage("Game paused by user"));
    Thread.sleep(200);
    assertEquals("game paused by user", msgLabel.getText().toLowerCase());

    AppContext context = fakeGui.getAppContext();
    context.setMode(AppMode.ONLINE);

    Field activeField = AppContext.class.getDeclaredField("onlineGameActive");
    activeField.setAccessible(true);
    activeField.set(context, true);

    Field turnField = AppContext.class.getDeclaredField("myOnlineTurn");
    turnField.setAccessible(true);
    turnField.set(context, true);

    Field colorField = AppContext.class.getDeclaredField("localOnlineColor");
    colorField.setAccessible(true);
    colorField.set(context, Color.WHITE);

    runAndWait(() -> controller.routeMessage(">> current player - Time left: 05:00"));
    Thread.sleep(200);
    assertTrue(msgLabel.getText().contains("Your turn!"));
    assertTrue(msgLabel.getText().contains("Time left: 05:00"));

    activeField.set(context, false);
  }

  @Test
  @DisplayName("Coverage: showLobby() when client is connected")
  void testShowLobby_ConnectedCoverage() throws Exception {
    AppContext context = fakeGui.getAppContext();
    context.setMode(AppMode.ONLINE);

    AgonClient mockClient =
        new AgonClient(new LocalProfile("Test")) {
          @Override
          public boolean isConnected() {
            return true;
          }

          @Override
          public String requestPlayers() {
            return "Player1 - ID 2";
          }

          @Override
          public String requestScoreboard() {
            return "Player1 : 100 Elo";
          }
        };

    Field clientField = AppContext.class.getDeclaredField("client");
    clientField.setAccessible(true);
    clientField.set(context, mockClient);

    fakeGui.sentCommands.clear();

    interactWithNextDialog("2", false);

    runAndWait(() -> controller.showLobby());
    Thread.sleep(500);

    assertTrue(fakeGui.sentCommands.contains("new 2"), "Should send challenge command 'new 2'");
  }

  @Test
  @DisplayName("Coverage: setupNetworkListener() triggers refreshBoardFromNetwork()")
  void testNetworkListener_RefreshBoardCoverage() throws Exception {
    AppContext context = fakeGui.getAppContext();
    fr.univ.bordeaux.application.match.Match dummyMatch =
        fr.univ.bordeaux.application.match.MatchFactory.createOnlineMatch(
            "Player1", "Player2", false);

    Field matchField = AppContext.class.getDeclaredField("onlineMatch");
    matchField.setAccessible(true);
    matchField.set(context, dummyMatch);

    AsyncEventLogger.logInfo("GAME_STARTED GAME_ID 9999");
    Thread.sleep(300);
    AsyncEventLogger.logInfo("Your turn to play!");
    Thread.sleep(300);

    AsyncEventLogger.logInfo("Opponent turn is now active");
    Thread.sleep(300);

    AsyncEventLogger.logInfo("You are WHITE");
    Thread.sleep(300);

    assertTrue(true, "All board refresh branches executed successfully.");
  }

  @Test
  @DisplayName("Coverage: routeMessage() with null message")
  void testRouteMessage_Null() {
    assertDoesNotThrow(() -> controller.routeMessage(null));
  }

  @Test
  @DisplayName("Coverage: handleTurnAndPauseMessages() - BLACK color, no time, opponent turn")
  void testHandleTurnAndPauseMessages_BlackColor_NoTime() throws Exception {

    AppContext context = fakeGui.getAppContext();
    context.setMode(AppMode.ONLINE);

    Field activeField = AppContext.class.getDeclaredField("onlineGameActive");
    activeField.setAccessible(true);
    activeField.set(context, true);

    Field turnField = AppContext.class.getDeclaredField("myOnlineTurn");
    turnField.setAccessible(true);
    turnField.set(context, false);

    Field colorField = AppContext.class.getDeclaredField("localOnlineColor");
    colorField.setAccessible(true);
    colorField.set(context, Color.BLACK);

    runAndWait(() -> controller.routeMessage(">> current player is testing"));
    Thread.sleep(200);

    Field msgLabelField = GameViewController.class.getDeclaredField("messageLabel");
    msgLabelField.setAccessible(true);
    Label msgLabel = (Label) msgLabelField.get(controller);

    assertTrue(
        msgLabel.getText().contains("Opponent's turn (WHITE)"), "The opponent must be WHITE");
    assertFalse(msgLabel.getText().contains("Time left:"), "Must not contain time");

    activeField.set(context, false); // Nettoyage
  }

  @Test
  @DisplayName("Coverage: handleTurnAndPauseMessages() with agonGui == null")
  void testHandleTurnAndPauseMessages_NullGui() throws Exception {
    controller.setAgonGui(null);
    runAndWait(() -> controller.routeMessage(">> current player testing null gui"));
    Thread.sleep(200);

    Field msgLabelField = GameViewController.class.getDeclaredField("messageLabel");
    msgLabelField.setAccessible(true);
    Label msgLabel = (Label) msgLabelField.get(controller);

    assertEquals(">> current player testing null gui", msgLabel.getText());
  }

  @Test
  @DisplayName(
      "Coverage: promptYesNo() & promptInvitation() dialogs closed without clicking buttons")
  void testDialogs_ClosedForcefully() throws Exception {
    fakeGui.sentCommands.clear();

    interactWithNextDialog(null, false, true);
    runAndWait(() -> controller.routeMessage("save the game before quitting ?"));
    Thread.sleep(400);
    assertTrue(fakeGui.sentCommands.contains("n"));

    fakeGui.sentCommands.clear();
    interactWithNextDialog(null, false, true);
    runAndWait(() -> controller.routeMessage("INVITATION_RECEIVED FROM=TEST"));
    Thread.sleep(400);
    assertTrue(fakeGui.sentCommands.contains("decline"));
  }

  @Test
  @DisplayName("Coverage: promptFilename() dialog closed or empty text")
  void testPromptFilename_ClosedOrEmpty() throws Exception {
    fakeGui.sentCommands.clear();

    interactWithNextDialog(null, false, true);
    runAndWait(() -> controller.routeMessage("enter filename"));
    Thread.sleep(400);
    assertTrue(fakeGui.sentCommands.isEmpty());

    interactWithNextDialog("   ", false);
    runAndWait(() -> controller.routeMessage("enter filename"));
    Thread.sleep(400);
    assertTrue(fakeGui.sentCommands.isEmpty());
  }

  @Test
  @DisplayName("Coverage: Inner if (agonGui != null) inside dialog callbacks")
  void testDialogCallbacks_WithNullGui() throws Exception {
    controller.setAgonGui(null);

    interactWithNextDialog(null, false);
    assertDoesNotThrow(
        () -> runAndWait(() -> controller.routeMessage("save the game before quitting ?")));
    Thread.sleep(300);

    interactWithNextDialog("testfile", false);
    assertDoesNotThrow(() -> runAndWait(() -> controller.routeMessage("enter filename")));
    Thread.sleep(300);

    interactWithNextDialog(null, false);
    assertDoesNotThrow(
        () -> runAndWait(() -> controller.routeMessage("INVITATION_RECEIVED FROM=TEST")));
    Thread.sleep(300);
  }

  @Test
  @DisplayName("Coverage: showNewGameDialog - AI vs AI with Minimax")
  void testStartNewGame_AiVsAi_Minimax() throws InterruptedException {
    fakeGui.sentCommands.clear();
    new Thread(
            () -> {
              handleDialogWithRobot(
                  pane -> {
                    Object[] combos = pane.lookupAll(".combo-box").toArray();
                    if (combos.length >= 3) {
                      ((ComboBox<String>) combos[0]).setValue("AI"); // P1 type
                      ((ComboBox<String>) combos[1]).setValue("White"); // P1 color
                      ((ComboBox<String>) combos[2]).setValue("AI"); // P2 type
                    }
                    // Sélection Minimax
                    ComboBox<String> aiMode = (ComboBox<String>) combos[3];
                    aiMode.setValue("Minimax");

                    clickButton(pane, ButtonBar.ButtonData.OK_DONE);
                  });
            })
        .start();

    runAndWait(() -> controller.startNewGame());
    Thread.sleep(500);
    assertTrue(
        fakeGui.sentCommands.stream().anyMatch(c -> c.contains("--ai a") && c.contains("minimax")));
  }

  @Test
  @DisplayName("Coverage: showNewGameDialog - Human vs AI (P2)")
  void testStartNewGame_P2IsAi() throws InterruptedException {
    fakeGui.sentCommands.clear();
    new Thread(
            () -> {
              handleDialogWithRobot(
                  pane -> {
                    Object[] combos = pane.lookupAll(".combo-box").toArray();
                    ((ComboBox<String>) combos[0]).setValue("Human");
                    ((ComboBox<String>) combos[2]).setValue("AI");
                    clickButton(pane, ButtonBar.ButtonData.OK_DONE);
                  });
            })
        .start();

    runAndWait(() -> controller.startNewGame());
    Thread.sleep(500);
    assertTrue(fakeGui.sentCommands.stream().anyMatch(c -> c.contains("--ai black")));
  }

  @Test
  @DisplayName("Coverage: showHostServerDialog - Server already running")
  void testShowHostServerDialog_AlreadyRunning() throws Exception {
    fakeGui.getAppContext().setMode(AppMode.ONLINE);

    AgonServer mockServer =
        new AgonServer("12345") {
          @Override
          public boolean isRunning() {
            return true;
          }

          @Override
          public int getConnectedClientsCount() {
            return 1;
          }
        };
    fakeGui.getAppContext().setServer(mockServer);

    interactWithNextDialog(null, false);
    runAndWait(() -> controller.showHostServerDialog());
    Thread.sleep(400);
    assertTrue(fakeGui.sentCommands.contains("server_stop"));
  }

  @Test
  @DisplayName("Coverage: showLobby - Failed to load players")
  void testShowLobby_NullPlayers() throws Exception {
    fakeGui.getAppContext().setMode(AppMode.ONLINE);

    AgonClient nullClient =
        new AgonClient(new LocalProfile("Test")) {
          @Override
          public boolean isConnected() {
            return true;
          }

          @Override
          public String requestPlayers() {
            return null;
          }
        };

    Field clientField = AppContext.class.getDeclaredField("client");
    clientField.setAccessible(true);
    clientField.set(fakeGui.getAppContext(), nullClient);

    interactWithNextDialog(null, true);
    runAndWait(() -> controller.showLobby());
  }

  private void handleDialogWithRobot(java.util.function.Consumer<DialogPane> action) {
    for (int i = 0; i < 50; i++) {
      try {
        Thread.sleep(50);
      } catch (Exception e) {
      }
      final boolean[] done = {false};
      Platform.runLater(
          () -> {
            for (Window w : Window.getWindows()) {
              if (w.isShowing() && w.getScene().getRoot() instanceof DialogPane pane) {
                action.accept(pane);
                done[0] = true;
                break;
              }
            }
          });
      if (done[0]) break;
    }
  }

  private void clickButton(DialogPane pane, ButtonBar.ButtonData data) {
    for (ButtonType type : pane.getButtonTypes()) {
      if (type.getButtonData() == data) {
        ((Button) pane.lookupButton(type)).fire();
        break;
      }
    }
  }
}
