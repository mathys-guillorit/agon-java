package fr.univ.bordeaux.agoncore.agonelements;

import java.util.Objects;

/**
 * Represents a single action performed by a player on the Agon board.
 *
 * <p>A move is defined by:
 *
 * <ul>
 *   <li>a starting position (source)
 *   <li>a destination position
 *   <li>the player's color
 *   <li>the type of piece being moved
 * </ul>
 *
 * <p>This class handles both:
 *
 * <ul>
 *   <li>standard movements on the board
 *   <li>relocation moves after a capture
 * </ul>
 *
 * <p>For relocation moves, the {@code from} index is set to {@code -1}. This indicates that the
 * piece comes from the relocation reserve (off-board) and is placed back onto the board edge.
 */
public class Move {

  /**
   * Source tile index.
   *
   * <p>A value of {@code -1} indicates a relocation move from the reserve.
   */
  private final int from;

  /** Destination tile index on the board. */
  private final int destination;

  /** Color of the player performing the move. */
  private final Color color;

  /** Type of the moved piece. */
  private final PieceType pieceType;

  /**
   * Constructs a move for a standard displacement.
   *
   * @param from source tile index, or {@code -1} for relocation
   * @param destination destination tile index
   * @param color player color
   */
  public Move(final int from, final int destination, final Color color) {
    this.from = from;
    this.destination = destination;
    this.color = color;
    this.pieceType = null;
  }

  /**
   * Constructs a move with an explicit piece type.
   *
   * @param from source tile index, or {@code -1} for relocation
   * @param destination destination tile index
   * @param color player color
   * @param pieceType moved piece type
   */
  public Move(final int from, final int destination, final Color color, final PieceType pieceType) {
    this.from = from;
    this.destination = destination;
    this.color = color;
    this.pieceType = pieceType;
  }

  /**
   * Returns the source tile index.
   *
   * @return source tile index, or {@code -1} for a relocation move
   */
  public int getFrom() {
    return from;
  }

  /**
   * Returns the destination tile index.
   *
   * @return destination tile index
   */
  public int getDestination() {
    return destination;
  }

  /**
   * Returns the player color.
   *
   * @return player color
   */
  public Color getColor() {
    return color;
  }

  /**
   * Returns the moved piece type.
   *
   * @return moved piece type
   */
  public PieceType getPieceType() {
    return pieceType;
  }

  /**
   * Indicates whether this move is a relocation move.
   *
   * @return {@code true} if the source index is {@code -1}, {@code false} otherwise
   */
  public boolean isRelocationMove() {
    return from == -1;
  }

  @Override
  public int hashCode() {
    return Objects.hash(from, destination, color, pieceType);
  }

  @Override
  public boolean equals(final Object obj) {
    boolean isEqual = false;

    if (this == obj) {
      isEqual = true;
    } else if (obj instanceof Move) {
      final Move move = (Move) obj;
      final boolean sameMainFields =
          this.from == move.from
              && this.destination == move.destination
              && this.color == move.color;

      if (this.pieceType != null && move.getPieceType() != null) {
        isEqual = sameMainFields && this.pieceType == move.pieceType;
      } else {
        isEqual = sameMainFields;
      }
    }

    return isEqual;
  }

  /**
   * Returns a string representation of the move.
   *
   * @return formatted move description
   */
  @Override
  public String toString() {
    return String.format(
        "Move[from: %d, to: %d, color: %s, type: %s]", from, destination, color, pieceType);
  }
}
