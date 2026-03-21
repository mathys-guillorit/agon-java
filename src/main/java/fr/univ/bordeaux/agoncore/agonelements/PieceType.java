package fr.univ.bordeaux.agoncore.agonelements;

/**
 * Represents the different types of pieces available in the Agon game. *
 *
 * <p>Each piece type is defined by its owning color and its rank (Queen or Pawn). This enum is used
 * throughout the board logic to simplify piece identification and movement rules without manual
 * conditional checks.
 */
public enum PieceType {
  /** The white queen piece. */
  WHITE_QUEEN(Color.WHITE, true),
  /** The black queen piece. */
  BLACK_QUEEN(Color.BLACK, true),
  /** A white pawn piece. */
  WHITE_PAWN(Color.WHITE, false),
  /** A black pawn piece. */
  BLACK_PAWN(Color.BLACK, false);

  /** The color of piece */
  private final Color color;

  /** Boolean to know if the piece is a queen */
  private final boolean isQueen;

  /**
   * Internal constructor for piece type constants.
   *
   * @param color The owner's color.
   * @param isQueen {@code true} if the piece is a queen, {@code false} if it is a pawn.
   */
  PieceType(Color color, boolean isQueen) {
    this.color = color;
    this.isQueen = isQueen;
  }

  /**
   * Returns the color associated with this piece type.
   *
   * @return The {@link Color} of the piece owner.
   */
  public Color getColor() {
    return color;
  }

  /**
   * Checks if this piece type is a Queen.
   *
   * @return {@code true} if it is a Queen, {@code false} otherwise.
   */
  public boolean isQueen() {
    return isQueen;
  }

  /**
   * Checks if this piece type is a Pawn.
   *
   * @return {@code true} if it is a Pawn, {@code false} if it is a Queen.
   */
  public boolean isPawn() {
    return !isQueen;
  }

  public static PieceType getQueen(final Color color) {
    PieceType queenType;

    if (color == Color.WHITE) {
      queenType = WHITE_QUEEN;
    } else {
      queenType = BLACK_QUEEN;
    }

    return queenType;
  }

  /**
   * Static factory method to retrieve the Pawn piece type for a specific color.
   *
   * @param color The {@link Color} of the requested Pawn.
   * @return The corresponding {@link PieceType} (WHITE_PAWN or BLACK_PAWN).
   */
  public static PieceType getPawn(final Color color) {
    PieceType pawnType;

    if (color == Color.WHITE) {
      pawnType = WHITE_PAWN;
    } else {
      pawnType = BLACK_PAWN;
    }

    return pawnType;
  }

  @Override
  public String toString() {
    if (this.isQueen) {
      return (this.color == Color.WHITE) ? "WhiteQueen" : "BlackQueen";
    } else {
      return (this.color == Color.WHITE) ? "WhitePawn" : "BlackPawn";
    }
  }
}
