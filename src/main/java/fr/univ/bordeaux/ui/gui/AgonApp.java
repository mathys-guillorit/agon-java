package fr.univ.bordeaux.ui.gui;

import fr.univ.bordeaux.ui.gui.controllers.GameViewController;
import java.io.IOException;
import java.util.Map;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * The main JavaFX Application class. Responsible for loading the FXML views, initializing the main
 * Stage, and binding keyboard shortcuts to the controller.
 */
public class AgonApp extends Application {

  /** The primary controller managing the game view. */
  private static GameViewController controller;

  /** The main JavaFX scene of the application. */
  private static Scene scene;

  /** The central GUI bridge instance. */
  private static AgonGui agonGui;

  /** Default constructor for the JavaFX Application. */
  public AgonApp() {}

  /**
   * The main entry point for the JavaFX application.
   *
   * @param stage The primary stage for this application.
   * @throws IOException If the FXML file cannot be loaded.
   */
  @Override
  public void start(final Stage stage) throws IOException {
    final FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/game_view.fxml"));
    final Parent root = loader.load();
    scene = new Scene(root, 900, 700);
    stage.setTitle("Agon - GUI Mode");
    stage.setScene(scene);
    stage.show();
    controller = loader.getController();
    controller.setAgonGui(agonGui);
    this.setupShortcuts();
  }

  /** Refreshes the keyboard shortcuts bound to the current scene. */
  public static void refreshShortcuts() {
    if (scene != null && agonGui != null) {
      scene.getAccelerators().clear();
      new AgonApp().setupShortcuts();
    }
  }

  /**
   * * Binds keyboard shortcuts (defined in the configuration) to their corresponding UI actions.
   */
  void setupShortcuts() {
    if (agonGui == null || agonGui.getConfig() == null || controller == null || scene == null) {
      return;
    }
    final Map<String, String> conf = agonGui.getConfig().getShortcuts();

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
  }

  private void bindShortcut(final String shortcut, final Runnable runnable) {
    if (shortcut != null && !shortcut.isBlank()) {
      try {
        scene.getAccelerators().put(KeyCombination.valueOf(shortcut), runnable);
      } catch (IllegalArgumentException e) {
        System.err.println("[WARNING] Invalid shortcut " + shortcut);
      }
    }
  }

  /**
   * Retrieves the active GameViewController instance.
   *
   * @return The current GameViewController.
   */
  public static GameViewController getController() {
    return controller;
  }

  /**
   * Sets the main AgonGui bridge instance.
   *
   * @param gui The AgonGui instance to link.
   */
  public static void setGui(final AgonGui gui) {
    agonGui = gui;
  }
}
