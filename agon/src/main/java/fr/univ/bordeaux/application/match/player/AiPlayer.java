package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.strategy.AgonAI;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;

public class AiPlayer extends AbstractPlayer {

  AgonBoard board;
  AgonAI ai;

  public AiPlayer(String name, Color color, AgonBoard board, AgonAI ai) {
    super(name, color);
    this.board = board;
    this.ai = ai;
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

  @Override
  public CmdAction getAction() {
    return new CmdMove(ai.getBestMove(board));
  }
}
