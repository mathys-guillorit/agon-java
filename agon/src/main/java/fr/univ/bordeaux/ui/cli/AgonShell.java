package fr.univ.bordeaux.ui.cli;

import fr.univ.bordeaux.ui.AbstractGameUI;

public class AgonShell extends AbstractGameUI {

    private boolean running;

    public AgonShell() {
        this.running = true;
    }
    
    @Override
    public void start() {}

    @Override
    public void updateBoard(String board) {}

    @Override
    public void showMessage(String message) {}

    @Override
    public void showError(String error) {}

    @Override
    public boolean getUserConfirmation(String prompt) {
        return false;
    }
}
