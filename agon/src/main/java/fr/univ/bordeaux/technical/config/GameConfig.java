package fr.univ.bordeaux.technical.config;

import fr.univ.bordeaux.agonCore.agonElements.Color;

public class GameConfig {
    private boolean verbose;
    private boolean debug;
    private boolean blitzMode;
    private int timeout;
    private boolean aiActive;
    private Color aiColor;
    private String aiMode;
    private int aiDepth;
    private double aiTimeLimit;
    private boolean aiIterativeDeepening;
    private String aiHeuristic;

    public void setVerbose(boolean verbose) {
        this.verbose=verbose;
    }

    public void setDebug(boolean debug) {
        this.debug=debug;
    }

    public void setBlitzMode(boolean blitzMode) {
        this.blitzMode=blitzMode;
    }

    public void setTimeout(int timeout) {
        this.timeout=timeout;
    }

    public void setAi(boolean ai) {
        this.aiActive=ai;
    }

    public void setAiColor(Color color) {
        this.aiColor=color;
    }

    public void setAiMode(String mode) {
        this.aiMode=mode;
    }

    public void setAiDepth(int depth) {
        this.aiDepth=depth;
    }

    public void setAiTimeLimit(int timeLimit) {
        this.aiTimeLimit=timeLimit;
    }

    public void setAiIterativeDeepening(boolean iterativeDeepening) {
        this.aiIterativeDeepening=iterativeDeepening;
    }

    public void setAiHeuristic(String heuristic) {
        this.aiHeuristic=heuristic;
    }
}
