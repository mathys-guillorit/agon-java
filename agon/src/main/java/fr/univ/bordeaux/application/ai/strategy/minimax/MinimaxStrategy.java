package fr.univ.bordeaux.application.ai.strategy.minimax;

import fr.univ.bordeaux.application.ai.interfaces.AbstractAgonAI;

import java.util.Random;

public class MinimaxStrategy extends AbstractAgonAI {
    @Override
    protected Move computeMove(AgonBoard board) {
        // Implémentation basique de Minimax

        // Pour l'instant, on retourne un coup aléatoire (placeholder)
        List<Move> legalMoves = board.generateLegalMoves();
        return legalMoves.get(new Random().nextInt(legalMoves.size()));
    }

    @Override
    public void setTimeLimit(long millis) {

    }
}
