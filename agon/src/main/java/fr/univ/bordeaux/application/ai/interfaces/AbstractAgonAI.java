package fr.univ.bordeaux.application.ai.interfaces;

public abstract class AbstractAgonAI implements AgonAI {
    private final BoardEvaluator evaluator;
    protected long timeLimit = 5000;
    protected long startTime;
    protected long nodeCount = 0;

    public AbstractAgonAI(BoardEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public void setTimeLimit(long millis) {
        this.timeLimit = millis;
    }

    // Méthode utilitaire pour les enfants
    protected boolean isTimeRemaining() {
        return (System.currentTimeMillis() - startTime) < (timeLimit - 50); 
    }

    public long getNodeCount() {
        return nodeCount;
    }
    
    @Override
    public final Move getBestMove(AgonBoard board) {
        this.startTime = System.currentTimeMillis();
        return computeMove(board);
    }

    // Chaque algo doit implémenter ça
    protected abstract Move computeMove(AgonBoard board);
}