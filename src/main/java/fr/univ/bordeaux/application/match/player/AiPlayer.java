package fr.univ.bordeaux.application.match.player;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.strategy.AgonAi;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;

/**
 * Implementation of a player controlled by Artificial Intelligence.
 *
 * <p>This player uses an {@link AgonAi} strategy to evaluate the board and decide the best move to
 * execute during its turn.
 */
public class AiPlayer extends AbstractPlayer {

  /** The board instance the AI uses to simulate and analyze moves. */
  private final AgonBoard board;

  /** The AI strategy engine used to calculate optimal moves. */
  private final AgonAi ai;

  /**
   * Constructs an AiPlayer with a specific strategy.
   *
   * @param name The display name for this AI.
   * @param color The {@link Color} assigned to the AI.
   * @param board The current {@link AgonBoard} state.
   * @param ai The {@link AgonAi} strategy to be used.
   */
  public AiPlayer(String name, Color color, AgonBoard board, AgonAi ai) {
    super(name, color);
    this.board = board;
    this.ai = ai;
  }

  /**
   * Calculates the best action for the AI using its strategy engine.
   *
   * <p>The AI analyzes the current board state and returns a {@link CmdMove} containing its chosen
   * move.
   *
   * @param cmds The registry of available commands (unused by the AI).
   * @return A {@link CmdAction} representing the AI's chosen move.
   */
  @Override
  public CmdAction getAction(AgonRegister<CmdAction> cmds) {
    return new CmdMove(ai.getBestMove(board), null);
  }
}
