package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.strategy.AgonAi;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;

public class AiPlayer extends AbstractPlayer {

  AgonBoard board;
  AgonAi ai;

  public AiPlayer(String name, Color color, AgonBoard board, AgonAi ai) {
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
  public CmdAction getAction(AgonRegister<CmdAction> cmds) {
    CmdAction cmd = new CmdMove(ai.getBestMove(board), null);
    System.out.println("l'ia a crée un move ");
    return cmd;
  }
}
