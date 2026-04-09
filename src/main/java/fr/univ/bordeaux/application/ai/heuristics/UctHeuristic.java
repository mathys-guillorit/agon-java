package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.strategy.mcts.MctsNode;

/**
 * Implementation of the UCB1 (Upper Confidence Bound 1) formula for MCTS node selection.
 *
 * <p>This heuristic balances <b>exploitation</b> (choosing nodes with a high historical win rate)
 * and <b>exploration</b> (choosing nodes that have been visited infrequently).
 */
public class UctHeuristic implements MctsSelectionHeuristic {

  /** The exploration parameter (often denoted as 'C'), determining the weight of exploration. */
  private final double explorationParam;

  /**
   * Constructs a new UCT heuristic with the specified exploration parameter.
   *
   * @param explorationParam The constant used to scale the exploration term (typically √2).
   */
  public UctHeuristic(double explorationParam) {
    this.explorationParam = explorationParam;
  }

  @Override
  public double evaluateNode(MctsNode parent, MctsNode child, AgonBoard board) {
    if (child.getVisitCount() == 0) {
      return Double.MAX_VALUE;
    }

    double exploit = child.getWinScore() / (double) child.getVisitCount();
    double explore =
        explorationParam
            * Math.sqrt(Math.log(parent.getVisitCount()) / (double) child.getVisitCount());

    return exploit + explore;
  }
}
