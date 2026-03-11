package fr.univ.bordeaux.application.ai.strategy.mcts;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.heuristics.Heuristic;
import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAI;
import java.util.List;
import java.util.Random;

public class MctsStrategy extends AbstractAgonAI {

  public MctsStrategy(Heuristic heuristic, Color color) {
    super(heuristic, color);
  }

  @Override
  protected Move computeMove(AgonBoard board) {
    // Implémentation basique de MCTS
    // 1. Sélection
    // 2. Expansion
    // 3. Simulation
    // 4. Rétropropagation

    // Pour l'instant, on retourne un coup aléatoire (placeholder)
    List<Move> legalMoves = board.generateLegalMoves(this.color);
    return legalMoves.get(new Random().nextInt(legalMoves.size()));
  }

  @Override
  public void setTimeLimit(long millis) {}
}
