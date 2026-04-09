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
  private final boolean itDeepening;

  /** Flag to immediately halt the recursive search when time runs out. */
  private boolean timeoutReached;

  /**
   * Constructs a Minimax strategy with Iterative Deepening and time management.
   *
   * @param heuristic The evaluation function used for leaf nodes.
   * @param color The color played by this AI.
   * @param maxDepth The absolute maximum depth limit for the recursion.
   * @param itDeepening {@code true} to enable Iterative Deepening, {@code false} for
   * standard Minimax.
   * @param timeLimitSeconds The maximum allowed calculation time per move in seconds.
   */
  public MinimaxStrategy(
          final Heuristic heuristic,
          final Color color,
          final int maxDepth,
          final boolean itDeepening,
          final int timeLimitSeconds) {
    super(heuristic, color);
    this.maxDepth = maxDepth;
    this.itDeepening = itDeepening;
    this.timeLimit = timeLimitSeconds * 1000L;
  }

  @Override
  protected Move computeMove(final AgonBoard board) {
    final Move result;
    this.timeoutReached = false;

    final List<Move> legalMoves = board.generateLegalMoves(this.color);
    if (legalMoves.isEmpty()) {
      result = null;
    } else if (!this.itDeepening) {
      result = runMinimaxForDepth(board, legalMoves, this.maxDepth);
    } else {
      Move absoluteBestMove = legalMoves.getFirst();
      for (int currentDepth = 1; currentDepth <= this.maxDepth; currentDepth++) {
        final Move bestMoveForCurrentDepth = runMinimaxForDepth(board, legalMoves, currentDepth);

        if (!this.timeoutReached && bestMoveForCurrentDepth != null) {
          absoluteBestMove = bestMoveForCurrentDepth;
        } else {
          break;
        }
      }
      result = absoluteBestMove;
    }
    return result;
  }

  private Move runMinimaxForDepth(final AgonBoard board, final List<Move> legalMoves, final int targetDepth) {
    Move bestMove = legalMoves.getFirst();
    long maxScore = Long.MIN_VALUE;
    long alpha = Long.MIN_VALUE;
    final long beta = Long.MAX_VALUE;

    for (final Move move : legalMoves) {
      if (!this.timeoutReached && !isTimeRemaining()) {
        this.timeoutReached = true;
      }
      if (this.timeoutReached) {
        break;
      }

      board.applyMove(move);
      this.nodeCount++;

      final long score = minimax(board, targetDepth - 1, false, alpha, beta);

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

  private long minimax(
          final AgonBoard board, final int depth, final boolean isMaximizingPlayer, final long alpha, final long beta) {
    final long result;

    GameLogger.debug("Node count :  " + this.nodeCount + "at depth : " + depth);

    if (!this.timeoutReached && !isTimeRemaining()) {
      this.timeoutReached = true;
    }

    if (this.timeoutReached) {
      result = 0;
    } else {
      final Color opponentColor = (this.color == Color.WHITE) ? Color.BLACK : Color.WHITE;

      if (board.isGameWon(this.color)) {
        result = 1_000_000L + depth;
      } else if (board.isGameWon(opponentColor)) {
        result = -1_000_000L - depth;
      } else if (depth == 0) {
        result = this.heuristic.evaluate(board, this.color);
      } else if (isMaximizingPlayer) {
        result = evaluateMaximizing(board, depth, alpha, beta);
      } else {
        result = evaluateMinimizing(board, depth, alpha, beta, opponentColor);
      }
    }
    return result;
  }

  private long evaluateMaximizing(
          final AgonBoard board, final int depth, final long alpha, final long beta) {
    final long result;
    final List<Move> moves = board.generateLegalMoves(this.color);

    if (moves.isEmpty()) {
      result = this.heuristic.evaluate(board, this.color);
    } else {
      long maxEval = Long.MIN_VALUE;
      long currentAlpha = alpha;

      for (final Move move : moves) {
        board.applyMove(move);
        this.nodeCount++;

        final long eval = minimax(board, depth - 1, false, currentAlpha, beta);

        board.undoMove();

        if (this.timeoutReached) {
          break;
        }

        maxEval = Math.max(maxEval, eval);
        currentAlpha = Math.max(currentAlpha, eval);
        if (beta <= currentAlpha) {
          break;
        }
      }

      if (this.timeoutReached) {
        result = 0;
      } else {
        result = maxEval;
      }
    }
    return result;
  }

  private long evaluateMinimizing(
          final AgonBoard board, final int depth, final long alpha, final long beta, final Color opponentColor) {
    final long result;
    final List<Move> moves = board.generateLegalMoves(opponentColor);

    if (moves.isEmpty()) {
      result = this.heuristic.evaluate(board, this.color);
    } else {
      long minEval = Long.MAX_VALUE;
      long currentBeta = beta;

      for (final Move move : moves) {
        board.applyMove(move);
        this.nodeCount++;

        final long eval = minimax(board, depth - 1, true, alpha, currentBeta);

        board.undoMove();

        if (this.timeoutReached) {
          break;
        }

        minEval = Math.min(minEval, eval);
        currentBeta = Math.min(currentBeta, eval);
        if (currentBeta <= alpha) {
          break;
        }
      }

      if (this.timeoutReached) {
        result = 0;
      } else {
        result = minEval;
      }
    }
    return result;
  }
}