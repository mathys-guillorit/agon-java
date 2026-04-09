package fr.univ.bordeaux.application.ai.strategy.mcts;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.heuristics.MctsSelectionHeuristic;
import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAi;
import fr.univ.bordeaux.technical.utils.GameLogger;
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

  /** Random number generator for MCTS rollouts. */
  private final Random random = new Random();

  /** The heuristic strategy used for evaluating and selecting nodes. */
  private final MctsSelectionHeuristic selHeur;

  /**
   * Constructs a new MCTS strategy instance.
   *
   * @param color The color played by this AI agent.
   * @param selHeur The specific heuristic used for node selection.
   * @param timeLimit The calculation time limit in seconds.
   */
  public MctsStrategy(
      final Color color, final MctsSelectionHeuristic selHeur, final int timeLimit) {
    super(null, color);
    this.timeLimit = timeLimit * 1000L;
    this.selHeur = selHeur;
  }

  @Override
  protected Move computeMove(final AgonBoard board) {
    this.nodeCount = 0;
    final List<Move> initMoves = board.generateLegalMoves(this.color);
    final Move result;

    if (initMoves.isEmpty()) {
      result = null;
    } else if (initMoves.size() == 1) {
      result = initMoves.get(0);
    } else {
      final MctsNode root = new MctsNode(null, null, this.color, initMoves);
      this.nodeCount++;

      while (isTimeRemaining()) {
        runMctsIteration(root, board);
      }

      final MctsNode bestChild = getBestChild(root, board);
      result = bestChild != null ? bestChild.getMove() : initMoves.get(0);
    }

    return result;
  }

  /**
   * Executes a single complete iteration of the MCTS algorithm (Selection, Expansion, Simulation,
   * Backpropagation).
   */
  private void runMctsIteration(final MctsNode root, final AgonBoard board) {
    int depth = 0;
    MctsNode node = root;

    while (node.isFullyExpanded() && !node.isLeaf()) {
      node = getBestChild(node, board);
      board.applyMove(node.getMove());
      depth++;
    }

    if (!node.isFullyExpanded()) {
      final Move untried = node.popRandomUntriedMove(random);
      board.applyMove(untried);
      depth++;

      final Color nextP = (node.getPlayerToMove() == Color.WHITE) ? Color.BLACK : Color.WHITE;
      final List<Move> newMoves = board.generateLegalMoves(nextP);

      final MctsNode newNode = new MctsNode(node, untried, nextP, newMoves);
      node.addChild(newNode);
      node = newNode;
      this.nodeCount++;

      GameLogger.debug("Node count :  " + this.nodeCount);
    }

    final int rollouts = simulate(board, node.getPlayerToMove());
    final Color winner = checkWinner(board);

    backpropagate(node, winner);

    final int totalUndos = depth + rollouts;
    for (int i = 0; i < totalUndos; i++) {
      board.undoMove();
    }
  }

  /**
   * Performs a random simulation (rollout) from the current board state until a terminal state or
   * depth limit is reached.
   */
  private int simulate(final AgonBoard board, final Color startTurn) {
    int rolloutMoves = 0;
    Color turn = startTurn;
    Color winner = checkWinner(board);

    while (winner == null && rolloutMoves < 50 && isTimeRemaining()) {
      final List<Move> moves = board.generateLegalMoves(turn);
      if (moves.isEmpty()) {
        break;
      }

      final Move randMove = moves.get(random.nextInt(moves.size()));
      board.applyMove(randMove);
      rolloutMoves++;

      turn = (turn == Color.WHITE) ? Color.BLACK : Color.WHITE;
      winner = checkWinner(board);
    }
    return rolloutMoves;
  }

  /** Backpropagates the simulation result up the tree to update node statistics. */
  private void backpropagate(final MctsNode startNode, final Color winner) {
    MctsNode tempNode = startNode;
    while (tempNode != null) {
      final double score;
      if (this.color.equals(winner)) {
        score = 1.0;
      } else if (winner == null) {
        score = 0.5;
      } else {
        score = 0.0;
      }

      tempNode.updateStats(score);
      tempNode = tempNode.getParent();
    }
  }

  private MctsNode getBestChild(final MctsNode node, final AgonBoard board) {
    MctsNode bestChild = null;
    double bestValue = Double.NEGATIVE_INFINITY;

    for (final MctsNode child : node.getChildren()) {
      board.applyMove(child.getMove());

      final double nodeValue = this.selHeur.evaluateNode(node, child, board);

      board.undoMove();

      if (nodeValue > bestValue) {
        bestValue = nodeValue;
        bestChild = child;
      }
    }
    return bestChild;
  }

  private Color checkWinner(final AgonBoard board) {
    final Color result;
    if (board.isGameWon(Color.WHITE)) {
      result = Color.WHITE;
    } else if (board.isGameWon(Color.BLACK)) {
      result = Color.BLACK;
    } else {
      result = null;
    }
    return result;
  }
}
