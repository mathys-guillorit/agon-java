package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;

public class CentralityHeuristic extends AbstractHeuristic {

    public CentralityHeuristic() {
        super(5, 200);
    }

    @Override
    protected long getFactor(AgonBoard board, int index) {
        long dist = board.getCentrality(index);
        long inverted = 6 - dist;
        return inverted * inverted;
    }
}