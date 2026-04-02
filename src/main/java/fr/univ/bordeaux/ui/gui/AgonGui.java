package fr.univ.bordeaux.ui.gui;

import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.AbstractGameUi;
import fr.univ.bordeaux.ui.gui.controllers.GameViewController;
import javafx.application.Platform;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The JavaFX implementation of the Agon Game User Interface.
 * <p>
 * This class acts as the bridge between the core game engine and the JavaFX visual components.
 * It handles the command queue, updates the graphical board state, and manages UI popups.
 */
public class AgonGui extends AbstractGameUi {

    private AgonBoardImpl board;
    private final AtomicBoolean debugMode = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(true);
    private boolean verbose = false;
    private final GameConfig config;
    private final BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

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

    /**
     * Binds the core game board to the UI.
     *
     * @param board The main game board implementation.
     */
    public void setBoard(AgonBoardImpl board) {
        this.board = board;
    }

    /**
     * Initializes and launches the JavaFX application thread.
     */
    public void start() {
        AgonApp.setBoard(this.board);
        AgonApp.setGui(this);
        new Thread(() -> javafx.application.Application.launch(AgonApp.class)).start();
    }

    @Override
    public void updateBoard(RestrictedAgonBoard agonBoard) {
        GameViewController controller = AgonApp.getController();
        if (controller != null && controller.getHexCanvas() != null) {
            Map<String, PieceType> safeSnapshot = controller.getHexCanvas().takeSnapshot(agonBoard);
            Platform.runLater(() -> {
                controller.getHexCanvas().applySnapshot(agonBoard, safeSnapshot);
            });
        }
    }

    @Override
    public void showMessage(String message) {
        Platform.runLater(() -> {
            GameViewController controller = AgonApp.getController();
            if (controller != null && message != null) {
                controller.routeMessage(message.trim());
            }
        });
    }

    @Override
    public void showError(String error) {
        System.err.println("[GUI ERROR] " + error);
        Platform.runLater(() -> {
            GameViewController controller = AgonApp.getController();
            if (controller != null) controller.showError(error);
        });
    }

    @Override
    public void showInfo(String info) {
        Platform.runLater(() -> {
            GameViewController controller = AgonApp.getController();
            if (controller != null && info != null) {
                controller.routeMessage(info.trim());
            }
        });
    }

    @Override
    public void showWarn(String warning) {
        Platform.runLater(() -> {
            GameViewController controller = AgonApp.getController();
            if (controller != null) controller.showWarn(warning);
        });
    }

    @Override
    public void showHelp() {
        Platform.runLater(() -> {
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
        Platform.exit();
    }

    @Override
    public AtomicBoolean getDebugMode() {
        return debugMode;
    }

    @Override
    public void setVerbose(boolean state) {
        this.verbose = state;
    }

}