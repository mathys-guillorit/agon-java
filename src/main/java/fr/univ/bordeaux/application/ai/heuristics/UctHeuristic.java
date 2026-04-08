package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.strategy.mcts.MctsNode;

/**
 * Implementation of the UCB1 (Upper Confidence Bound 1) formula for MCTS node selection.
 *
 * <p>This heuristic balances <b>exploitation</b> (choosing nodes with a high historical win rate)
 * and <b>exploration</b> (choosing nodes that have been visited infrequently).</p>
 */
public class UctHeuristic implements MctsSelectionHeuristic {

    /** The exploration parameter (often denoted as 'C'), determining the weight of exploration. */
    private final double explorationParam;

    /**
     * Constructs a new UCT heuristic with the specified exploration parameter.
     *
     * @param explorationParam The constant used to scale the exploration term (typically √2).
     */
    public UctHeuristic(final double explorationParam) {
        this.explorationParam = explorationParam;
    }

    @Override
    public double evaluateNode(final MctsNode parent, final MctsNode child, AgonBoard board) {
        final double retVal;
        if (child.getVisitCount() == 0) {
            retVal = Double.MAX_VALUE;
        }else{
            final double exploit = child.getWinScore() / child.getVisitCount();
            final double explore = explorationParam * Math.sqrt(Math.log(parent.getVisitCount()) /  child.getVisitCount());
            retVal = explore + exploit;
        }
        return retVal;
    }
}