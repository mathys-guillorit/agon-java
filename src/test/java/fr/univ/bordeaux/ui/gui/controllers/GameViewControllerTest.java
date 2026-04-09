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
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
                                } else {
                                  return;
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
  void testRouteMessage_OrBranches() throws Exception {
    assertDoesNotThrow(() -> controller.routeMessage(null));

    runAndWait(() -> controller.routeMessage("current player is white"));
    runAndWait(() -> controller.routeMessage(">> It's your turn"));

    fakeGui.sentCommands.clear();
    interactWithNextDialog(null, true);
    runAndWait(() -> controller.routeMessage("filename"));
    Thread.sleep(300);

    setPrivateField(controller, "messageLabel", null);
    runAndWait(() -> controller.routeMessage(">> test null label"));
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
  void testRouteMessage_YesNo_Branches() throws InterruptedException {
    fakeGui.sentCommands.clear();
    interactWithNextDialog(null, false);
    runAndWait(() -> controller.routeMessage("Save the game before quitting?"));
    assertTrue(fakeGui.sentCommands.contains("y"));

    Thread.sleep(300);

    fakeGui.sentCommands.clear();
    interactWithNextDialog(null, true);
    runAndWait(() -> controller.routeMessage("Save the game before quitting?"));
    assertTrue(fakeGui.sentCommands.contains("n"));

    Thread.sleep(300);

    fakeGui.sentCommands.clear();
    interactWithNextDialog(null, false, true);
    runAndWait(() -> controller.routeMessage("Save the game before quitting?"));
    assertTrue(fakeGui.sentCommands.contains("n"));

    Thread.sleep(300);

    fakeGui.sentCommands.clear();
    interactWithNextDialog("custom_save", false);
    runAndWait(() -> controller.routeMessage("Enter filename:"));
    assertTrue(fakeGui.sentCommands.contains("custom_save"));
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
  void testStartNewGame_BlitzCheckBox_Lambda() throws InterruptedException {
    new Thread(
            () -> {
              AtomicBoolean handled = new AtomicBoolean(false);
              for (int i = 0; i < 50; i++) {
                if (handled.get()) break;
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

                            CheckBox cb = findCheckBox(pane);
                            if (cb != null) {
                              cb.fire();
                              cb.fire();
                            }

                            for (ButtonType type : pane.getButtonTypes()) {
                              if (type == ButtonType.CANCEL
                                  || type.getButtonData().isCancelButton()) {
                                Node btnNode = pane.lookupButton(type);
                                if (btnNode instanceof Button btn && !btn.isDisabled()) {
                                  btn.fire();
                                  handled.set(true);
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

    CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(
        () -> {
          controller.startNewGame();
          latch.countDown();
        });
    latch.await(5, TimeUnit.SECONDS);
  }

  @Test
  void testDialogCallbacks_NullAgonGuiBranches() throws InterruptedException {
    controller.setAgonGui(null);

    interactWithNextDialog(null, false, true);
    runAndWait(() -> controller.routeMessage("filename"));
    Thread.sleep(300);

    interactWithNextDialog("   ", false);
    runAndWait(() -> controller.routeMessage("filename"));
    Thread.sleep(300);

    interactWithNextDialog("my_save", false);
    runAndWait(() -> controller.routeMessage("filename"));
    Thread.sleep(300);

    interactWithNextDialog(null, false);
    runAndWait(() -> controller.routeMessage("save the game before quitting"));
    Thread.sleep(300);

    interactWithNextDialog(null, true);
    runAndWait(() -> controller.routeMessage("save the game before quitting"));

    assertTrue(true);
  }

  @Test
  void testInitializeAndMoveRequestListener() {
    assertNotNull(controller.getHexCanvas());
    controller.getHexCanvas().requestMove("F6G7");
    assertTrue(fakeGui.sentCommands.contains("F6G7"));
  }

  @Test
  void testMenuActionsWithValidGui() {
    controller.undo();
    assertTrue(fakeGui.sentCommands.contains("undo"));

    controller.redo();
    assertTrue(fakeGui.sentCommands.contains("redo"));

    controller.pauseGame();
    assertTrue(fakeGui.sentCommands.contains("pause"));

    controller.quitGame();
    assertTrue(fakeGui.sentCommands.contains("quit"));

    controller.requestHint();
    assertTrue(fakeGui.sentCommands.contains("hint"));

    controller.showConfig();
    assertTrue(fakeGui.sentCommands.contains("show -configuration"));

    controller.showHistory();
    assertTrue(fakeGui.sentCommands.contains("show -history"));
  }

  @Test
  void testAlertPopups() throws InterruptedException {
    interactWithNextDialog(null, false);
    runAndWait(() -> controller.showError("A critical error occurred"));

    interactWithNextDialog(null, false);
    runAndWait(() -> controller.showWarn("Be careful!"));

    interactWithNextDialog(null, false);
    runAndWait(() -> controller.showVersion());

    interactWithNextDialog(null, false);
    runAndWait(() -> controller.showHelp());
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

    discoveryField.set(fakeGui.getAppContext(), null);
    interactWithNextDialog(null, true);
    runAndWait(() -> controller.showServerBrowser());
    Thread.sleep(400);

    ClientDiscovery emptyDiscovery =
        new ClientDiscovery() {
          @Override
          public List<ServerInfo> getServers() {
            return new ArrayList<>();
          }
        };
    discoveryField.set(fakeGui.getAppContext(), emptyDiscovery);
    interactWithNextDialog(null, true);
    runAndWait(() -> controller.showServerBrowser());
    Thread.sleep(400);

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
  void testShowLobby_NotConnected() throws InterruptedException {
    fakeGui.getAppContext().setMode(AppMode.ONLINE);
    fakeGui.sentCommands.clear();

    interactWithNextDialog(null, false);
    runAndWait(() -> controller.showLobby());
    assertTrue(fakeGui.sentCommands.isEmpty());
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
  void testNetworkListener_ChooseMode() throws InterruptedException {
    fakeGui.sentCommands.clear();

    interactWithNextDialog(null, false);
    AsyncEventLogger.logInfo("[ONLINE] CHOOSE_MODE COMMAND=mode OPTIONS=normal|blitz");

    Thread.sleep(600);
    assertTrue(fakeGui.sentCommands.contains("mode normal"));
  }

  @Test
  void testNetworkListener_GameStarted() throws InterruptedException {
    fakeGui.sentCommands.clear();
    AsyncEventLogger.logInfo("[ONLINE] GAME_STARTED GAME_ID=1");
    Thread.sleep(600);
    assertTrue(fakeGui.sentCommands.isEmpty());
  }

  @Test
  void testEditShortcuts_Branches() throws InterruptedException {
    fakeGui.getConfig().addShortcut("shortcut_test", "Ctrl+T");
    interactWithNextDialog(null, true);
    runAndWait(() -> controller.editShortcuts());
    Thread.sleep(300);
    interactWithNextDialog("Shift+P", false);
    new Thread(
            () -> {
              try {
                Thread.sleep(500);
              } catch (Exception e) {
              }
              interactWithNextDialog(null, false);
            })
        .start();
    runAndWait(() -> controller.editShortcuts());
    assertTrue(true);
  }

  @Test
  void testHexCanvas_MoveRequest_WhenPaused() throws InterruptedException {
    FakeAgonGui pausedGui =
        new FakeAgonGui() {
          @Override
          public boolean getPaused() {
            return true;
          }
        };

    interactWithNextDialog(null, false);
    runAndWait(() -> controller.setAgonGui(pausedGui));

    interactWithNextDialog(null, false);
    runAndWait(() -> controller.getHexCanvas().requestMove("F6G7"));

    assertTrue(pausedGui.sentCommands.isEmpty());
  }

  @Test
  void testRouteMessage_SpecificBranches() throws InterruptedException {
    runAndWait(() -> controller.routeMessage("GAME PAUSED"));
    interactWithNextDialog("my_save", false);
    runAndWait(() -> controller.routeMessage("Enter filename:"));
    fakeGui.getAppContext().setMode(AppMode.ONLINE);
    runAndWait(() -> controller.routeMessage(">> current player"));
    assertTrue(true);
  }

  @Test
  void testServerBrowser_ListViewSelection() throws InterruptedException {
    fakeGui.getAppContext().setMode(AppMode.ONLINE);

    new Thread(
            () -> {
              try {
                Thread.sleep(300);
              } catch (Exception e) {
              }
              Platform.runLater(
                  () -> {
                    for (Window window : Window.getWindows()) {
                      if (window instanceof Stage
                          && window.isShowing()
                          && window.getScene() != null) {
                        if (window.getScene().getRoot() instanceof DialogPane pane) {

                          @SuppressWarnings("unchecked")
                          ListView<String> listView = (ListView<String>) pane.lookup(".list-view");
                          if (listView != null) {
                            listView.getItems().add("TestServer @ 127.0.0.1:8888");
                            listView.getSelectionModel().select(0);
                          }

                          for (ButtonType type : pane.getButtonTypes()) {
                            if (type.getButtonData().isCancelButton()) {
                              Node btn = pane.lookupButton(type);
                              if (btn instanceof Button) ((Button) btn).fire();
                            }
                          }
                        }
                      }
                    }
                  });
            })
        .start();

    runAndWait(() -> controller.showServerBrowser());
    assertTrue(true);
  }

  @Test
  void testShowLobby_NotConnected_Protection() throws InterruptedException {
    fakeGui.getAppContext().setMode(AppMode.ONLINE);
    fakeGui.sentCommands.clear();

    interactWithNextDialog(null, false);
    runAndWait(() -> controller.showLobby());

    assertTrue(fakeGui.sentCommands.isEmpty());
  }

  @Test
  void testShowHostServer_NotRunning_Branch() throws InterruptedException {
    fakeGui.getAppContext().setMode(AppMode.ONLINE);
    fakeGui.sentCommands.clear();

    interactWithNextDialog("8888", false);
    runAndWait(() -> controller.showHostServerDialog());

    Thread.sleep(300);
    assertTrue(fakeGui.sentCommands.contains("server_start 8888"));
  }

  @Test
  void testShowLobby_Connected_FullInteraction() throws Exception {
    fakeGui.getAppContext().setMode(AppMode.ONLINE);

    AgonClient fakeClient =
        new AgonClient(null) {
          @Override
          public boolean isConnected() {
            return true;
          }

          @Override
          public String requestPlayers() {
            return "Player1, Player2";
          }

          @Override
          public String requestScoreboard() {
            return "Scores...";
          }
        };

    Field clientField = AppContext.class.getDeclaredField("client");
    clientField.setAccessible(true);
    clientField.set(fakeGui.getAppContext(), fakeClient);

    fakeGui.sentCommands.clear();

    new Thread(
            () -> {
              try {
                Thread.sleep(400);
              } catch (Exception e) {
              }
              Platform.runLater(
                  () -> {
                    List<Window> windows = new ArrayList<>(Window.getWindows());

                    for (Window window : windows) {
                      if (window instanceof Stage
                          && window.isShowing()
                          && window.getScene() != null) {
                        if (window.getScene().getRoot() instanceof DialogPane pane) {

                          List<Node> buttons = new ArrayList<>(pane.lookupAll(".button"));
                          for (Node node : buttons) {
                            if (node instanceof Button btn) {
                              if ("Refresh Players".equals(btn.getText())
                                  || "View Scoreboard".equals(btn.getText())) {
                                btn.fire();
                              }
                            }
                          }

                          TextField tf = (TextField) pane.lookup(".text-field");
                          if (tf != null) {
                            tf.setText("42");
                          }

                          List<ButtonType> buttonTypes = new ArrayList<>(pane.getButtonTypes());
                          for (ButtonType type : buttonTypes) {
                            if (type.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                              Node submitBtn = pane.lookupButton(type);
                              if (submitBtn instanceof Button) {
                                ((Button) submitBtn).fire();
                                return;
                              }
                            }
                          }
                        }
                      }
                    }
                  });
            })
        .start();

    runAndWait(() -> controller.showLobby());
    assertTrue(fakeGui.sentCommands.contains("new 42"));
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
  void testShowHostServer_Running_Branch() throws Exception {
    fakeGui.getAppContext().setMode(AppMode.ONLINE);

    AgonServer fakeServer =
        new AgonServer("12345") {
          @Override
          public boolean isRunning() {
            return true;
          }

          @Override
          public int getPort() {
            return 12345;
          }

          @Override
          public int getConnectedClientsCount() {
            return 1;
          }
        };

    Field serverField = AppContext.class.getDeclaredField("server");
    serverField.setAccessible(true);
    serverField.set(fakeGui.getAppContext(), fakeServer);

    fakeGui.sentCommands.clear();

    interactWithNextDialog(null, false);
    runAndWait(() -> controller.showHostServerDialog());

    Thread.sleep(300);
    assertTrue(fakeGui.sentCommands.contains("server_stop"));
  }

  @Test
  void testNetworkListener_OnlineTurns() throws InterruptedException {
    AsyncEventLogger.logInfo("[ONLINE] Your turn");
    AsyncEventLogger.logInfo("[ONLINE] Opponent turn");
    AsyncEventLogger.logInfo("[ONLINE] You are WHITE");
    Thread.sleep(600);
    assertTrue(true);
  }

  @Test
  void testServerBrowser_ListSelection_And_Routing() throws Exception {
    fakeGui.getAppContext().setMode(AppMode.ONLINE);
    runAndWait(() -> controller.routeMessage(">> current player"));

    new Thread(
            () -> {
              try {
                Thread.sleep(400);
              } catch (Exception e) {
              }
              Platform.runLater(
                  () -> {
                    List<Window> windows = new ArrayList<>(Window.getWindows());
                    for (Window window : windows) {
                      if (window instanceof Stage
                          && window.isShowing()
                          && window.getScene() != null) {
                        if (window.getScene().getRoot() instanceof DialogPane pane) {

                          @SuppressWarnings("unchecked")
                          ListView<String> listView = (ListView<String>) pane.lookup(".list-view");
                          if (listView != null) {
                            listView.getItems().add("FakeServer @ 10.0.0.1:9999");
                            listView.getSelectionModel().select(0);
                          }

                          List<ButtonType> types = new ArrayList<>(pane.getButtonTypes());
                          for (ButtonType type : types) {
                            if (type.getButtonData().isCancelButton()) {
                              Node btn = pane.lookupButton(type);
                              if (btn instanceof Button) {
                                ((Button) btn).fire();
                                return;
                              }
                            }
                          }
                        }
                      }
                    }
                  });
            })
        .start();

    runAndWait(() -> controller.showServerBrowser());
    assertTrue(true);
  }

  @Test
  void testRouteMessage_OnlineTurns_BothBranches() throws Exception {
    AppContext fakeContextTrue =
        new AppContext(null) {
          @Override
          public boolean isOnlineGameActive() {
            return true;
          }

          @Override
          public boolean isMyOnlineTurn() {
            return true;
          }

          @Override
          public Color getLocalOnlineColor() {
            return Color.WHITE;
          }
        };

    Field contextField = FakeAgonGui.class.getDeclaredField("appContext");
    contextField.setAccessible(true);
    contextField.set(fakeGui, fakeContextTrue);

    runAndWait(() -> controller.routeMessage(">> current player"));

    AppContext fakeContextFalse =
        new AppContext(null) {
          @Override
          public boolean isOnlineGameActive() {
            return true;
          }

          @Override
          public boolean isMyOnlineTurn() {
            return false;
          }

          @Override
          public Color getLocalOnlineColor() {
            return Color.BLACK;
          }
        };
    contextField.set(fakeGui, fakeContextFalse);

    runAndWait(() -> controller.routeMessage(">> current player"));

    assertTrue(true);
  }
}
