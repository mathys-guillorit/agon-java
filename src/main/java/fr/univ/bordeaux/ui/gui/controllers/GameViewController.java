package fr.univ.bordeaux.ui.gui.controllers;

import fr.univ.bordeaux.application.AppMode;
import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.client.ClientDiscovery;
import fr.univ.bordeaux.application.network.client.ServerInfo;
import fr.univ.bordeaux.application.network.client.runtime.AsyncEventLogger;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.technical.io.config.ConfigSerializer;
import fr.univ.bordeaux.technical.utils.GameLogger;
import fr.univ.bordeaux.ui.gui.AgonApp;
import fr.univ.bordeaux.ui.gui.AgonGui;
import fr.univ.bordeaux.ui.gui.components.HexagonCanvas;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * The main JavaFX Controller for the Agon game interface. Handles all UI events, dialog prompts,
 * network lobbies, and routes commands to the core game engine via the {@link AgonGui} bridge.
 */
public class GameViewController {

  /** The UI stack pane container holding the hexagon canvas. */
  @FXML private StackPane boardContainer;

  /** The UI label used to display game status and server messages. */
  @FXML private Label messageLabel;

  /** The main GUI bridge connecting the interface to the game engine. */
  private AgonGui agonGui;

  /** The custom canvas component responsible for rendering the game board. */
  private HexagonCanvas hexCanvas;

  /**
   * Default constructor for the GameViewController. Instantiated automatically by JavaFX when
   * loading the FXML.
   */
  public GameViewController() {}

  /**
   * Injects the AgonGui bridge into the controller and triggers initial session setups.
   *
   * @param agonGui The core GUI bridge instance.
   */
  public void setAgonGui(final AgonGui agonGui) {
    this.agonGui = agonGui;
    Platform.runLater(this::promptSessionSetup);
    setupNetworkListener();
  }

  /**
   * Retrieves the hexagon canvas component.
   *
   * @return The HexagonCanvas instance used for rendering the board.
   */
  public HexagonCanvas getHexCanvas() {
    return this.hexCanvas;
  }

  /**
   * Initializes the JavaFX components after the FXML has been loaded. Sets up the HexagonCanvas and
   * binds its dimensions to the parent container.
   */
  @FXML
  public void initialize() {
    hexCanvas = new HexagonCanvas();
    hexCanvas.setMoveRequestListener(
        move -> {
          if (agonGui != null) {
            if (agonGui.getPaused()) {
              showWarn("The game is currently paused. Please unpause to make a move.");
            } else {
              agonGui.sendCommand(move);
            }
          }
        });
    boardContainer.getChildren().add(hexCanvas);
    hexCanvas.widthProperty().bind(boardContainer.widthProperty());
    hexCanvas.heightProperty().bind(boardContainer.heightProperty());
    hexCanvas.widthProperty().addListener((obs, oldVal, newVal) -> hexCanvas.draw());
    hexCanvas.heightProperty().addListener((obs, oldVal, newVal) -> hexCanvas.draw());
  }

  /**
   * Updates the primary message label on the UI.
   *
   * @param message The text to display.
   */
  public void updateMessage(final String message) {
    if (messageLabel != null) {
      messageLabel.setText(message);
    }
  }

  /**
   * Parses incoming engine messages and routes them to the appropriate UI component (e.g., updating
   * the status label, prompting a dialog, or showing a notification).
   *
   * @param message The raw message string from the game engine.
   */
  public void routeMessage(final String message) {
    if (message != null) {
      final String lowerMsg = message.toLowerCase();

      if (lowerMsg.contains("game paused")
          || lowerMsg.contains("current player")
          || lowerMsg.contains(">>")) {
        handleTurnAndPauseMessages(message, lowerMsg);
      } else if (lowerMsg.contains("invitation_received")
          || lowerMsg.contains("save the game")
          || lowerMsg.contains("filename")) {
        handleDialogPrompts(message, lowerMsg);
      } else {
        showInfo(message);
      }
    }
  }

