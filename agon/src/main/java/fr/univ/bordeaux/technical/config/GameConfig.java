package fr.univ.bordeaux.technical.config;

public class GameConfig {
    private boolean verbose = false;
    private boolean debug = false;
    private boolean blitzMode = false;
    private int timeout = 300;
    private boolean aiActive = false;
    private Color aiColor = Color.NONE; // Enum ou String
    private String aiMode= "MINIMAX";
    private int aiDepth = 4;
    private double aiTimeLimit = 2.0;
    private boolean aiIterativeDeepening = true;
    private String aiHeuristic = "MIXED";
}
