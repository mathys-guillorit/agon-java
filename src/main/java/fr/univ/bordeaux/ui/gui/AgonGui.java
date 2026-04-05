package fr.univ.bordeaux.ui.gui;

import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.application.match.BlitzMatch;
import fr.univ.bordeaux.application.match.MoveDtO;
import fr.univ.bordeaux.application.match.ReadOnlyMatch;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.MatchObserver;
import fr.univ.bordeaux.ui.gui.controllers.GameViewController;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.util.Duration;

/**
 * The JavaFX implementation of the Agon Game User Interface.
 *
 * <p>This class acts as the bridge between the core game engine and the JavaFX visual components.
 * It handles the command queue, updates the graphical board state, and manages UI popups.
 */
public class AgonGui implements GameUserInterface, MatchObserver {

  private final AtomicBoolean debugMode = new AtomicBoolean(false);
  private final AtomicBoolean running = new AtomicBoolean(true);
  private final GameConfig config;
  private final BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();
  private ReadOnlyMatch currentMatch;
  private Timeline blitzTimeline;

  /**
   * Constructs a new AgonGui instance.
   *
   * @param config The game configuration settings.
   */
  public AgonGui(GameConfig config) {
    this.config = config;
  }

  /**
   * Retrieves the current game configuration.
   *
   * @return The GameConfig instance.
   */
  public GameConfig getConfig() {
    return this.config;
  }

  /**
   * Submits a command string to the processing queue.
   *
   * @param command The command input (e.g., "new", "F6G7").
   */
  public void sendCommand(String command) {
    commandQueue.offer(command);
  }

  @Override
  public String getUserInput() {
    try {
      return commandQueue.take();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    }
  }

  @Override
  public void displayHistory(List<MoveDtO> moves) {
    if (moves.isEmpty()) {
      showInfo("The history is currently empty.");
      return;
    }

    StringBuilder sb = new StringBuilder("--- Move History ---\n\n");
    for (int i = 0; i < moves.size(); i++) {
      MoveDtO m = moves.get(i);
      sb.append(
          String.format(
              "%d. %s : %s -> %s\n", i / 2 + 1, i % 2 == 0 ? "White" : "Black", m.from(), m.to()));
    }
    showInfo(sb.toString());
  }

  /** Initializes and launches the JavaFX application thread. */
  public void start() {
    AgonApp.setGui(this);
    new Thread(() -> Application.launch(AgonApp.class)).start();
  }

  public void updateBoard(RestrictedAgonBoard agonBoard) {
    GameViewController controller = AgonApp.getController();
    if (controller != null && controller.getHexCanvas() != null) {
      Map<String, PieceType> safeSnapshot = controller.getHexCanvas().takeSnapshot(agonBoard);
      Platform.runLater(
          () -> {
            controller.getHexCanvas().applySnapshot(agonBoard, safeSnapshot);
          });
    }
  }

  @Override
  public void showMessage(String message) {
    Platform.runLater(
        () -> {
          GameViewController controller = AgonApp.getController();
          if (controller != null && message != null) {
            controller.routeMessage(message.trim());
          }
        });
  }

  @Override
  public void showError(String error) {
    System.err.println("[GUI ERROR] " + error);
    Platform.runLater(
        () -> {
          GameViewController controller = AgonApp.getController();
          if (controller != null) controller.showError(error);
        });
  }

  @Override
  public void showInfo(String info) {
    Platform.runLater(
        () -> {
          GameViewController controller = AgonApp.getController();
          if (controller != null && info != null) {
            controller.routeMessage(info.trim());
          }
        });
  }

  @Override
  public void showWarn(String warning) {
    Platform.runLater(
        () -> {
          GameViewController controller = AgonApp.getController();
          if (controller != null) controller.showWarn(warning);
        });
  }

  @Override
  public void showHelp() {
    Platform.runLater(
        () -> {
          GameViewController controller = AgonApp.getController();
          if (controller != null) controller.showHelp();
        });
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  @Override
  public void quit() {
    running.set(false);

    if (!"true".equals(System.getProperty("IS_TEST_ENV"))) {
      Platform.exit();
      System.exit(0);
    }
  }

  @Override
  public void onMatchUpdate(ReadOnlyMatch match) {
    this.currentMatch = match;
    if (match != null) {
      updateBoard(match.getAgonBoard());

      if (match.isMatchOver()) {
        if (blitzTimeline != null) blitzTimeline.stop();
        showInfo(
            "MATCH OVER! Winner: "
                + (match.getWinner() != null ? match.getWinner().getColor() : "None"));
      } else {
        updateStatusMessage();
        if (match instanceof BlitzMatch && blitzTimeline == null) {
          blitzTimeline =
              new Timeline(new KeyFrame(Duration.seconds(1), e -> updateStatusMessage()));
          blitzTimeline.setCycleCount(Timeline.INDEFINITE);
          blitzTimeline.play();
        }
      }
    }
  }

  private void updateStatusMessage() {
    if (currentMatch == null || currentMatch.isMatchOver()) return;
    StringBuilder sb =
        new StringBuilder("Current Player: " + currentMatch.getCurrentPlayer().getColor());
    if (currentMatch instanceof BlitzMatch blitzMatch) {
      sb.append("   |   Time left: ").append(blitzMatch.getRemainingTime());
    }
    showMessage(sb.toString());
  }

  @Override
  public AtomicBoolean getDebugMode() {
    return debugMode;
  }

  @Override
  public void setVerbose(boolean state) {}
}
