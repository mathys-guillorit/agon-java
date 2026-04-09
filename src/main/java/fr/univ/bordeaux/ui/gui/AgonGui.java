package fr.univ.bordeaux.ui.gui;

import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.match.BlitzMatch;
import fr.univ.bordeaux.application.match.MoveDtO;
import fr.univ.bordeaux.application.match.ReadOnlyMatch;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.technical.utils.GameLogger;
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

  /** Flag indicating whether the debug mode is currently active. */
  private final AtomicBoolean debugMode = new AtomicBoolean(false);

  /** Flag indicating whether the GUI application is currently running. */
  private final AtomicBoolean running = new AtomicBoolean(true);

  /** The user's game configuration settings. */
  private final GameConfig config;

  /** The global application context storing profiles and network states. */
  private final AppContext context;

  /** A thread-safe queue storing commands sent from the UI to the engine. */
  private final BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

  /** The currently active match state (read-only). */
  private ReadOnlyMatch currentMatch;

  /** The animation timeline controlling the blitz mode countdown updates. */
  private Timeline blitzTimeline;

  /** Flag indicating whether the current match is paused. */
  private boolean isPaused = false;

  /**
   * Constructs a new AgonGui instance.
   *
   * @param config The game configuration settings.
   */
  public AgonGui(final GameConfig config, final AppContext context) {
    this.config = config;
    this.context = context;
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
   * Retrieves the current application context.
   *
   * @return The AppContext instance.
   */
  public AppContext getAppContext() {
    return this.context;
  }

  /**
   * Submits a command string to the processing queue.
   *
   * @param command The command input (e.g., "new", "F6G7").
   */
  public void sendCommand(final String command) {
    if (command != null && command.trim().equalsIgnoreCase("pause")) {
      isPaused = !isPaused;
      updateStatusMessage();
    }
    commandQueue.offer(command);
  }

  @Override
  public String getUserInput() {
    String input = null;
    try {
      input = commandQueue.take();
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return input;
  }

  @Override
  public void displayHistory(final List<MoveDtO> moves) {
    if (moves.isEmpty()) {
      showInfo("The history is currently empty.");
    } else {
      final StringBuilder sb = new StringBuilder("--- Move History ---\n\n");
      for (int i = 0; i < moves.size(); i++) {
        final MoveDtO m = moves.get(i);
        sb.append(
            String.format(
                "%d. %s : %s -> %s\n",
                i / 2 + 1, i % 2 == 0 ? "White" : "Black", m.from(), m.to()));
      }
      showInfo(sb.toString());
    }
  }

  /** * Initializes and launches the underlying JavaFX application thread. */
  public void start() {
    AgonApp.setGui(this);
    new Thread(() -> Application.launch(AgonApp.class)).start();
  }

  /**
   * Synchronizes the graphical board with the provided core game board state.
   *
   * @param agonBoard The updated board state to render.
   */
  public void updateBoard(final RestrictedAgonBoard agonBoard) {
    final GameViewController controller = AgonApp.getController();
    if (controller != null && controller.getHexCanvas() != null) {
      final Map<String, PieceType> safeSnapshot = controller.getHexCanvas().takeSnapshot(agonBoard);
      Platform.runLater(
          () -> {
            controller.getHexCanvas().applySnapshot(agonBoard, safeSnapshot);
          });
    }
  }

  @Override
  public void showMessage(final String message) {
    Platform.runLater(
        () -> {
          final GameViewController controller = AgonApp.getController();
          if (controller != null && message != null) {
            controller.routeMessage(message.trim());
          }
        });
  }

  @Override
  public void showError(final String error) {
    GameLogger.error("[GUI ERROR] " + error);
    Platform.runLater(
        () -> {
          final GameViewController controller = AgonApp.getController();
          if (controller != null) {
            controller.showError(error);
          }
        });
  }

  @Override
  public void showInfo(final String info) {
    Platform.runLater(
        () -> {
          final GameViewController controller = AgonApp.getController();
          if (controller != null && info != null) {
            controller.routeMessage(info.trim());
          }
        });
  }

  @Override
  public void showWarn(final String warning) {
    Platform.runLater(
        () -> {
          final GameViewController controller = AgonApp.getController();
          if (controller != null) {
            controller.showWarn(warning);
          }
        });
  }

  @Override
  public void showHelp() {
    Platform.runLater(
        () -> {
          final GameViewController controller = AgonApp.getController();
          if (controller != null) {
            controller.showHelp();
          }
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
  public void onMatchUpdate(final ReadOnlyMatch match) {
    this.currentMatch = match;
    if (match != null) {
      updateBoard(match.getAgonBoard());

      if (match.isMatchOver()) {
        if (blitzTimeline != null) {
          blitzTimeline.stop();
        }
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
    if (currentMatch != null && !currentMatch.isMatchOver() && !isPaused) {
      final StringBuilder sb =
          new StringBuilder("Current Player: " + currentMatch.getCurrentPlayer().getColor());
      if (currentMatch instanceof BlitzMatch blitzMatch) {
        sb.append("   |   Time left: ").append(blitzMatch.getCurrentPlayerRemainingTime());
      }
      showMessage(sb.toString());
    }
  }

  /**
   * Checks if the game is currently paused.
   *
   * @return True if the game is paused, false otherwise.
   */
  public boolean getPaused() {
    return isPaused;
  }

}
