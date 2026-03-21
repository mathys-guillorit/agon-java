package fr.univ.bordeaux.agoncore.history;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import java.util.List;

/**
 * Encapsulates all necessary data to track and revert a single game turn. *
 *
 * <p>In Agon, a single turn can trigger multiple physical displacements (e.g., a standard move
 * followed by one or more mandatory relocations). This class groups these actions together, along
 * with context about the piece and player, to ensure that the {@link
 * fr.univ.bordeaux.agoncore.bitboard.AgonBoard} can accurately restore previous states during undo
 * operations.
 */
public class HistoryInformations {

  /** The list of {@link Move} objects executed during this specific turn. */
  private final List<Move> moves;

  /** The type of the primary piece that initiated the turn. */
  private final PieceType pieceType;

  /** The color of the player who performed the turn. */
  private final Color color;

  /**
   * Constructs a new history record.
   *
   * @param moves A {@link List} of all moves performed (standard + relocations).
   * @param pieceType The {@link PieceType} of the main piece moved.
   * @param color The {@link Color} of the active player.
   */
  public HistoryInformations(List<Move> moves, PieceType pieceType, Color color) {
    this.moves = moves;
    this.pieceType = pieceType;
    this.color = color;
  }

  /**
   * Returns the sequence of moves that occurred during this turn. *
   *
   * <p>This list is ordered chronologically: the first element is the standard move, followed by
   * any capture-induced relocations.
   *
   * @return A {@link List} of {@link Move} objects.
   */
  public List<Move> getMoves() {
    return moves;
  }

  /**
   * Get the PieceType of the piece that has been moved
   *
   * @return The {@link PieceType} that was originally moved.
   */
  public PieceType getPieceType() {
    return pieceType;
  }

  /**
   * Get the color of the player who made the move
   *
   * @return The {@link Color} of the player who made the move.
   */
  public Color getColor() {
    return color;
  }
}
