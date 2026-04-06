package fr.univ.bordeaux.ui.gui;

import fr.univ.bordeaux.ui.gui.controllers.GameViewController;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class AgonApp extends Application {
  private static GameViewController controller;
  private static Scene scene;
  private static AgonGui agonGui;

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/game_view.fxml"));
    Parent root = loader.load();
    scene = new Scene(root, 900, 700);
    stage.setTitle("Agon - GUI Mode");
    stage.setScene(scene);
    stage.show();
    controller = loader.getController();
    controller.setAgonGui(agonGui);
    this.setupShortcuts();
  }

  // shortcuts

    public static void refreshShortcuts() {
      if (scene != null && agonGui != null) {
          scene.getAccelerators().clear();
          new AgonApp().setupShortcuts();
      }
    }

  /** Add shortcuts to GUI. */
  void setupShortcuts() {
      if ( agonGui == null || agonGui.getConfig() == null || controller == null || scene == null ) {
          return;
      }
      /*
      Map<String, String> conf = agonGui.getConfig().getShortcuts();

      bindShortcut(conf.get("shortcut_new"), controller::startNewGame);
      bindShortcut(conf.get("shortcut_load"), controller::loadGame);
      bindShortcut(conf.get("shortcut_save"), controller::saveGame);
      bindShortcut(conf.get("shortcut_config"), controller::showConfig);
      bindShortcut(conf.get("shortcut_info"), controller::showVersion);
      bindShortcut(conf.get("shortcut_quit"), controller::quitGame);
      bindShortcut(conf.get("shortcut_undo"), controller::undo);
      bindShortcut(conf.get("shortcut_redo"), controller::redo);
      bindShortcut(conf.get("shortcut_pause"), controller::pauseGame);
      bindShortcut(conf.get("shortcut_hint"), controller::requestHint);
      */
  }

  private void bindShortcut(String shortcut, Runnable runnable) {
      if (shortcut != null &&  !shortcut.isBlank()) {
          try {
              scene.getAccelerators().put(KeyCombination.valueOf(shortcut), runnable);
          } catch (IllegalArgumentException e) {
              System.err.println("[WARNING] Invalid shortcut " + shortcut);
          }
      }
  }

  ///  getters and setters

  public static GameViewController getController() {
    return controller;
  }

  public static void setGui(AgonGui gui) {
    agonGui = gui;
  }
}
