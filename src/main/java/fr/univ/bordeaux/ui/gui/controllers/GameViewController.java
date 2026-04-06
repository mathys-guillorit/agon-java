package fr.univ.bordeaux.ui.gui.controllers;

import fr.univ.bordeaux.ui.gui.AgonApp;
import fr.univ.bordeaux.ui.gui.AgonGui;
import fr.univ.bordeaux.ui.gui.components.HexagonCanvas;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

/**
 * The main JavaFX Controller handling interactions on the game board screen.
 *
 * <p>This class binds the visual FXML elements to the logic and sends interactions to the {@link
 * AgonGui} engine wrapper.
 */
public class GameViewController {

  @FXML private StackPane boardContainer;

  @FXML private Label messageLabel;

  private AgonGui agonGui;

  private HexagonCanvas hexCanvas;

  /**
   * Injects the underlying GUI controller.
   *
   * @param agonGui The AgonGui instance managing the game state.
   */
  public void setAgonGui(AgonGui agonGui) {
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
   * Initializes the JavaFX controller. Sets up the canvas, binds dimensions, and links the resizing
   * listener.
   */
  @FXML
  public void initialize() {
    hexCanvas = new HexagonCanvas();
    hexCanvas.setMoveRequestListener(
        move -> {
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
   * Routes a message to the appropriate UI component based on its content. - "Current Player" or
   * ">>" implies a Status bar update. - Everything else is displayed as an Information Popup.
   *
   * @param message The system message.
   */
  public void routeMessage(String message) {
    if (message == null) return;
    String lowerMsg = message.toLowerCase();
    if (lowerMsg.contains("current player") || lowerMsg.contains(">>")) {
      updateMessage(message);
    } else if (lowerMsg.contains("save the game before quitting")) {
      promptYesNo("save the game before quitting ?");
    } else if (lowerMsg.contains("filename") || lowerMsg.contains("nom du fichier")) {
      promptFilename("Enter the name of the save file :");
    } else {
      showInfo(message);
    }
  }

  private void promptYesNo(String msg) {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Quit the Game");
    alert.setHeaderText(null);
    alert.setContentText(msg);

    ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
    ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
    alert.getButtonTypes().setAll(buttonYes, buttonNo);

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == buttonYes) {
      if (agonGui != null) agonGui.sendCommand("y");
    } else {
      if (agonGui != null) agonGui.sendCommand("n");
    }
  }

  private void promptFilename(String msg) {
    TextInputDialog dialog = new TextInputDialog("");
    dialog.setTitle("Save");
    dialog.setHeaderText(null);
    dialog.setContentText(msg);

    Optional<String> result = dialog.showAndWait();
    if (result.isPresent() && !result.get().trim().isEmpty()) {
      if (agonGui != null) agonGui.sendCommand(result.get().trim());
    } else {
      if (agonGui != null) agonGui.sendCommand("default_save");
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

    dialog.setResultConverter(
        dialogButton -> {
          if (dialogButton == createButtonType) {
            StringBuilder cmd = new StringBuilder("new");

            String p1ColorStr = p1Color.getValue().toLowerCase();
            String p2ColorStr = p1ColorStr.equals("white") ? "black" : "white";

            if (p1Type.getValue().equals("AI")) {
              cmd.append(" --ai ").append(p1ColorStr);
            }
            if (p2Type.getValue().equals("AI")) {
              cmd.append(" --ai ").append(p2ColorStr);
            }

            cmd.append(" --player1Color ").append(p1ColorStr);

            if (blitzCheck.isSelected()) {
              cmd.append(" --blitz --time ").append(timeSpinner.getValue());
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
      TextInputDialog dialog = new TextInputDialog("");
      dialog.setTitle("Save");
      dialog.setHeaderText(null);
      dialog.setContentText("Enter the name of the game you want to save :");

      Optional<String> result = dialog.showAndWait();
      if (result.isPresent()) {
        String filename = result.get().trim();
        if (filename.isEmpty()) {
          filename = "default_save";
        }
        agonGui.sendCommand("save " + filename);
      }
    }
  }

  @FXML
  public void loadGame() {
    if (agonGui != null) {
      TextInputDialog dialog = new TextInputDialog("");
      dialog.setTitle("Load a Game");
      dialog.setHeaderText(null);
      dialog.setContentText("Enter the name of the game you want to load :");

      Optional<String> result = dialog.showAndWait();
      if (result.isPresent() && !result.get().trim().isEmpty()) {
        agonGui.sendCommand("load " + result.get().trim());
      }
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
    if (agonGui != null) {
      agonGui.sendCommand("show -configuration");
    }
  }

  @FXML
  public void showVersion() {
    showInfo("Agon Game - GUI Version");
  }

  @FXML
  public void showHistory() {
    if (agonGui != null) {
      agonGui.sendCommand("show -history");
    }
  }
/*
  @FXML
    public void editShortcuts() {
      if (agonGui == null) return;
      Dialog<Map<String, String>> dialog = new Dialog<>();
      dialog.setTitle("Keyboard Shortcuts");
      dialog.setHeaderText("Modify your shortcuts (ex: Ctrl+N, Alt+N)");

      ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
      dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));

      Map<String, String> currentShortcuts = agonGui.getConfig().getShortcuts();
      Map<String, TextField> fields = new java.util.HashMap<>();
      int row = 0;
      for (String key : currentShortcuts.keySet()) {
        grid.add(new Label(key.replace("shortcut_", "") + " :"), 0, row);
        TextField tf = new TextField(currentShortcuts.get(key));
        grid.add(tf, 1, row);
        fields.put(key, tf);
        row++;
        }
      dialog.getDialogPane().setContent(grid);

      dialog.setResultConverter(dialogButton -> {
        if (dialogButton == saveButtonType) {
            Map<String, String> newShortcuts = new HashMap<>();
            fields.forEach((key, tf) -> newShortcuts.put(key, tf.getText().trim()));
            return newShortcuts;
        }
        return null;
      });
      Optional<Map<String, String>> result = dialog.showAndWait();
      result.ifPresent(newShortcuts -> {
          newShortcuts.forEach((key, tf) -> {
              agonGui.getConfig().saveShortcuts(key, tf);
          });
          currentShortcuts.putAll(newShortcuts);
          AgonApp.refreshShortcuts();
          showInfo("Shortcuts updated successfully !");
      });
  }
*/
}
