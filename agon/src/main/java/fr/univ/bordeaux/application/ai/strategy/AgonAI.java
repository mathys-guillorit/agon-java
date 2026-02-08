package fr.univ.bordeaux.application.ai.strategy;

import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;

public interface AgonAI {
    Move getBestMove(AgonBoard board);
    void setTimeLimit(long millis); 
}