package fr.univ.bordeaux.ui.gui.controllers;

import fr.univ.bordeaux.ui.gui.AgonGui;
import fr.univ.bordeaux.ui.gui.components.HexagonCanvas;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * The main JavaFX Controller handling interactions on the game board screen.
 * <p>
 * This class binds the visual FXML elements to the logic and sends interactions
 * to the {@link AgonGui} engine wrapper.
 */
public class GameViewController {

    @FXML
    private StackPane boardContainer;

    @FXML
    private Label messageLabel;

    private AgonGui agonGui;

    private HexagonCanvas hexCanvas;

    /**
     * Injects the underlying GUI controller.
     *
     * @param agonGui The AgonGui instance managing the game state.
     */
    public void setAgonGUI(AgonGui agonGui) {
        this.agonGui = agonGui;
    }

    /**
     * Returns the canvas currently managing the board rendering.
     *
     * @return The active HexagonCanvas.
     */
    public HexagonCanvas getHexCanvas() {
        return this.hexCanvas;
    }

    /**
     * Initializes the JavaFX controller.
     * Sets up the canvas, binds dimensions, and links the resizing listener.
     */
    @FXML
    public void initialize() {
        hexCanvas = new HexagonCanvas();
        hexCanvas.setMoveRequestListener(move -> {;
            if (agonGui != null) {
                agonGui.sendCommand(move);
            }
        });
        boardContainer.getChildren().add(hexCanvas);
        hexCanvas.widthProperty().bind(boardContainer.widthProperty());
        hexCanvas.heightProperty().bind(boardContainer.heightProperty());
        hexCanvas.widthProperty().addListener((obs, oldVal, newVal) -> hexCanvas.draw());
        hexCanvas.heightProperty().addListener((obs, oldVal, newVal) -> hexCanvas.draw());
    }

    /**
     * Updates the status message displayed at the top/bottom of the board.
     *
     * @param message The text to display.
     */
    public void updateMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }

    /**
     * Routes a message to the appropriate UI component based on its content.
     * - "Current Player" or ">>" implies a Status bar update.
     * - Everything else is displayed as an Information Popup.
     *
     * @param message The system message.
     */
    public void routeMessage(String message) {
        if (message == null) return;
        if (message.contains("Current Player") || message.contains(">>")) {
            updateMessage(message);
        } else {
            showInfo(message);
        }
    }

    /**
     * Displays an error popup dialogue.
     *
     * @param error The error message to present to the user.
     */
    public void showError(String error) {
        updateMessage("Error : " + error);
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(error);
        alert.showAndWait();
    }

    /**
     * Displays an informational popup dialogue.
     *
     * @param info The information to present.
     */
    public void showInfo(String info) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(info);
        alert.showAndWait();
    }

    /**
     * Displays a warning popup dialogue.
     *
     * @param warning The warning text to present.
     */
    public void showWarn(String warning) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(warning);
        alert.showAndWait();
    }

    /**
     * Builds and displays the "New Game" configuration dialog box.
     *
     * @return An Optional containing the command string to be executed if accepted.
     */
    private Optional<String> showNewGameDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("New Game");
        dialog.setHeaderText("Player configuration for the new game");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        ComboBox<String> p1Type = new ComboBox<>();
        p1Type.getItems().addAll("Human", "AI");
        p1Type.setValue("Human");

        ComboBox<String> p1Color = new ComboBox<>();
        p1Color.getItems().addAll("White", "Black");
        p1Color.setValue("White");

        ComboBox<String> p2Type = new ComboBox<>();
        p2Type.getItems().addAll("Human", "AI");
        p2Type.setValue("AI");

        CheckBox blitzCheck = new CheckBox("Enable Blitz mode");
        Spinner<Integer> timeSpinner = new Spinner<>(1, 60, 5);
        timeSpinner.setDisable(true);

        blitzCheck.setOnAction(e -> timeSpinner.setDisable(!blitzCheck.isSelected()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("PLayer 1 :"), 0, 0);
        grid.add(p1Type, 1, 0);
        grid.add(new Label("PLayer 1 Color:"), 0, 1);
        grid.add(p1Color, 1, 1);

        grid.add(new Label("PLayer 2 :"), 0, 2);
        grid.add(p2Type, 1, 2);

        grid.add(blitzCheck, 0, 3, 2, 1);
        grid.add(new Label("Time (minutes) :"), 0, 4);
        grid.add(timeSpinner, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                StringBuilder cmd = new StringBuilder("new");

                boolean p1IsAi = p1Type.getValue().equals("AI");
                boolean p2IsAi = p2Type.getValue().equals("AI");
                String color = p1Color.getValue().toLowerCase();

                cmd.append(" -p1Ia ").append(p1IsAi);
                cmd.append(" -p2Ia ").append(p2IsAi);
                cmd.append(" -p1Color ").append(color);

                if (blitzCheck.isSelected()) {
                    cmd.append(" -b true");
                    cmd.append(" -t ").append(timeSpinner.getValue());
                } else {
                    cmd.append(" -b false");
                }

                return cmd.toString();
            }
            return null;
        });

        return dialog.showAndWait();
    }

    @FXML
    public void startNewGame() {
        if (agonGui == null) return;
        Optional<String> result = showNewGameDialog();
        result.ifPresent(command -> agonGui.sendCommand(command));
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
        if (agonGui != null) {
            agonGui.sendCommand("save");
        }
    }

    @FXML
    public void loadGame() {
        if (agonGui != null) {
            agonGui.sendCommand("load");
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
        try {
            java.io.InputStream in = getClass().getResourceAsStream("/cmdsInformations/agonGuiMenu.txt");

            if (in != null) {
                String helpText = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                showInfo(helpText);
            } else {
                showInfo("The help file agonGuiMenu.txt could not be found.");
            }
        } catch (Exception e) {
            showError("Error reading the help file:" + e.getMessage());
        }
    }

    @FXML
    public void showConfig() {
        if (agonGui != null && agonGui.getConfig() != null) {
            String configText = agonGui.getConfig().toString();
            showInfo("Current configuration:\n\n" + configText);
        } else {
            showInfo("The configuration is not loaded yet.");
        }
    }

    @FXML
    public void showVersion() {
        showInfo("Agon Game - GUI Version");
    }
}