  private void handleTurnAndPauseMessages(final String originalMsg, final String lowerMsg) {
    if (lowerMsg.contains("game paused")) {
      updateMessage(originalMsg);
    } else if (agonGui != null && agonGui.getAppContext().isOnlineGameActive()) {
      final boolean isMyTurn = agonGui.getAppContext().isMyOnlineTurn();
      final String myColor = agonGui.getAppContext().getLocalOnlineColor().toString();
      final String oppColor = myColor.equals("WHITE") ? "BLACK" : "WHITE";

      String customMsg =
          isMyTurn ? "Your turn! (You are " + myColor + ")" : "Opponent's turn (" + oppColor + ")";

      if (originalMsg.contains("Time left:")) {
        final String timePart = originalMsg.substring(originalMsg.indexOf("Time left:"));
        customMsg += "   |   " + timePart;
      }

      updateMessage(customMsg);
    } else {
      updateMessage(originalMsg);
    }
  }

  private void handleDialogPrompts(final String originalMsg, final String lowerMsg) {
    if (lowerMsg.contains("invitation_received")) {
      promptInvitation(originalMsg);
    } else if (lowerMsg.contains("save the game before quitting")) {
      promptYesNo("Save the game before quitting ?");
    } else if (lowerMsg.contains("filename")) {
      promptFilename("Enter the name of the save file :");
    }
  }

