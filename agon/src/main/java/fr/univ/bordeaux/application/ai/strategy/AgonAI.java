package fr.univ.bordeaux.application.ai.strategy;

public interface AgonAI {
    Move getBestMove(AgonBoard board);
    void setTimeLimit(long millis); 
}