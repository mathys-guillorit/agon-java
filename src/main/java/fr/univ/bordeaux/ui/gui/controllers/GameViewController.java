package fr.univ.bordeaux.ui.gui.controllers;

import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.ui.gui.AgonGui;
import fr.univ.bordeaux.ui.gui.components.HexagonCanvas;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import java.io.File;

public class GameViewController {

    @FXML
    private StackPane boardContainer;

    @FXML
    private Label messageLabel;

    private AgonGui agonGui;

    private HexagonCanvas hexCanvas;

    public void setAgonGUI(AgonGui agonGui) {
        this.agonGui = agonGui;
    }

    @FXML
    public void initialize() {
        hexCanvas = new HexagonCanvas();
        boardContainer.getChildren().add(hexCanvas);
        hexCanvas.widthProperty().bind(boardContainer.widthProperty());
        hexCanvas.heightProperty().bind(boardContainer.heightProperty());
        hexCanvas.widthProperty().addListener((obs, oldVal, newVal) -> hexCanvas.draw());
        hexCanvas.heightProperty().addListener((obs, oldVal, newVal) -> hexCanvas.draw());
    }

    public void updateMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }

    public void refreshBoard(RestrictedAgonBoard agonBoard) {
        System.out.println("New board received");
        if (hexCanvas != null) {
            hexCanvas.updateBoard(agonBoard);
        }
    }

    public void showError(String error) {
        updateMessage("Error : " + error);
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(error);
        alert.showAndWait();
    }

    public void showInfo(String info) {
        updateMessage(info);
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(info);
        alert.showAndWait();
    }

    public void showWarn(String warning) {
        updateMessage("Warning: " + warning);
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(warning);
        alert.showAndWait();
    }

    @FXML
    public void startNewGame() {
        if (agonGui != null) agonGui.sendCommand("new");
    }

    @FXML
    public void undo() {
        if (agonGui != null) agonGui.sendCommand("undo");
    }

    @FXML
    public void redo() {
        if (agonGui != null) agonGui.sendCommand("redo");
    }

    @FXML
    public void saveGame() {
        if (agonGui == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Game");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Agon Files", "*.agon"));

        File file = fileChooser.showSaveDialog(boardContainer.getScene().getWindow());
        if (file != null) {
            agonGui.sendCommand("save " + file.getAbsolutePath());
        }
    }

    @FXML
    public void loadGame() {
        if (agonGui == null) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load game");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Agon Files", "*.agon"));

        File file = fileChooser.showOpenDialog(boardContainer.getScene().getWindow());
        if (file != null) {
            agonGui.sendCommand("load " + file.getAbsolutePath());
        }
    }

    @FXML
    public void pauseGame() {
        if (agonGui != null) agonGui.sendCommand("pause");
    }

    @FXML
    public void quitGame() {
        if (agonGui != null) agonGui.sendCommand("quit");
    }

    @FXML
    public void requestHint() {
        if (agonGui != null) agonGui.sendCommand("hint");
    }

    @FXML
    public void showHelp() {
        if (agonGui != null) agonGui.sendCommand("help");
    }

    @FXML
    public void showConfig() {
        showInfo("");
    }

    @FXML
    public void showVersion() {
        showInfo("Agon Game - GUI Version");
    }
}