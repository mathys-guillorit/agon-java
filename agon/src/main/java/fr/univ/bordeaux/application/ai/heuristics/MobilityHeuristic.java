package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;

public class MobilityHeuristic extends AbstractHeuristic {

    public MobilityHeuristic() {
        super(5, 20);
    }

    @Override
    protected long getFactor(AgonBoard board, int index) {
        return board.getMobility(index);
    }
}