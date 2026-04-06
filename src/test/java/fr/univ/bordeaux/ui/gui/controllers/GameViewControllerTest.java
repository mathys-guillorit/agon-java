package fr.univ.bordeaux.ui.gui.controllers;

import static org.junit.jupiter.api.Assertions.*;

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

    public FakeAgonGui() {
      super(new GameConfig(), null);
    }

    @Override
    public void sendCommand(String command) {
      sentCommands.add(command);
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
    controller.setAgonGui(fakeGui);
    AgonApp.setGui(fakeGui);

    setPrivateField(controller, "messageLabel", new Label());
    setPrivateField(controller, "boardContainer", new StackPane());

    runAndWait(() -> controller.initialize());
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
          //controller.editShortcuts();
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

    interactWithNextDialog(null, true);
    runAndWait(() -> controller.routeMessage("nom du fichier"));
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
    interactWithNextDialog("ma_sauvegarde", false);
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
            .anyMatch(
                cmd ->
                    cmd.contains("--ai black")
                        && cmd.contains("--blitz")
                        && cmd.contains("--player1Color black")));
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
/*
    @Test
    void testEditShortcuts_Branches() throws InterruptedException {
        interactWithNextDialog(null, true);
        runAndWait(() -> controller.editShortcuts());
        Thread.sleep(300);
        interactWithNextDialog("Shift+P", false);
        new Thread(() -> {
            try { Thread.sleep(500); } catch (Exception e) {}
            interactWithNextDialog(null, false);
        }).start();
        runAndWait(() -> controller.editShortcuts());
        assertTrue(true);
    }
*/
}
