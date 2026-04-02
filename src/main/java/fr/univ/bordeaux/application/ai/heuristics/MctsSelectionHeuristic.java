package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.strategy.mcts.MctsNode;

/**
 * Dedicated interface for the selection phase of the Monte Carlo Tree Search (MCTS) algorithm.
 *
 * <p>This interface implements the <b>Strategy Pattern</b>, allowing the AI to dynamically switch
 * between different node evaluation methods during tree traversal. It is primarily used to swap
 * between the classic mathematical approach (UCT) and a predictive Machine Learning model.</p>
 */
public interface MctsSelectionHeuristic {

    /**
     * Evaluates a child node relative to its parent to determine its selection priority.
     *
     * @param parent The parent node (provides context, such as the total visit count).
     * @param child  The child node to be evaluated.
     * @param board  The game board representing the exact state of the child node.
     * @return The calculated selection score (the node with the highest score will be chosen).
     */
    double evaluateNode(MctsNode parent, MctsNode child, AgonBoard board);
}