package fr.univ.bordeaux.application.ai.strategy.minimax;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.heuristics.Heuristic;
import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAI;
import java.util.List;

/**
 * A concrete AI strategy implementing the <b>Minimax algorithm with Alpha-Beta Pruning</b>.
 *
 * <p>This strategy constructs a game tree to a specified depth and evaluates the terminal nodes
 * using the injected {@link Heuristic}. It assumes the opponent plays optimally (zero-sum game).
 *
 * <p><b>Key optimization:</b> Although named Minimax, this implementation includes <b>Alpha-Beta
 * Pruning</b>. This technique stops evaluating a move when at least one possibility has been found
 * that proves the move to be worse than a previously examined move. This significantly reduces the
 * number of nodes visited ({@code nodeCount}).
 */
public class MinimaxStrategy extends AbstractAgonAI {

  /**
   * The maximum depth of the game tree search.
   *
   * <p>A depth of 1 means looking only at immediate moves. Higher depths increase the AI's
   * foresight but exponentially increase calculation time.
   */
  private final int maxDepth;

  /**
   * Constructs a Minimax strategy with a fixed search depth.
   *
   * @param heuristic The evaluation function used for leaf nodes.
   * @param maxDepth The depth limit for the recursion (e.g., 3 or 4 for reasonable performance).
   */
  public MinimaxStrategy(Heuristic heuristic, Color color, int maxDepth) {
    super(heuristic, color);
    this.maxDepth = maxDepth;
  }

  /**
   * Initiates the Minimax algorithm to find the best move from the current board state.
   *
   * <p>This method acts as the <b>Root Maximizer</b>. It generates the initial legal moves, calls
   * the recursive {@link #minimax} function for each, and selects the move with the highest
   * returned score.
   *
   * @param board The current game board.
   * @return The move that leads to the highest heuristic score, or {@code null} if no moves are
   *     available.
   */
  @Override
  protected Move computeMove(AgonBoard board) {
    List<Move> legalMoves = board.generateLegalMoves(this.color);
    if (legalMoves.isEmpty()) return null;
    Move bestMove = legalMoves.getFirst();
    long maxScore = Long.MIN_VALUE;
    long alpha = Long.MIN_VALUE;
    long beta = Long.MAX_VALUE;
    for (Move move : legalMoves) {
      board.applyMove(move);
      this.nodeCount++;
      long score = minimax(board, this.maxDepth - 1, false, alpha, beta);
      board.undoMove();
      if (score > maxScore) {
        maxScore = score;
        bestMove = move;
      }
      alpha = Math.max(alpha, score);
    }
    return bestMove;
  }

  /**
   * Recursive Minimax function with Alpha-Beta pruning.
   *
   * <p>Traverses the game tree, alternating between maximizing (AI) and minimizing (Opponent)
   * layers.
   *
   * @param board The board state at the current node.
   * @param depth The remaining depth to explore. Returns heuristic value when 0.
   * @param isMaximizingPlayer {@code true} if it's the AI's turn, {@code false} for the opponent.
   * @param alpha The best value that the maximizer can guarantee so far.
   * @param beta The best value that the minimizer can guarantee so far.
   * @return The heuristic score of the board at this node (propagated from the leaves).
   */
  private long minimax(
      AgonBoard board, int depth, boolean isMaximizingPlayer, long alpha, long beta) {
    Color opponentColor = (this.color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    if (board.isGameWon(this.color)) {
      return 1000000L + depth;
    }
    if (board.isGameWon(opponentColor)) {
      return -1000000L - depth;
    }
    if (depth == 0) {
      return heuristic.evaluate(board, this.color);
    }
    if (isMaximizingPlayer) {
      List<Move> moves = board.generateLegalMoves(this.color);
      long maxEval = Long.MIN_VALUE;
      for (Move move : moves) {
        board.applyMove(move);
        this.nodeCount++;
        long eval = minimax(board, depth - 1, false, alpha, beta);
        board.undoMove();
        maxEval = Math.max(maxEval, eval);
        alpha = Math.max(alpha, eval);
        if (beta <= alpha) {
          break;
        }
      }
      return maxEval;
    } else {
      List<Move> moves = board.generateLegalMoves(opponentColor);
      long minEval = Long.MAX_VALUE;
      for (Move move : moves) {
        board.applyMove(move);
        this.nodeCount++;
        long eval = minimax(board, depth - 1, true, alpha, beta);
        board.undoMove();
        minEval = Math.min(minEval, eval);
        beta = Math.min(beta, eval);
        if (beta <= alpha) {
          break;
        }
      }
      return minEval;
    }
  }
}
