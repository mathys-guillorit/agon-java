package fr.univ.bordeaux.agoncore.bitboard;

/**
 * Represents the six possible movement directions on the Agon hexagonal grid.
 *
 * <p>Each direction is associated with an integer offset ({@code v}) used to navigate the 1D array
 * representation of the hexagonal board. These offsets are calculated based on an axial coordinate
 * system mapped onto an 11-column grid layout. *
 */
public enum Direction {
  /**
   * Move one tile to the right.
   *
   * <p>Index offset: {@code +1}.
   */
  EAST(1),

  /**
   * Move one tile to the left.
   *
   * <p>Index offset: {@code -1}.
   */
  WEST(-1),

  /**
   * Move to the upper-right neighbor.
   *
   * <p>Index offset: {@code +12}.
   */
  NORTH_EAST(12),

  /**
   * Move to the upper-left neighbor.
   *
   * <p>Index offset: {@code +11}.
   */
  NORTH_WEST(11),

  /**
   * Move to the lower-right neighbor.
   *
   * <p>Index offset: {@code -11}.
   */
  SOUTH_EAST(-11),

  /**
   * Move to the lower-left neighbor.
   *
   * <p>Index offset: {@code -12}.
   */
  SOUTH_WEST(-12);

  /** The integer shift value applied to a bitboard index. */
  private final int value;

  /**
   * Constructs a direction with its corresponding bitboard index offset.
   *
   * @param value The integer value to add to a 1D index to move in this direction.
   */
  Direction(int value) {
    this.value = value;
  }

  /**
   * Retrieves the direction diametrically opposite to the current one.
   *
   * <p>This utility is essential for capture detection logic (sandwiches), where an enemy piece is
   * trapped between two friendly pieces aligned along an axis.
   *
   * @param d The {@link Direction} for which to find the counterpart.
   * @return The opposite {@link Direction}.
   * @throws java.util.NoSuchElementException if the direction is not handled.
   */
  public static Direction getOpposite(Direction d) {
    return switch (d) {
      case EAST -> WEST;
      case WEST -> EAST;
      case NORTH_EAST -> SOUTH_WEST;
      case SOUTH_WEST -> NORTH_EAST;
      case NORTH_WEST -> SOUTH_EAST;
      case SOUTH_EAST -> NORTH_WEST;
    };
  }

  /**
   * Returns the integer offset associated with this direction.
   *
   * @return The index shift value (positive or negative).
   */
  public int getValue() {
    return value;
  }
}
