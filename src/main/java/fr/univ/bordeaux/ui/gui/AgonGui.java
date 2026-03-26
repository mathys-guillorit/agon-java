package fr.univ.bordeaux.ui.gui;

import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.technical.config.GameConfig;
import fr.univ.bordeaux.ui.AbstractGameUi;
import fr.univ.bordeaux.ui.gui.controllers.GameViewController;
import javafx.application.Platform;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AgonGui extends AbstractGameUi {

    private AgonBoardImpl board;
    private final AtomicBoolean debugMode = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(true);
    private boolean verbose = false;
    private final GameConfig config;
    private final BlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    public AgonGui(GameConfig config) {
        this.config = config;
    }

    public GameConfig getConfig() {
        return this.config;
    }

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

    public void setBoard(AgonBoardImpl board) {
        this.board = board;
    }

    public void start() {
        AgonApp.setBoard(this.board);
        AgonApp.setGui(this);
        new Thread(() -> javafx.application.Application.launch(AgonApp.class)).start();
    }

    @Override
    public void updateBoard(RestrictedAgonBoard agonBoard) {
        Platform.runLater(() -> {
            GameViewController controller = AgonApp.getController();
            if (controller != null) controller.refreshBoard(agonBoard);
        });
    }

    @Override
    public void showMessage(String message) {
        Platform.runLater(() -> {
            GameViewController controller = AgonApp.getController();
            if (controller != null) controller.updateMessage(message);
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
            if (controller != null) controller.showInfo(info);
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
        System.exit(0);
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