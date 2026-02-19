package fr.univ.bordeaux.technical.config;

import fr.univ.bordeaux.agonCore.agonElements.Color;

public class GameConfig {
    private boolean verbose = false;
    private boolean debug = false;
    private boolean blitzMode = false;
    private int timeout = 1800;
    private boolean aiActive = true;
    private String aiMode = "MINIMAX";
    private int aiDepth = 4;
    private int aiTimeLimit = 5;
    private boolean aiIterativeDeepening = true;
    private String aiHeuristic = "MIXED";
    private boolean whiteIsAI = false;
    private boolean blackIsAI = true;

    public void setWhiteAI(boolean whiteIsAI) { this.whiteIsAI = whiteIsAI; }

    public void setBlackAI(boolean blackIsAI) { this.blackIsAI = blackIsAI; }

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

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isBlitzMode() {
        return blitzMode;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isAiActive() {
        return aiActive;
    }

    public String getAiMode() {
        return aiMode;
    }

    public int getAiDepth() {
        return aiDepth;
    }

    public int getAiTimeLimit() {
        return aiTimeLimit;
    }

    public boolean isAiIterativeDeepening() {
        return aiIterativeDeepening;
    }

    public String getAiHeuristic() {
        return aiHeuristic;
    }

    public boolean isWhiteAI() { return whiteIsAI; }

    public boolean isBlackAI() { return blackIsAI; }
}
