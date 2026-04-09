package fr.univ.bordeaux.application.ai.strategy.mcts;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a single node in the Monte Carlo Tree Search (MCTS) algorithm.
 *
 * <p>This class stores game state statistics, tree topology (parent and children relationships),
 * and the specific move that led to this node's creation. It is designed to be memory-efficient by
 * strictly holding the move applied rather than cloning the entire board state.
 */
public class MctsNode {

  /**
   * The parent node of this node in the MCTS tree. This is {@code null} if this node is the root of
   * the tree.
   */
  private final MctsNode parent;

  /** The list of expanded child nodes originating from this node. */
  private final List<MctsNode> children;

  /**
   * The specific move that was applied to the parent's state to reach this node's state. This is
   * {@code null} for the root node.
   */
  private final Move move;

  /** The color of the player whose turn it is to make a move from this node's state. */
  private final Color playerToMove;

  /** A list of valid, legal moves from this state that have not yet been explored (expanded). */
  private final List<Move> untriedMoves;

  /** The total number of times this node has been visited during the simulation phases. */
  private int visitCount;

  /**
   * The accumulated score representing the success rate of simulations passing through this node.
   * Typically incremented by 1.0 for a win, 0.5 for a draw, and 0.0 for a loss.
   */
  private double winScore;

  /**
   * Constructs a new MCTS Node.
   *
   * @param parent The parent node ({@code null} if this is the root node).
   * @param move The move applied to reach this node's state.
   * @param playerToMove The player whose turn it is to play next.
   * @param legalMoves The complete list of legal moves available from this current state.
   */
  public MctsNode(final MctsNode parent, final Move move, final Color playerToMove, final List<Move> legalMoves) {
    this.parent = parent;
    this.move = move;
    this.playerToMove = playerToMove;
    this.children = new ArrayList<>();
    this.untriedMoves = new ArrayList<>(legalMoves);
    this.visitCount = 0;
    this.winScore = 0.0;
  }

  /**
   * Updates the node's statistics after a simulation rollout is completed.
   *
   * <p>This method increments the visit count and adds the rollout score to the total win score.
   *
   * @param score The outcome of the simulation (e.g., 1.0 for a win, 0.0 for a loss, 0.5 for a
   *     draw).
   */
  public void updateStats(final double score) {
    this.visitCount++;
    this.winScore += score;
  }

  /**
   * Pops a random untried move from the internal list of unexplored moves.
   *
   * <p>This method is utilized primarily during the Expansion phase of the MCTS algorithm.
   *
   * @param random A {@link Random} instance used to select the move uniformly.
   * @return A randomly selected {@link Move} which is concurrently removed from the untried list,
   *     or {@code null} if no untried moves remain.
   */
  public Move popRandomUntriedMove(final Random random) {
    final Move retMove;
    if (untriedMoves.isEmpty()) {
      retMove = null;
    }else{
      final int index = random.nextInt(untriedMoves.size());
      retMove = untriedMoves.remove(index);
    }
    return retMove;
  }

  /**
   * Adds a newly expanded child node to this node's list of children.
   *
   * @param child The expanded {@link MctsNode} to attach to this node.
   */
  public void addChild(final MctsNode child) {
    this.children.add(child);
  }

  /**
   * Checks if all possible legal moves from this node have been expanded into child nodes.
   *
   * @return {@code true} if there are no untried moves left; {@code false} otherwise.
   */
  public boolean isFullyExpanded() {
    return this.untriedMoves.isEmpty();
  }

  /**
   * Checks if this node is currently a leaf node (i.e., it has no expanded children).
   *
   * @return {@code true} if the node has no children; {@code false} otherwise.
   */
  public boolean isLeaf() {
    return this.children.isEmpty();
  }

  /**
   * Retrieves the parent node.
   *
   * @return The parent {@link MctsNode}, or {@code null} if this is the root node.
   */
  public MctsNode getParent() {
    return parent;
  }

  /**
   * Retrieves the list of expanded children nodes.
   *
   * @return A {@link List} containing all child {@link MctsNode} objects.
   */
  public List<MctsNode> getChildren() {
    return children;
  }

  /**
   * Retrieves the move that led to this node's creation.
   *
   * @return The originating {@link Move}, or {@code null} if this is the root node.
   */
  public Move getMove() {
    return move;
  }

  /**
   * Retrieves the player whose turn it is to act from this node.
   *
   * @return The {@link Color} of the active player.
   */
  public Color getPlayerToMove() {
    return playerToMove;
  }

  /**
   * Retrieves the total number of simulation visits this node has received.
   *
   * @return The visit count as an integer.
   */
  public int getVisitCount() {
    return visitCount;
  }

  /**
   * Retrieves the total accumulated win score from all simulations passing through this node.
   *
   * @return The win score as a double.
   */
  public double getWinScore() {
    return winScore;
  }
}
