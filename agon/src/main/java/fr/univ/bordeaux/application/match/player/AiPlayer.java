package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.strategy.AgonAI;

public class AiPlayer extends AbstractPlayer {
  AgonBoard board;
  AgonAI ai;

  public AiPlayer(String name, Color color, AgonBoard board, AgonAI AI) {
    super(name, color);
    this.board = board;
    this.ai = AI;
  }

  @Override
  public boolean isAI() {
    return true;
  }

  @Override
  public Color getColor() {
    return color;
  }

  @Override
  public String getName() {
    return name;
  }

  public Move play() {
    return ai.getBestMove(board);
  }
}
