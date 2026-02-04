package fr.univ.bordeaux.application.ai.interfaces;

public abstract class AbstractAgonAI implements AgonAI {
    protected long timeLimit = 5000; 
    protected long startTime;

    // Méthode utilitaire pour les enfants
    protected boolean isTimeRemaining() {
        return (System.currentTimeMillis() - startTime) < (timeLimit - 50); 
    }
    
    @Override
    public final Move getBestMove(AgonBoard board) {
        this.startTime = System.currentTimeMillis();
        return computeMove(board);
    }

    // Chaque algo doit implémenter ça
    protected abstract Move computeMove(AgonBoard board);
}