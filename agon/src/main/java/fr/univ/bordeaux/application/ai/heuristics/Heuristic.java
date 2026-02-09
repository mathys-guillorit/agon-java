package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;

public interface Heuristic {
    public long evaluate(AgonBoard board, Color aiColor);
}
