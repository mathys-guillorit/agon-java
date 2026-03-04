package fr.univ.bordeaux.agonCore.agonElements;

/**
 * Represents a single action performed by a player on the Agon board.
 * <p>
 * A move consists of a starting position, a destination position, and the color of the player
 * making the move. This class is used for standard movements, queen relocations, and pawn
 * relocations.
 * </p>
 */
public class Move {

  /**
   * The starting tile index (0-120). A value of -1 indicates a piece coming from the relocation
   * queue.
   */
  int from;
  /**
   * The destination tile index (0-120).
   */
  int to;
  /**
   * The color of the player performing the move.
   */
  Color color;

  /**
   * Constructs a new Move.
   *
   * @param from  The source tile index. Use {@code -1} if the piece is being relocated from the
   *              reserve.
   * @param to    The destination tile index on the board.
   * @param color The {@link Color} of the player making the move.
   */
  public Move(int from, int to, Color color) {
    this.from = from;
    this.to = to;
    this.color = color;
  }

  public int getFrom() {
    return from;
  }
  public int getTo() {
    return to;
  }
  public Color getColor() {
    return color;
  }
  }