package fr.univ.bordeaux.ui.gui;

import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.ui.gui.controllers.GameViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AgonApp extends Application {
    private static GameViewController controller;
    private static Scene scene;
    private static AgonBoardImpl agonBoard;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/game_view.fxml"));
        Parent root = loader.load();
        scene = new Scene(root, 900, 700);
        stage.setTitle("Agon - GUI Mode");
        stage.setScene(scene);
        stage.show();
        // temporarily
        AgonApp.controller = loader.getController();
        AgonGui gui = new AgonGui();
        AgonApp.controller.setAgonGUI(gui);
        this.setupShortcuts();
    }


    // shortcuts

    /**
     * Add shortcuts to GUI.
     */
    private void setupShortcuts(){
        Map<KeyCombination, Runnable> shortcuts = new HashMap<>();
        shortcuts.put(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                AgonApp.controller::startNewGame
        );
        shortcuts.put(
                new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN),
                AgonApp.controller::loadGame
        );
        shortcuts.put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                AgonApp.controller::saveGame
        );
        shortcuts.put(
                new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN),
                AgonApp.controller::showConfig
        );
        shortcuts.put(
                new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN),
                AgonApp.controller::showVersion
        );
        shortcuts.put(
                new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN),
                AgonApp.controller::quitGame
        );
        shortcuts.put(
                new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN),
                AgonApp.controller::undo
        );
        shortcuts.put(
                new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
                AgonApp.controller::redo
        );
        shortcuts.put(
                new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN),
                AgonApp.controller::pauseGame
        );
        shortcuts.put(
                new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN),
                AgonApp.controller::requestHint
        );
        shortcuts.forEach((keyCombination, runnable) -> {
            scene.getAccelerators().put(keyCombination, runnable);
        });
    }


    ///  getters and setters

    public static void setBoard(AgonBoardImpl board) {
        agonBoard = board;
    }

    public static GameViewController getController() {
        return controller;
    }



}
