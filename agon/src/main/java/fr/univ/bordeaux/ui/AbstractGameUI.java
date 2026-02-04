package fr.univ.bordeaux.ui;

public abstract class AbstractGameUI implements IGameUserInterface {
    private IGameController controller;

    public void setController(IGameController controller) {
        this.controller = controller;
    }
    protected IGameController getController() {
        if (this.controller == null) {
            throw new IllegalStateException("Controller has not been set.");
        }
        return this.controller;
    }

    public abstract void start();    
}
