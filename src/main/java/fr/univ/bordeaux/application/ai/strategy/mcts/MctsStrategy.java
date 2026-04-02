package fr.univ.bordeaux.application.ai.strategy.mcts;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.heuristics.Heuristic;
import fr.univ.bordeaux.application.ai.heuristics.MctsSelectionHeuristic;
import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAi;
import java.util.List;
import java.util.Random;

/**
 * Implementation of the Monte Carlo Tree Search (MCTS) algorithm.
 *
 * <p>Inherits time management and performance metrics from {@link AbstractAgonAi}. This strategy
 * uses the UCT (Upper Confidence bounds applied to Trees) algorithm to balance exploration and
 * exploitation during the search process.
 */
public class MctsStrategy extends AbstractAgonAi {

  private final Random random = new Random();

  /**  The heuristic strategy specifically used for evaluating and selecting nodes
   * during the tree traversal phase.
   */
  private final MctsSelectionHeuristic selectionHeuristic;

  /**
   * Constructs a new MCTS strategy instance.
   *
   * @param color              The color played by this AI agent.
   * @param selectionHeuristic The specific heuristic used for node selection (e.g., UCT or ML).
   */
  public MctsStrategy(Color color, MctsSelectionHeuristic selectionHeuristic, int timeLimit) {
    super(null, color);
    this.setTimeLimit(timeLimit*1000L);
    this.selectionHeuristic = selectionHeuristic;
  }

  /**
   * Computes the best move for the current board state using the MCTS algorithm.
   *
   * <p>This method iteratively performs the four core phases of MCTS: Selection, Expansion,
   * Simulation (Rollout), and Backpropagation, until the allocated time limit is exhausted.
   *
   * @param board The current state of the game board.
   * @return The calculated optimal {@link Move}, or {@code null} if no legal moves are available.
   */
  @Override
  protected Move computeMove(AgonBoard board) {
    this.nodeCount = 0;

    List<Move> initialLegalMoves = board.generateLegalMoves(this.color);

    if (initialLegalMoves.isEmpty()) return null;
    if (initialLegalMoves.size() == 1) return initialLegalMoves.get(0);

    MctsNode root = new MctsNode(null, null, this.color, initialLegalMoves);
    this.nodeCount++;

    while (isTimeRemaining()) {
      int depth = 0;
      MctsNode node = root;

      while (node.isFullyExpanded() && !node.isLeaf()) {
        node = getBestChild(node, board);
        board.applyMove(node.getMove());
        depth++;
      }

      if (!node.isFullyExpanded()) {
        Move untriedMove = node.popRandomUntriedMove(random);
        board.applyMove(untriedMove);
        depth++;

        Color nextPlayer = (node.getPlayerToMove() == Color.WHITE) ? Color.BLACK : Color.WHITE;
        List<Move> newLegalMoves = board.generateLegalMoves(nextPlayer);

        MctsNode newNode = new MctsNode(node, untriedMove, nextPlayer, newLegalMoves);
        node.addChild(newNode);
        node = newNode;
        this.nodeCount++;
      }

      Color winner = checkWinner(board);
      int rolloutMoves = 0;
      Color turn = node.getPlayerToMove();

      while (winner == null && rolloutMoves < 50) {
        List<Move> moves = board.generateLegalMoves(turn);
        if (moves.isEmpty()) break;

        Move randomMove = moves.get(random.nextInt(moves.size()));
        board.applyMove(randomMove);
        rolloutMoves++;

        turn = (turn == Color.WHITE) ? Color.BLACK : Color.WHITE;
        winner = checkWinner(board);
      }

      MctsNode tempNode = node;
      while (tempNode != null) {
        double score = 0.0;
        if (winner == this.color) {
          score = 1.0;
        } else if (winner == null) {
          score = 0.5;
        }

        tempNode.updateStats(score);
        tempNode = tempNode.getParent();
      }

      for (int i = 0; i < depth + rolloutMoves; i++) {
        board.undoMove();
      }
    }

    MctsNode bestChild = getBestChild(root, board);
    return bestChild != null ? bestChild.getMove() : initialLegalMoves.get(0);
  }

  /**
   * Evaluates and selects the best child node using the injected {@link MctsSelectionHeuristic}.
   *
   * @param node  The parent node whose children are to be evaluated.
   * @param board The current game board state (matching the parent node's state).
   * @return The child {@link MctsNode} with the highest computed selection score.
   */
  private MctsNode getBestChild(MctsNode node, AgonBoard board) {
    MctsNode bestChild = null;
    double bestValue = Double.NEGATIVE_INFINITY;

    for (MctsNode child : node.getChildren()) {
      board.applyMove(child.getMove());

      double nodeValue = this.selectionHeuristic.evaluateNode(node, child, board);

      board.undoMove();

      if (nodeValue > bestValue) {
        bestValue = nodeValue;
        bestChild = child;
      }
    }
    return bestChild;
  }

  /**
   * Checks the current board state to determine if a victory condition has been met.
   *
   * @param board The current game board state.
   * @return The winning {@link Color}, or {@code null} if there is no winner yet.
   */
  private Color checkWinner(AgonBoard board) {
    if (board.isGameWon(Color.WHITE)) return Color.WHITE;
    if (board.isGameWon(Color.BLACK)) return Color.BLACK;
    return null;
  }
}
