package fr.univ.bordeaux.application.ai.strategy.mcts;

import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAI;

import java.util.Random;

public class MctsStrategy extends AbstractAgonAI {
    @Override
    protected Move computeMove(AgonBoard board) {
        // Implémentation basique de MCTS
        // 1. Sélection
        // 2. Expansion
        // 3. Simulation
        // 4. Rétropropagation

        // Pour l'instant, on retourne un coup aléatoire (placeholder)
        List<Move> legalMoves = board.generateLegalMoves();
        return legalMoves.get(new Random().nextInt(legalMoves.size()));
    }

    @Override
    public void setTimeLimit(long millis) {

    }
}