  private void promptYesNo(final String msg) {
    final Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Quit the Game");
    alert.setHeaderText(null);
    alert.setContentText(msg);

    final ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
    final ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
    alert.getButtonTypes().setAll(buttonYes, buttonNo);

    final Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == buttonYes) {
      if (agonGui != null) {
        agonGui.sendCommand("y");
      }
    } else {
      if (agonGui != null) {
        agonGui.sendCommand("n");
      }
    }
  }

  private void promptInvitation(final String message) {
    String challenger = "A player";
    try {
      final String[] parts = message.split("FROM=");
      if (parts.length > 1) {
        challenger = parts[1].split(" ")[0];
      }
    } catch (Exception ignored) {
    }

    final Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Incoming Challenge!");
    alert.setHeaderText(challenger + " has challenged you!");
    alert.setContentText("Do you want to accept this match?");

    final ButtonType btnAccept = new ButtonType("Accept", ButtonBar.ButtonData.YES);
    final ButtonType btnDecline = new ButtonType("Decline", ButtonBar.ButtonData.NO);
    alert.getButtonTypes().setAll(btnAccept, btnDecline);

    Platform.runLater(
        () -> {
          final Optional<ButtonType> result = alert.showAndWait();
          if (result.isPresent() && result.get().equals(btnAccept)) {
            if (agonGui != null) {
              agonGui.sendCommand("accept");
            }
          } else {
            if (agonGui != null) {
              agonGui.sendCommand("decline");
            }
          }
        });
  }

  /**
   * Prompts the user with a text input dialog to enter a filename.
   *
   * @param message The instruction message displayed in the dialog.
   */
  private void promptFilename(final String message) {
    final TextInputDialog dialog = new TextInputDialog("");
    dialog.setTitle("Input Required");
    dialog.setHeaderText(null);
    dialog.setContentText(message);

    final Optional<String> result = dialog.showAndWait();
    if (result.isPresent() && !result.get().trim().isEmpty()) {
      if (agonGui != null) {
        agonGui.sendCommand(result.get().trim());
      }
    }
  }

  /**
   * Displays an error dialog to the user.
   *
   * @param error The error message to display.
   */
  public void showError(final String error) {
    updateMessage("Error : " + error);
    final Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(null);
    alert.setContentText(error);
    alert.showAndWait();
  }

  /**
   * Displays an informational dialog to the user.
   *
   * @param info The information message to display.
   */
  public void showInfo(final String info) {
    final Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("Information");
    alert.setHeaderText(null);
    alert.setContentText(info);
    alert.showAndWait();
  }

  /**
   * Displays a warning dialog to the user.
   *
   * @param warning The warning message to display.
   */
  public void showWarn(final String warning) {
    final Alert alert = new Alert(AlertType.WARNING);
    alert.setTitle("Warning");
    alert.setHeaderText(null);
    alert.setContentText(warning);
    alert.showAndWait();
  }

  private Optional<String> showNewGameDialog() {
    final Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("New Game");
    dialog.setHeaderText("Player configuration for the new game");

    final ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

    final ComboBox<String> p1Type = new ComboBox<>();
    p1Type.getItems().addAll("Human", "AI");
    p1Type.setValue("Human");

    final ComboBox<String> p1Color = new ComboBox<>();
    p1Color.getItems().addAll("White", "Black");
    p1Color.setValue("White");

    final ComboBox<String> p2Type = new ComboBox<>();
    p2Type.getItems().addAll("Human", "AI");
    p2Type.setValue("AI");

    final ComboBox<String> aiMode = new ComboBox<>();
    aiMode.getItems().addAll("Random", "Minimax", "MCTS");
    aiMode.setValue("Minimax");

    final ComboBox<String> aiMinimaxScoring = new ComboBox<>();
    aiMinimaxScoring.getItems().addAll("Mobility", "Centrality", "Mixed");
    aiMinimaxScoring.setValue("Mobility");

    final Spinner<Integer> aiDepth = new Spinner<>(1, 10, 3);

    final ComboBox<String> aiMctsSelection = new ComboBox<>();
    aiMctsSelection.getItems().addAll("UCT", "ML");
    aiMctsSelection.setValue("UCT");

    final HBox scoringBox = new HBox(10, new Label("Heuristic:"), aiMinimaxScoring);
    final HBox depthBox = new HBox(10, new Label("Depth:"), aiDepth);
    final HBox mctsBox = new HBox(10, new Label("Selection:"), aiMctsSelection);

    final VBox aiSettingsBox = new VBox(10);
    aiSettingsBox.setPadding(new Insets(10));
    aiSettingsBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5;");
    aiSettingsBox
        .getChildren()
        .addAll(
            new Label("--- AI Configuration ---"),
            new HBox(10, new Label("Strategy:"), aiMode),
            scoringBox,
            depthBox,
            mctsBox);

    aiSettingsBox
        .visibleProperty()
        .bind(p1Type.valueProperty().isEqualTo("AI").or(p2Type.valueProperty().isEqualTo("AI")));
    aiSettingsBox.managedProperty().bind(aiSettingsBox.visibleProperty());

    scoringBox.visibleProperty().bind(aiMode.valueProperty().isEqualTo("Minimax"));
    scoringBox.managedProperty().bind(scoringBox.visibleProperty());
    depthBox.visibleProperty().bind(aiMode.valueProperty().isEqualTo("Minimax"));
    depthBox.managedProperty().bind(depthBox.visibleProperty());

    mctsBox.visibleProperty().bind(aiMode.valueProperty().isEqualTo("MCTS"));
    mctsBox.managedProperty().bind(mctsBox.visibleProperty());

    final CheckBox blitzCheck = new CheckBox("Enable Blitz mode");
    final Spinner<Integer> timeSpinner = new Spinner<>(1, 60, 5);
    timeSpinner.setDisable(true);
    blitzCheck.setOnAction(e -> timeSpinner.setDisable(!blitzCheck.isSelected()));

    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    grid.add(new Label("PLayer 1 :"), 0, 0);
    grid.add(p1Type, 1, 0);
    grid.add(new Label("PLayer 1 Color:"), 0, 1);
    grid.add(p1Color, 1, 1);

    grid.add(new Label("PLayer 2 :"), 0, 2);
    grid.add(p2Type, 1, 2);

    grid.add(aiSettingsBox, 0, 3, 2, 1);
    grid.add(blitzCheck, 0, 4, 2, 1);
    grid.add(new Label("Time (minutes) :"), 0, 5);
    grid.add(timeSpinner, 1, 5);

    dialog.getDialogPane().setContent(grid);

    dialog.setResultConverter(
        dialogButton -> {
          String res = null;
          if (dialogButton == createButtonType) {
            final StringBuilder cmd = new StringBuilder("new");
            final String p1ColorStr = p1Color.getValue().toLowerCase();
            final String p2ColorStr = "white".equals(p1ColorStr) ? "black" : "white";

            boolean p1IsAi = "AI".equals(p1Type.getValue());
            boolean p2IsAi = "AI".equals(p2Type.getValue());

            if (p1IsAi && p2IsAi) {
              cmd.append(" --ai a");
            } else if (p1IsAi) {
              cmd.append(" --ai ").append(p1ColorStr);
            } else if (p2IsAi) {
              cmd.append(" --ai ").append(p2ColorStr);
            }

            if (p1IsAi || p2IsAi) {
              cmd.append(" --ai-mode ").append(aiMode.getValue().toLowerCase());
              if ("Minimax".equals(aiMode.getValue())) {
                cmd.append(" --ai-minimax-scoring ")
                    .append(aiMinimaxScoring.getValue().toLowerCase());
                cmd.append(" --ai-minimax-depth ").append(aiDepth.getValue());
              } else if ("MCTS".equals(aiMode.getValue())) {
                cmd.append(" --ai-mcts-selection ")
                    .append(aiMctsSelection.getValue().toLowerCase());
              }
            }

            if (blitzCheck.isSelected()) {
              cmd.append(" --blitz --time ").append(timeSpinner.getValue());
            }
            res = cmd.toString();
          }
          return res;
        });

    return dialog.showAndWait();
  }

  /** Opens the dialog to configure and start a new game. */
  @FXML
  public void startNewGame() {
    if (agonGui != null) {
      final Optional<String> result = showNewGameDialog();
      result.ifPresent(command -> agonGui.sendCommand(command));
    }
  }

  /** Sends an undo command to the game engine. */
  @FXML
  public void undo() {
    if (agonGui != null) {
      agonGui.sendCommand("undo");
    }
  }

  /** Sends a redo command to the game engine. */
  @FXML
  public void redo() {
    if (agonGui != null) {
      agonGui.sendCommand("redo");
    }
  }

  /** Opens a dialog to save the current game state. */
  @FXML
  public void saveGame() {
    if (agonGui != null) {
      final TextInputDialog dialog = new TextInputDialog("");
      dialog.setTitle("Save");
      dialog.setHeaderText(null);
      dialog.setContentText("Enter the name of the game you want to save :");

      final Optional<String> result = dialog.showAndWait();
      if (result.isPresent()) {
        String filename = result.get().trim();
        if (filename.isEmpty()) {
          filename = "default_save";
        }
        agonGui.sendCommand("save " + filename);
      }
    }
  }

  /** Opens a dialog to load a previously saved game. */
  @FXML
  public void loadGame() {
    if (agonGui != null) {
      final TextInputDialog dialog = new TextInputDialog("");
      dialog.setTitle("Load a Game");
      dialog.setHeaderText(null);
      dialog.setContentText("Enter the name of the game you want to load :");

      final Optional<String> result = dialog.showAndWait();
      if (result.isPresent() && !result.get().trim().isEmpty()) {
        agonGui.sendCommand("load " + result.get().trim());
      }
    }
  }

  /** Sends a pause command to toggle the game's running state. */
  @FXML
  public void pauseGame() {
    if (agonGui != null) {
      agonGui.sendCommand("pause");
    }
  }

  /** Sends a quit command to safely terminate the game. */
  @FXML
  public void quitGame() {
    if (agonGui != null) {
      agonGui.sendCommand("quit");
    }
  }

  /** Requests a hint from the engine for the current player's turn. */
  @FXML
  public void requestHint() {
    if (agonGui != null) {
      agonGui.sendCommand("hint");
    }
  }

  /** Displays the help instructions parsed from the internal text file. */
  @FXML
  public void showHelp() {
    try {
      final InputStream in = getClass().getResourceAsStream("/cmdsInformations/agonGuiMenu.txt");

      if (in != null) {
        final String helpText = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        showInfo(helpText);
      } else {
        showInfo("The help file agonGuiMenu.txt could not be found.");
      }
    } catch (Exception e) {
      showError("Error reading the help file:" + e.getMessage());
    }
  }

  /** Displays the current application configuration. */
  @FXML
  public void showConfig() {
    if (agonGui != null) {
      agonGui.sendCommand("show -configuration");
    }
  }

  /** Displays the current application version. */
  @FXML
  public void showVersion() {
    showInfo("Agon Game - GUI Version");
  }

  /** Requests the move history from the game engine. */
  @FXML
  public void showHistory() {
    if (agonGui != null) {
      agonGui.sendCommand("show -history");
    }
  }

  /** Prompts the user with a session setup dialog to choose between Local and Online modes. */
  private void promptSessionSetup() {
    if (agonGui != null && agonGui.getAppContext() != null) {
      if (agonGui.getConfig().isBlitzMode()) {
        agonGui.getAppContext().setMode(AppMode.LOCAL);
        return;
      }

      final Dialog<String> dialog = new Dialog<>();
      dialog.setTitle("Welcome to Agon");
      dialog.setHeaderText("Session Setup\\nPlease enter your name and choose a game mode:");

      final TextField nameField = new TextField(System.getProperty("user.name"));
      nameField.setPromptText("Enter your player name");
      final VBox content = new VBox(10, new Label("Player Name:"), nameField);
      content.setPadding(new Insets(10, 0, 10, 0));
      dialog.getDialogPane().setContent(content);

      final ButtonType localBtn = new ButtonType("Local (Play offline)", ButtonBar.ButtonData.YES);
      final ButtonType onlineBtn = new ButtonType("Online (Multiplayer)", ButtonBar.ButtonData.NO);
      dialog.getDialogPane().getButtonTypes().addAll(localBtn, onlineBtn);

      dialog.setResultConverter(
          btn -> {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) {
              playerName = "Player";
            }
            agonGui.getAppContext().setPlayerName(playerName);
            updateMessage("Logged in as: " + playerName);
            return btn == onlineBtn ? "ONLINE" : "LOCAL";
          });

      dialog
          .showAndWait()
          .ifPresent(
              choice -> {
                if ("ONLINE".equals(choice)) {
                  agonGui.getAppContext().setMode(AppMode.ONLINE);
                  showServerBrowser();
                } else {
                  agonGui.getAppContext().setMode(AppMode.LOCAL);
                }
              });
    }
  }

  /** Opens the Server Browser dialog to find and join local network games. */
  @FXML
  public void showServerBrowser() {
    if (agonGui != null && agonGui.getAppContext() != null) {
      if (agonGui.getAppContext().getMode() == AppMode.LOCAL) {
        showWarn(
            "This menu is restricted to Online mode.\nPlease restart the game to play online.");
      } else {
        final Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Server Browser");
        dialog.setHeaderText("Select a game server on your network to join");

        final ButtonType joinButtonType = new ButtonType("Join", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(joinButtonType, ButtonType.CANCEL);

        final ListView<String> serverList = new ListView<>();
        final TextField manualIpField = new TextField("localhost:12345");
        manualIpField.setPromptText("Enter IP:PORT...");

        try {
          final ClientDiscovery discovery = agonGui.getAppContext().getDiscovery();
          final List<ServerInfo> servers = discovery.getServers();
          if (servers.isEmpty()) {
            serverList.getItems().add("No local servers found...");
          } else {
            for (final ServerInfo s : servers) {
              serverList.getItems().add(s.name + " @ " + s.serverIp + ":" + s.tcpPort);
            }
          }
        } catch (Exception e) {
          serverList.getItems().add("Discovery service offline.");
        }

        serverList
            .getSelectionModel()
            .selectedItemProperty()
            .addListener(
                (obs, oldVal, newVal) -> {
                  if (newVal != null && newVal.contains("@")) {
                    manualIpField.setText(newVal.split("@")[1].trim());
                  }
                });

        final VBox vbox =
            new VBox(
                10,
                new Label("Discovered servers:"),
                serverList,
                new Label("Direct IP:"),
                manualIpField);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(
            btn -> btn == joinButtonType ? manualIpField.getText().trim() : null);

        dialog
            .showAndWait()
            .ifPresent(
                ipAndPort -> {
                  if (!ipAndPort.isBlank()) {
                    agonGui.sendCommand("join " + ipAndPort);
                  }
                });
      }
    }
  }

  /** Opens the Host Server dialog to start or stop a local multiplayer server. */
  @FXML
  public void showHostServerDialog() {
    if (agonGui != null && agonGui.getAppContext() != null) {
      if (agonGui.getAppContext().getMode() == AppMode.LOCAL) {
        showWarn(
            "This menu is restricted to Online mode.\nPlease restart the game to play online.");
      } else {
        final AgonServer server = agonGui.getAppContext().getServer();
        final boolean isRunning = server != null && server.isRunning();

        final Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Host Game Server");

        if (isRunning) {
          dialog.setHeaderText(
              "Server is already running on port "
                  + server.getPort()
                  + "\nConnected clients: "
                  + server.getConnectedClientsCount());
          final ButtonType stopBtn = new ButtonType("Stop Server", ButtonBar.ButtonData.OK_DONE);
          dialog.getDialogPane().getButtonTypes().addAll(stopBtn, ButtonType.CANCEL);

          dialog.setResultConverter(btn -> btn == stopBtn ? "STOP" : null);
        } else {
          dialog.setHeaderText("Start a new local game server");
          final ButtonType startBtn = new ButtonType("Start", ButtonBar.ButtonData.OK_DONE);
          dialog.getDialogPane().getButtonTypes().addAll(startBtn, ButtonType.CANCEL);

          final TextField portField = new TextField("12345");
          final VBox vbox = new VBox(10, new Label("TCP Port (default 12345):"), portField);
          dialog.getDialogPane().setContent(vbox);

          dialog.setResultConverter(btn -> btn == startBtn ? portField.getText().trim() : null);
        }

        dialog
            .showAndWait()
            .ifPresent(
                result -> {
                  if ("STOP".equals(result)) {
                    agonGui.sendCommand("server_stop");
                  } else {
                    agonGui.sendCommand("server_start " + result);
                    agonGui.sendCommand("join localhost:" + result);
                  }
                });
      }
    }
  }

  /** Opens the multiplayer lobby to view connected players and issue challenges. */
  @FXML
  public void showLobby() {
    if (agonGui != null && agonGui.getAppContext() != null) {
      if (agonGui.getAppContext().getMode() == AppMode.LOCAL) {
        showWarn(
            "This menu is restricted to Online mode.\nPlease restart the game to play online.");
      } else {
        final AgonClient client = agonGui.getAppContext().getClient();
        if (client == null || !client.isConnected()) {
          showWarn("You must be connected to a server to open the lobby!");
        } else {
          final Dialog<String> dialog = new Dialog<>();
          dialog.setTitle("Multiplayer Lobby");
          dialog.setHeaderText("See who is online and challenge them!");

          final ButtonType challengeBtn =
              new ButtonType("Challenge Player", ButtonBar.ButtonData.OK_DONE);
          dialog.getDialogPane().getButtonTypes().addAll(challengeBtn, ButtonType.CLOSE);

          final TextArea infoArea = new TextArea();
          infoArea.setEditable(false);
          infoArea.setPrefRowCount(12);
          infoArea.setPrefColumnCount(40);
          infoArea.setStyle("-fx-font-family: monospace;");

          final String initialPlayers = client.requestPlayers();
          infoArea.setText(initialPlayers != null ? initialPlayers : "Failed to load players.");

          final Button refreshBtn = new Button("Refresh Players");
          refreshBtn.setOnAction(e -> infoArea.setText(client.requestPlayers()));

          final Button scoreBtn = new Button("View Scoreboard");
          scoreBtn.setOnAction(e -> infoArea.setText(client.requestScoreboard()));

          final HBox topButtons = new HBox(10, refreshBtn, scoreBtn);

          final TextField playerIdField = new TextField();
          playerIdField.setPromptText("Ex: 2");
          final HBox challengeBox = new HBox(10, new Label("Target Player ID:"), playerIdField);

          final VBox layout = new VBox(10, topButtons, infoArea, challengeBox);
          dialog.getDialogPane().setContent(layout);

          dialog.setResultConverter(
              btn -> {
                String cmd = null;
                if (btn == challengeBtn) {
                  final String targetId = playerIdField.getText().trim();
                  if (!targetId.isEmpty()) {
                    cmd = "new " + targetId;
                  }
                }
                return cmd;
              });

          dialog.showAndWait().ifPresent(cmd -> agonGui.sendCommand(cmd));
        }
      }
    }
  }

  private void refreshBoardFromNetwork() {
    if (agonGui != null && agonGui.getAppContext() != null) {
      final Match match = agonGui.getAppContext().getCurrentOnlineMatch();
      if (match != null) {
        agonGui.onMatchUpdate(match);
      }
    }
  }

  /**
   * Cleanly subscribes to the client's network events to update the graphical interface without
   * needing to hijack the standard console output (System.out).
   */
  private void setupNetworkListener() {
    AsyncEventLogger.setEventObserver(
        message -> {
          if (message != null) {
            if (message.contains("INVITATION_RECEIVED")) {
              Platform.runLater(() -> promptInvitation(message));
            } else if (message.contains("CHOOSE_MODE")) {
              Platform.runLater(() -> promptGameMode());
            } else if (message.contains("GAME_STARTED GAME_ID")) {
              Platform.runLater(this::refreshBoardFromNetwork);
            } else if (message.contains("Your turn")
                || message.contains("Opponent turn")
                || message.contains("You are")) {
              Platform.runLater(this::refreshBoardFromNetwork);
            }
          }
        });
  }

  private void promptGameMode() {
    final Alert alert = new Alert(AlertType.CONFIRMATION);
    alert.setTitle("Select Game Mode");
    alert.setHeaderText("The match is ready!");
    alert.setContentText("Do you want to play a Normal game or a Blitz game?");

    final ButtonType btnNormal = new ButtonType("Normal", ButtonBar.ButtonData.YES);
    final ButtonType btnBlitz = new ButtonType("Blitz", ButtonBar.ButtonData.NO);
    alert.getButtonTypes().setAll(btnNormal, btnBlitz);

    final Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == btnNormal) {
      if (agonGui != null) {
        agonGui.sendCommand("mode normal");
      }
    } else {
      if (agonGui != null) {
        agonGui.sendCommand("mode blitz");
      }
    }
  }

  /** Opens the shortcut editor dialog allowing the user to remap key bindings. */
  @FXML
  public void editShortcuts() {
    if (agonGui != null) {
      final Dialog<Map<String, String>> dialog = new Dialog<>();
      dialog.setTitle("Keyboard Shortcuts");
      dialog.setHeaderText("Modify your shortcuts (ex: Ctrl+N, Alt+N)");

      final ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
      dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

      final GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));

      final Map<String, String> currentShortcuts = agonGui.getConfig().getShortcuts();
      final Map<String, TextField> fields = new HashMap<>();
      int row = 0;
      for (final String key : currentShortcuts.keySet()) {
        grid.add(new Label(key.replace("shortcut_", "") + " :"), 0, row);
        final TextField tf = new TextField(currentShortcuts.get(key));
        grid.add(tf, 1, row);
        fields.put(key, tf);
        row++;
      }
      dialog.getDialogPane().setContent(grid);

      dialog.setResultConverter(
          dialogButton -> {
            Map<String, String> res = null;
            if (dialogButton == saveButtonType) {
              res = new HashMap<>();
              final Map<String, String> finalRes = res;
              fields.forEach((key, tf) -> finalRes.put(key, tf.getText().trim()));
            }
            return res;
          });

      final Optional<Map<String, String>> result = dialog.showAndWait();
      result.ifPresent(
          newShortcuts -> {
            newShortcuts.forEach(
                (key, tf) -> {
                  agonGui.getConfig().addShortcut(key, tf);
                });
            currentShortcuts.putAll(newShortcuts);
            try {
              if (!"true".equals(System.getProperty("IS_TEST_ENV"))) {
                new ConfigSerializer().save(agonGui.getConfig(), ".agonrc");
              }
            } catch (Exception e) {
              GameLogger.error("Failed to save shortcuts to .agonrc: " + e.getMessage());
            }
            AgonApp.refreshShortcuts();
            showInfo("Shortcuts updated successfully !");
          });
    }
  }
}
