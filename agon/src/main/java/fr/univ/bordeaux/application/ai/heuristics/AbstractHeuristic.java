package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.PieceType;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;

/**
 * Base abstract class for all heuristic evaluations in the Agon game AI.
 *
 * <p>This class implements the <b>Template Method pattern</b> to factorize the common logic of
 * iterating over the board, applying weights to pieces, and calculating the differential score (AI
 * score minus Opponent score).
 *
 * <p>Concrete subclasses only need to implement the specific {@link #getFactor(AgonBoard, int)}
 * method to define the raw metric (e.g., centrality, mobility).
 */
public abstract class AbstractHeuristic implements Heuristic {

  /** The weight multiplier applied to the raw factor for Pawn pieces. */
  protected int pawnWeight;

  /**
   * The weight multiplier applied to the raw factor for the Queen. Usually significantly higher
   * than {@link #pawnWeight} to reflect the Queen's importance.
   */
  protected int queenWeight;

  /**
   * Constructs a new heuristic with specific weights for pieces.
   *
   * @param pawnWeight The importance of the heuristic factor for a Pawn.
   * @param queenWeight The importance of the heuristic factor for the Queen.
   */
  public AbstractHeuristic(int pawnWeight, int queenWeight) {
    this.pawnWeight = pawnWeight;
    this.queenWeight = queenWeight;
  }

  public long getPawnWeight() {
    return pawnWeight;
  }

  public long getQueenWeight() {
    return queenWeight;
  }

  /**
   * Calculates the raw heuristic factor for a specific piece at a given position.
   *
   * <p>This method must be implemented by subclasses to provide the specific logic (e.g.,
   * calculating distance to center, counting legal moves, etc.).
   *
   * @param board The current board state.
   * @param index The index (0-120) of the cell to evaluate.
   * @return The raw value of the factor before weight application.
   */
  protected abstract long getFactor(AgonBoard board, int index);

  /**
   * {@inheritDoc}
   *
   * <p><b>Implementation Note:</b> This implementation uses the <b>Template Method</b> pattern. It
   * iterates over all 91 cells of the board, calculates a specific factor using {@link
   * #getFactor(AgonBoard, int)}, and applies the corresponding weight (Queen or Pawn).
   */
  @Override
  public long evaluate(AgonBoard board, Color aiColor) {
    long score = 0;
    for (int i = 0; i <= 120; i++) {
      PieceType piece = board.getPieceAt(i);
      if (piece == null) continue;
      long factor = getFactor(board, i);
      long pieceValue = 0;
      if (piece == PieceType.WHITE_QUEEN || piece == PieceType.BLACK_QUEEN) {
        pieceValue = factor * queenWeight;
      } else {
        pieceValue = factor * pawnWeight;
      }
      boolean isWhitePiece = (piece == PieceType.WHITE_PAWN || piece == PieceType.WHITE_QUEEN);
      if (aiColor == Color.WHITE) {
        score += (isWhitePiece ? pieceValue : -pieceValue);
      } else {
        score += (isWhitePiece ? -pieceValue : pieceValue);
      }
    }
    return score;
  }
}
