package fr.univ.bordeaux.agonCore.agonElements;
/**
 * Represents the different types of pieces available in the Agon game.
 * * <p>Each piece type is defined by its owning color and its rank (Queen or Pawn).
 * This enum is used throughout the board logic to simplify piece identification
 * and movement rules without manual conditional checks.</p>
 */
public enum PieceType {
    WHITE_QUEEN(Color.WHITE, true),
    BLACK_QUEEN(Color.BLACK, true),
    WHITE_PAWN(Color.WHITE, false),
    BLACK_PAWN(Color.BLACK, false);

    private final Color color;
    private final boolean isQueen;

  /**
   * Internal constructor for piece type constants.
   * * @param color   The owner's color.
   * @param isQueen True if the piece is a queen.
   */
    PieceType(Color color, boolean isQueen) {
      this.color = color;
      this.isQueen = isQueen;
    }

  /**
   * Returns the color associated with this piece type.
   * * @return The {@link Color} of the piece.
   */
  public Color getColor() {
    return color;
  }

  /**
   * Checks if this piece type is a Queen.
   * * @return {@code true} if it is a Queen, {@code false} otherwise.
   */
  public boolean isQueen() {
    return isQueen;
  }

  /**
   * Checks if this piece type is a Pawn.
   * * @return {@code true} if it is a Pawn, {@code false} if it is a Queen.
   */
  public boolean isPawn() {
    return !isQueen;
  }
  }
