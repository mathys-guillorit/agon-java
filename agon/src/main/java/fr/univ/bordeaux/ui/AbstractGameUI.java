package fr.univ.bordeaux.ui;

public abstract class AbstractGameUI implements GameUserInterface {

    private GameUserInterface gameEngine;

    public void setGameEngine(GameUserInterface gameEngine) {
        this.gameEngine = gameEngine;
    }

    protected GameUserInterface getGameEngine() {
        if (this.gameEngine == null) {
            throw new IllegalStateException("The game engine is disconnected");
        }
        return this.gameEngine;
    }

    public abstract void start();

    @Override public void startNewGame(String[] args) {
        getGameEngine().startNewGame(args);
    }

/*

    @Override public void tryMove(Position from, Position to) {
        getGameEngine().tryMove(from, to);
    }

    @Override public void selectPiece(Position pos) {
        getGameEngine().selectPiece(pos);
    }

*/

    @Override public void undo() {
        getGameEngine().undo();
    }

    @Override public void redo() {
        getGameEngine().redo();
    }

    @Override public void saveGame(String f) {
        getGameEngine().saveGame(f);
    }

    @Override public void loadGame(String f) {
        getGameEngine().loadGame(f);
    }

    @Override public void pauseGame() {
        getGameEngine().pauseGame();
    }

    @Override public void resumeGame() {
        getGameEngine().resumeGame();
    }

    @Override public void quitGame() {
        getGameEngine().quitGame();
    }

    @Override public void requestHint() {
        getGameEngine().requestHint();
    }

}