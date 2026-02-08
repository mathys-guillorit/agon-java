package fr.univ.bordeaux.application.ai.strategy;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.heuristics.Heuristics;

public abstract class AbstractAgonAI implements AgonAI {
    protected final Heuristics heuristic;
    protected Color color;
    protected long timeLimit = 5000;
    protected long startTime;
    protected long nodeCount = 0;

    public AbstractAgonAI(Heuristics heuristic) {
        this.heuristic = heuristic;
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