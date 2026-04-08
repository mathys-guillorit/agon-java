package fr.univ.bordeaux.application.ai.strategy.minimax;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.application.ai.heuristics.Heuristic;
import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAi;
import fr.univ.bordeaux.technical.utils.GameLogger;
import java.util.List;

/**
 * A concrete AI strategy implementing the <b>Minimax algorithm with Alpha-Beta Pruning</b> and
 * optional <b>Iterative Deepening</b>.
 *
 * <p>This strategy constructs a game tree to a specified depth and evaluates the terminal nodes
 * using the injected {@link Heuristic}.
 *
 * <p>If Iterative Deepening is enabled, the algorithm searches progressively deeper (depth 1, then
 * 2, etc.) until the maximum depth is reached or the time limit is exceeded. This ensures a valid
 * move is always ready even if the computation is interrupted.
 */
public class MinimaxStrategy extends AbstractAgonAi {

  /** The maximum depth limit for the game tree search. */
  private final int maxDepth;

  /** Flag indicating whether to use iterative deepening instead of a single deep search. */
  private final boolean iterativeDeepening;

  /** Flag to immediately halt the recursive search when time runs out. */
  private volatile boolean timeoutReached = false;

  /**
   * Constructs a Minimax strategy with Iterative Deepening and time management.
   *
   * @param heuristic The evaluation function used for leaf nodes.
   * @param color The color played by this AI.
   * @param maxDepth The absolute maximum depth limit for the recursion.
   * @param iterativeDeepening {@code true} to enable Iterative Deepening, {@code false} for
   *     standard Minimax.
   * @param timeLimitSeconds The maximum allowed calculation time per move in seconds.
   */
  public MinimaxStrategy(
      Heuristic heuristic,
      Color color,
      int maxDepth,
      boolean iterativeDeepening,
      int timeLimitSeconds) {
    super(heuristic, color);
    this.maxDepth = maxDepth;
    this.iterativeDeepening = iterativeDeepening;
    this.setTimeLimit(timeLimitSeconds * 1000L);
  }

  /**
   * Computes the best move for the current board state.
   *
   * <p>Initializes the search and manages the overall time limit. If Iterative Deepening is
   * disabled, it performs a single search directly to {@code maxDepth}. If enabled, it loops
   * through progressively deeper searches, retaining the best fully completed result before the
   * time runs out.
   *
   * @param board The current game board.
   * @return The optimal {@link Move} found within the constraints, or {@code null} if no legal
   *     moves exist.
   */
  @Override
  protected Move computeMove(AgonBoard board) {
    this.timeoutReached = false;

    List<Move> legalMoves = board.generateLegalMoves(this.color);
    if (legalMoves.isEmpty()) {
      return null;
    }

    Move absoluteBestMove = legalMoves.getFirst();

    if (!this.iterativeDeepening) {
      return runMinimaxForDepth(board, legalMoves, this.maxDepth);
    }

    for (int currentDepth = 1; currentDepth <= this.maxDepth; currentDepth++) {

      Move bestMoveForCurrentDepth = runMinimaxForDepth(board, legalMoves, currentDepth);

      if (!this.timeoutReached && bestMoveForCurrentDepth != null) {
        absoluteBestMove = bestMoveForCurrentDepth;
      } else {
        break;
      }
    }

    return absoluteBestMove;
  }

  /**
   * Executes the root layer of the Minimax algorithm for a specific target depth.
   *
   * <p>Iterates over all initially available legal moves and evaluates them using the recursive
   * {@link #minimax} method. It keeps track of the best move found and immediately breaks the
   * evaluation loop if a timeout occurs.
   *
   * @param board The current game board state.
   * @param legalMoves The list of valid moves available from the root position.
   * @param targetDepth The depth limit for this specific search run.
   * @return The best {@link Move} evaluated at the given depth.
   */
  private Move runMinimaxForDepth(AgonBoard board, List<Move> legalMoves, int targetDepth) {
    Move bestMove = legalMoves.getFirst();
    long maxScore = Long.MIN_VALUE;
    long alpha = Long.MIN_VALUE;
    long beta = Long.MAX_VALUE;

    for (Move move : legalMoves) {
      if (!this.timeoutReached && !isTimeRemaining()) {
        this.timeoutReached = true;
      }
      if (this.timeoutReached) {
        break;
      }

      board.applyMove(move);
      this.nodeCount++;

      long score = minimax(board, targetDepth - 1, false, alpha, beta);

      board.undoMove();

      if (this.timeoutReached) {
        break;
      }

      if (score > maxScore) {
        maxScore = score;
        bestMove = move;
      }
      alpha = Math.max(alpha, score);
    }
    return bestMove;
  }

  /**
   * Recursive Minimax function with Alpha-Beta pruning and timeout checks.
   *
   * <p>Explores the game tree by alternating between the maximizing player (the AI) and the
   * minimizing player (the opponent). It halts early and evaluates the board if a terminal state is
   * reached (win, loss, depth 0, or no legal moves available).
   *
   * @param board The simulated game board at the current node.
   * @param depth The remaining depth to explore before applying the heuristic.
   * @param isMaximizingPlayer {@code true} if evaluating the AI's optimal move, {@code false} for
   *     the opponent's.
   * @param alpha The best guaranteed score for the maximizing player.
   * @param beta The best guaranteed score for the minimizing player.
   * @return The heuristic evaluation score of the current branch.
   */
  private long minimax(
      AgonBoard board, int depth, boolean isMaximizingPlayer, long alpha, long beta) {
    GameLogger.debug("Node count :  " + this.nodeCount + "at depth : " + depth);

    if (!this.timeoutReached && !isTimeRemaining()) {
      this.timeoutReached = true;
    }

    if (this.timeoutReached) {
      return 0;
    }

    Color opponentColor = (this.color == Color.WHITE) ? Color.BLACK : Color.WHITE;

    if (board.isGameWon(this.color)) {
      return 1000000L + depth;
    }
    if (board.isGameWon(opponentColor)) {
      return -1000000L - depth;
    }
    if (depth == 0) {
      return this.heuristic.evaluate(board, this.color);
    }

    if (isMaximizingPlayer) {
      List<Move> moves = board.generateLegalMoves(this.color);
      if (moves.isEmpty()) {
        return this.heuristic.evaluate(board, this.color);
      }
      long maxEval = Long.MIN_VALUE;
      for (Move move : moves) {
        board.applyMove(move);
        this.nodeCount++;

        long eval = minimax(board, depth - 1, false, alpha, beta);

        board.undoMove();

        if (this.timeoutReached) {
          return 0;
        }

        maxEval = Math.max(maxEval, eval);
        alpha = Math.max(alpha, eval);
        if (beta <= alpha) {
          break;
        }
      }
      return maxEval;
    } else {
      List<Move> moves = board.generateLegalMoves(opponentColor);
      if (moves.isEmpty()) {
        return this.heuristic.evaluate(board, this.color);
      }
      long minEval = Long.MAX_VALUE;
      for (Move move : moves) {
        board.applyMove(move);
        this.nodeCount++;

        long eval = minimax(board, depth - 1, true, alpha, beta);

        board.undoMove();

        if (this.timeoutReached) {
          return 0;
        }

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
