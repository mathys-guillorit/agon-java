package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.strategy.AgonAi;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;

/**
 * Default AIPlayer class to play with.
 */
public class AiPlayer extends AbstractPlayer {

  AgonBoard board;
  AgonAi ai;

  /**
   * Create a AiPlayer.
   *
   * @param name {@link String} AiPlayer's name
   *
   * @param color {@link Color} AiPlayer's color
   *
   * @param board {@link AgonBoard} board to play on
   *
   * @param ai {@link AgonAi}
   */
  public AiPlayer(String name, Color color, AgonBoard board, AgonAi ai) {
    super(name, color);
    this.board = board;
    this.ai = ai;
  }

  /**
   * Get if the AIPlayer is an AI or not.
   *
   * @return true | false
   */
  @Override
  public boolean isAi() {
    return true;
  }

  /**
   * Get the player's color.
   *
   * @return {@link Color}
   */
  @Override
  public Color getColor() {
    return color;
  }

  /**
   * Get the player's name.
   *
   * @return {@link String}
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Get command Behavior.
   *
   * @param cmds {@link AgonRegister}
   *
   * @return {@link CmdAction}
   */
  @Override
  public CmdAction getAction(AgonRegister<CmdAction> cmds) {
    CmdAction cmd = new CmdMove(ai.getBestMove(board), null);
    return cmd;
  }
}
