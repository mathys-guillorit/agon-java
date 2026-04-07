package fr.univ.bordeaux.ui.gui.controllers;

import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.application.AppMode;
import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.ui.gui.AgonApp;
import fr.univ.bordeaux.ui.gui.AgonGui;
import fr.univ.bordeaux.ui.gui.components.HexagonCanvas;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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
    Platform.runLater(this::promptSessionSetup);
    setupConsoleInterceptor();
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
      if (agonGui != null && agonGui.getAppContext() != null && agonGui.getAppContext().isOnlineGameActive()) {
        boolean isMyTurn = agonGui.getAppContext().isMyOnlineTurn();
        String myColor = agonGui.getAppContext().getLocalOnlineColor().toString();

        String customMsg = isMyTurn
                ? "Your turn! (You are " + myColor + ")"
                : "Opponent's turn (You are " + myColor + ")";

        updateMessage(customMsg);
      } else {
        updateMessage(message);
      }
    } else if (lowerMsg.contains("invitation_received")) {
      promptInvitation(message);
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

  private void promptInvitation(String message) {
    String challenger = "A player";
    try {
      String[] parts = message.split("FROM=");
      if (parts.length > 1) {
        challenger = parts[1].split(" ")[0];
      }
    } catch (Exception ignored) {}

    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Incoming Challenge!");
    alert.setHeaderText(challenger + " has challenged you!");
    alert.setContentText("Do you want to accept this match?");

    ButtonType btnAccept = new ButtonType("Accept", ButtonBar.ButtonData.YES);
    ButtonType btnDecline = new ButtonType("Decline", ButtonBar.ButtonData.NO);
    alert.getButtonTypes().setAll(btnAccept, btnDecline);

    Platform.runLater(() -> {
      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == btnAccept) {
        if (agonGui != null) agonGui.sendCommand("accept");
      } else {
        if (agonGui != null) agonGui.sendCommand("decline");
      }
    });
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

  private void promptSessionSetup() {
    if (agonGui == null || agonGui.getAppContext() == null) return;

    String playerName = "Player";
    try {
      playerName = agonGui.getAppContext().getProfile().getName();
    } catch (Exception ignored) {
      System.err.println("Could not retrieve player name.");
    }

    updateMessage("Logged in as: " + playerName);

    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("Welcome to Agon");

    dialog.setHeaderText("Hello " + playerName + "!\nChoose your game mode for this session:");

    ButtonType localBtn = new ButtonType("Local (Play offline)", ButtonBar.ButtonData.YES);
    ButtonType onlineBtn = new ButtonType("Online (Multiplayer)", ButtonBar.ButtonData.NO);
    dialog.getDialogPane().getButtonTypes().addAll(localBtn, onlineBtn);

    dialog.setResultConverter(btn -> btn == onlineBtn ? "ONLINE" : "LOCAL");

    dialog.showAndWait().ifPresent(choice -> {
      if (choice.equals("ONLINE")) {
        agonGui.getAppContext().setMode(AppMode.ONLINE);
        showServerBrowser();
      } else {
        agonGui.getAppContext().setMode(AppMode.LOCAL);
      }
    });
  }

  @FXML
  public void showServerBrowser() {
    if (agonGui == null || agonGui.getAppContext() == null) return;

    if (agonGui.getAppContext().getMode() == AppMode.LOCAL) {
      showWarn("This menu is restricted to Online mode.\nPlease restart the game to play online.");
      return;
    }

    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("Server Browser");
    dialog.setHeaderText("Select a game server on your network to join");

    ButtonType joinButtonType = new ButtonType("Join", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(joinButtonType, ButtonType.CANCEL);

    ListView<String> serverList = new ListView<>();
    TextField manualIpField = new TextField("localhost:12345");
    manualIpField.setPromptText("Enter IP:PORT...");

    try {
      var discovery = agonGui.getAppContext().getDiscovery();
      var servers = discovery.getServers();
      if (servers.isEmpty()) {
        serverList.getItems().add("No local servers found...");
      } else {
        for (var s : servers) {
          serverList.getItems().add(s.name + " @ " + s.ip + ":" + s.tcpPort);
        }
      }
    } catch (Exception e) {
      serverList.getItems().add("Discovery service offline.");
    }

    serverList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null && newVal.contains("@")) {
        manualIpField.setText(newVal.split("@")[1].trim());
      }
    });

    javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10, new Label("Discovered servers:"), serverList, new Label("Direct IP:"), manualIpField);
    dialog.getDialogPane().setContent(vbox);

    dialog.setResultConverter(btn -> btn == joinButtonType ? manualIpField.getText().trim() : null);

    dialog.showAndWait().ifPresent(ipAndPort -> {
      if (!ipAndPort.isBlank()) agonGui.sendCommand("join " + ipAndPort);
    });
  }

  @FXML
  public void showHostServerDialog() {
    if (agonGui == null || agonGui.getAppContext() == null) return;

    if (agonGui.getAppContext().getMode() == AppMode.LOCAL) {
      showWarn("This menu is restricted to Online mode.\nPlease restart the game to play online.");
      return;
    }

    var server = agonGui.getAppContext().getServer();
    boolean isRunning = server != null && server.isRunning();

    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("Host Game Server");

    if (isRunning) {
      dialog.setHeaderText("Server is already running on port " + server.getPort() + "\nConnected clients: " + server.getConnectedClientsCount());
      ButtonType stopBtn = new ButtonType("Stop Server", ButtonBar.ButtonData.OK_DONE);
      dialog.getDialogPane().getButtonTypes().addAll(stopBtn, ButtonType.CANCEL);

      dialog.setResultConverter(btn -> btn == stopBtn ? "STOP" : null);
    } else {
      dialog.setHeaderText("Start a new local game server");
      ButtonType startBtn = new ButtonType("Start", ButtonBar.ButtonData.OK_DONE);
      dialog.getDialogPane().getButtonTypes().addAll(startBtn, ButtonType.CANCEL);

      TextField portField = new TextField("12345");
      javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10, new Label("TCP Port (default 12345):"), portField);
      dialog.getDialogPane().setContent(vbox);

      dialog.setResultConverter(btn -> btn == startBtn ? portField.getText().trim() : null);
    }

    dialog.showAndWait().ifPresent(result -> {
      if (result.equals("STOP")) {
        agonGui.sendCommand("server_stop");
      } else {
        agonGui.sendCommand("server_start " + result);
        agonGui.sendCommand("join localhost:" + result);
      }
    });
  }

  @FXML
  public void showLobby() {
    if (agonGui == null || agonGui.getAppContext() == null) return;

    if (agonGui.getAppContext().getMode() == AppMode.LOCAL) {
      showWarn("This menu is restricted to Online mode.\nPlease restart the game to play online.");
      return;
    }

    var client = agonGui.getAppContext().getClient();
    if (client == null || !client.isConnected()) {
      showWarn("You must be connected to a server to open the lobby!");
      return;
    }

    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("Multiplayer Lobby");
    dialog.setHeaderText("See who is online and challenge them!");

    ButtonType challengeBtn = new ButtonType("Challenge Player", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(challengeBtn, ButtonType.CLOSE);

    TextArea infoArea = new TextArea();
    infoArea.setEditable(false);
    infoArea.setPrefRowCount(12);
    infoArea.setPrefColumnCount(40);
    infoArea.setStyle("-fx-font-family: monospace;");

    String initialPlayers = client.requestPlayers();
    infoArea.setText(initialPlayers != null ? initialPlayers : "Failed to load players.");

    Button refreshBtn = new Button("Refresh Players");
    refreshBtn.setOnAction(e -> infoArea.setText(client.requestPlayers()));

    Button scoreBtn = new Button("View Scoreboard");
    scoreBtn.setOnAction(e -> infoArea.setText(client.requestScoreboard()));

    javafx.scene.layout.HBox topButtons = new javafx.scene.layout.HBox(10, refreshBtn, scoreBtn);

    TextField playerIdField = new TextField();
    playerIdField.setPromptText("Ex: 2");
    javafx.scene.layout.HBox challengeBox = new javafx.scene.layout.HBox(10, new Label("Target Player ID:"), playerIdField);

    javafx.scene.layout.VBox layout = new javafx.scene.layout.VBox(10, topButtons, infoArea, challengeBox);
    dialog.getDialogPane().setContent(layout);

    dialog.setResultConverter(btn -> {
      if (btn == challengeBtn) {
        String targetId = playerIdField.getText().trim();
        if (!targetId.isEmpty()) return "new " + targetId;
      }
      return null;
    });

    dialog.showAndWait().ifPresent(cmd -> agonGui.sendCommand(cmd));
  }

  private void refreshBoardFromNetwork() {
    if (agonGui == null || agonGui.getAppContext() == null) return;

    Match match = agonGui.getAppContext().getCurrentOnlineMatch();

    if (match != null) {
      try {
        RestrictedAgonBoard board = match.getAgonBoard();

        Map<String, PieceType> snapshot = hexCanvas.takeSnapshot(board);
        hexCanvas.applySnapshot(board, snapshot);
      } catch (Exception e) {
        System.err.println("Impossible de rafraîchir le plateau : " + e.getMessage());
      }
    }
  }

  private void setupConsoleInterceptor() {
    java.io.PrintStream originalOut = System.out;

    System.setOut(new java.io.PrintStream(originalOut) {
      @Override
      public void println(String x) {
        super.println(x);

        if (x != null) {
          if (x.contains("INVITATION_RECEIVED")) {
            Platform.runLater(() -> promptInvitation(x));
          } else if (x.contains("CHOOSE_MODE")) {
            Platform.runLater(() -> promptGameMode());
          } else if (x.contains("GAME_STARTED GAME_ID")) {
            Platform.runLater(GameViewController.this::refreshBoardFromNetwork);
          } else if (x.contains("Your turn") || x.contains("Opponent turn") || x.contains("You are")) {
            String cleanText = x.replace("[ONLINE]", "").trim();
            Platform.runLater(() -> {
              updateMessage(cleanText);
              refreshBoardFromNetwork();
            });
          }
        }
      }
    });
  }

  private void promptGameMode() {
    Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Select Game Mode");
    alert.setHeaderText("The match is ready!");
    alert.setContentText("Do you want to play a Normal game or a Blitz game?");

    ButtonType btnNormal = new ButtonType("Normal", ButtonBar.ButtonData.YES);
    ButtonType btnBlitz = new ButtonType("Blitz", ButtonBar.ButtonData.NO);
    alert.getButtonTypes().setAll(btnNormal, btnBlitz);

    java.util.Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == btnNormal) {
      if (agonGui != null) agonGui.sendCommand("mode normal");
    } else {
      if (agonGui != null) agonGui.sendCommand("mode blitz");
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
