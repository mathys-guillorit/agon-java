package fr.univ.bordeaux.agonCore.bitboard;

/**
 * Represents the six possible movement directions on the Agon hexagonal grid.
 * <p>
 * Each direction is associated with an integer offset (v) used to navigate the 1D array
 * representation of the 2D hexagonal board. These offsets are specifically designed for an
 * 11-column axial coordinate mapping.
 * </p>
 */
public enum Direction {
  /**
   * Move one tile to the right (+1 index).
   */
  East(1),
  /**
   * Move one tile to the left (-1 index).
   */
  West(-1),
  /**
   * Move to the upper-right neighbor (+12 index).
   */
  NorthEast(12),
  /**
   * Move to the upper-left neighbor (+11 index).
   */
  NorthWest(11),
  /**
   * Move to the lower-right neighbor (-11 index).
   */
  SouthEast(-11),
  /**
   * Move to the lower-left neighbor (-12 index).
   */
  SouthWest(-12);

  private final int v;

  /**
   * Constructs a direction with its corresponding bitboard index offset. * @param v The integer
   * value to add to an index to move in this direction.
   */
  Direction(int v) {
    this.v = v;
  }

  /**
   * Retrieves the direction diametrically opposite to the one provided.
   * <p>
   * This is primarily used for capture logic (detecting "sandwiches"), where an enemy piece must be
   * flanked by two friendly pieces in opposite directions.
   * </p>
   *
   * @param d The direction to find the opposite of.
   * @return The opposite {@link Direction}, or {@code null} if the input is invalid.
   */
  public static Direction getOpposite(Direction d) {
    return switch (d) {
      case East -> Direction.West;
      case West -> Direction.East;
      case NorthEast -> Direction.SouthWest;
      case SouthWest -> Direction.NorthEast;
      case NorthWest -> Direction.SouthEast;
      case SouthEast -> Direction.NorthWest;
    };
  }

  /**
   * Returns the integer offset associated with this direction. * @return The index shift value.
   */
  public int getValue() {
    return v;
  }
}