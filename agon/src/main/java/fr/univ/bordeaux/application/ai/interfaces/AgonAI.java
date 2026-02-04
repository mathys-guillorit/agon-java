package fr.univ.bordeaux.application.ai.interfaces;

public interface AgonAI {
    Move getBestMove(AgonBoard board);
    void setTimeLimit(long millis); 
}