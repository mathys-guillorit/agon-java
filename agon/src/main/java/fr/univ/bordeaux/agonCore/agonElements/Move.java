package fr.univ.bordeaux.agonCore.agonElements;

/**
 * Represents a single action performed by a player on the Agon board.
 * <p>
 * A move consists of a starting position, a destination position, and the
 * color of the player making the move. This class is used for standard
 * movements, queen relocations, and pawn relocations.
 * </p>
 */
public class Move {
  /** The starting tile index (0-120). A value of -1 indicates a piece coming from the relocation queue. */
  int from;
  /** The destination tile index (0-120). */
  int to;
  /** The color of the player performing the move. */
  Color color;

  /**
   * Constructs a new Move.
   *
   * @param from  The source tile index. Use {@code -1} if the piece is being relocated from the reserve.
   * @param to    The destination tile index on the board.
   * @param color The {@link Color} of the player making the move.
   */
  public Move(int from, int to, Color color) {
    this.from = from;
    this.to = to;
    this.color = color;
  }

  /**
   * Returns the starting position of the move.
   * * @return The source index, or -1 for relocations.
   */
  public int getFrom() {
    return from;
  }

  /**
   * Returns the destination position of the move.
   * * @return The target index.
   */
  public int getTo() {
    return to;
  }

  /**
   * Returns the color of the player who owns this move.
   * * @return The player's {@link Color}.
   */
  public Color getColor() {
    return color;
  }

  /**
   * Returns a string representation of the move for debugging purposes.
   * * @return A formatted string containing source, destination, and color.
   */
  @Override
  public String toString() {
    return "departure: " + from + " arrival: " + to + " color: " + color;
  }
}