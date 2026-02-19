package fr.univ.bordeaux.agonCore.agonElements;

/**
 * Represents a single action performed by a player on the Agon board.
 *
 * <p>A move is characterized by a starting position (source), a destination position,
 * the player's color, and the specific rank of the piece being moved. This class
 * encapsulates standard displacements, as well as the mandatory relocations that
 * occur after a piece is captured.</p>
 * * <p>In the case of a relocation, the {@code from} index is set to {@code -1},
 * indicating the piece is being moved from the relocation queue (off-board) back
 * onto the board's edge.</p>
 */
public class Move {

  /**
   * The starting tile index (0-120).
   * <p>A value of {@code -1} indicates a "Relocation Move" where a piece
   * returns to the board from the relocation reserve.</p>
   */
  private final int from;

  /** The destination tile index on the board (0-120). */
  private final int to;

  /** The color of the player performing the move. */
  private final Color color;

  /** The type of piece (Pawn or Queen) being moved. */
  private final PieceType pieceType;

  /**
   * Constructs a new Move for general displacement.
   *
   * @param from  The source tile index. Use {@code -1} for relocation from the reserve.
   * @param to    The destination tile index on the board.
   * @param color The {@link Color} of the player making the move.
   */
  public Move(int from, int to, Color color) {
    this.from = from;
    this.to = to;
    this.color = color;
    this.pieceType = null; // Default if not specified
  }

  /**
   * Constructs a new Move with explicit piece type identification.
   *
   * @param from      The source tile index (or {@code -1} for relocation).
   * @param to        The destination tile index.
   * @param color     The {@link Color} of the player.
   * @param pieceType The {@link PieceType} rank of the piece.
   */
  public Move(int from, int to, Color color, PieceType pieceType) {
    this.from = from;
    this.to = to;
    this.color = color;
    this.pieceType = pieceType;
  }

  /**
   * * Get the source of a Move
   * @return The source tile index. Returns {@code -1} if the move is a relocation.
   */
  public int getFrom() {
    return from;
  }

  /**
   * Get the destination of a Move
   * @return The destination tile index (0-120).
   */
  public int getTo() {
    return to;
  }

  /**
   * Get the color of the piece owner
   * @return The {@link Color} of the player who owns this move.
   */
  public Color getColor() {
    return color;
  }

  /**
   * Get the PieceType of the piece that has been moved
   * @return The {@link PieceType} being moved (Queen or Pawn).
   */
  public PieceType getPieceType() {
    return pieceType;
  }

  /**
   * Returns a string representation of the move for debugging purposes.
   *
   * @return A formatted string containing the source, destination, and color.
   */
  @Override
  public String toString() {
    return String.format("Move[from: %d, to: %d, color: %s, type: %s]", from, to, color, pieceType);
  }
}