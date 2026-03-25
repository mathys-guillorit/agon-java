<<<<<<< HEAD:src/main/java/fr/univ/bordeaux/agonCore/bitboard/Direction.java
package fr.univ.bordeaux.agonCore.bitboard;

/**
 * Represents the six possible movement directions on the Agon hexagonal grid.
 *
 * <p>Each direction is associated with an integer offset ({@code v}) used to navigate
 * the 1D array representation of the hexagonal board. These offsets are calculated
 * based on an axial coordinate system mapped onto an 11-column grid layout.</p>
 * *
 */
public enum Direction {
  /** * Move one tile to the right.
   * <p>Index offset: {@code +1}.</p>
   */
  East(1),

  /** * Move one tile to the left.
   * <p>Index offset: {@code -1}.</p>
   */
  West(-1),

  /** * Move to the upper-right neighbor.
   * <p>Index offset: {@code +12}.</p>
   */
  NorthEast(12),

  /** * Move to the upper-left neighbor.
   * <p>Index offset: {@code +11}.</p>
   */
  NorthWest(11),

  /** * Move to the lower-right neighbor.
   * <p>Index offset: {@code -11}.</p>
   */
  SouthEast(-11),

  /** * Move to the lower-left neighbor.
   * <p>Index offset: {@code -12}.</p>
   */
  SouthWest(-12);

  /** The integer shift value applied to a bitboard index. */
  private final int v;

  /**
   * Constructs a direction with its corresponding bitboard index offset.
   *
   * @param v The integer value to add to a 1D index to move in this direction.
   */
  Direction(int v) {
    this.v = v;
  }

  /**
   * Retrieves the direction diametrically opposite to the current one.
   *
   * <p>This utility is essential for capture detection logic (sandwiches),
   * where an enemy piece is trapped between two friendly pieces aligned
   * along an axis.</p>
   *
   *
   *
   * @param d The {@link Direction} for which to find the counterpart.
   * @return The opposite {@link Direction}.
   * @throws java.util.NoSuchElementException if the direction is not handled.
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
   * Returns the integer offset associated with this direction.
   *
   * @return The index shift value (positive or negative).
   */
  public int getValue() {
    return v;
  }
}
=======
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
  East(1),

  /**
   * Move one tile to the left.
   *
   * <p>Index offset: {@code -1}.
   */
  West(-1),

  /**
   * Move to the upper-right neighbor.
   *
   * <p>Index offset: {@code +12}.
   */
  NorthEast(12),

  /**
   * Move to the upper-left neighbor.
   *
   * <p>Index offset: {@code +11}.
   */
  NorthWest(11),

  /**
   * Move to the lower-right neighbor.
   *
   * <p>Index offset: {@code -11}.
   */
  SouthEast(-11),

  /**
   * Move to the lower-left neighbor.
   *
   * <p>Index offset: {@code -12}.
   */
  SouthWest(-12);

  /** The integer shift value applied to a bitboard index. */
  private final int v;

  /**
   * Constructs a direction with its corresponding bitboard index offset.
   *
   * @param v The integer value to add to a 1D index to move in this direction.
   */
  Direction(int v) {
    this.v = v;
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
      case East -> Direction.West;
      case West -> Direction.East;
      case NorthEast -> Direction.SouthWest;
      case SouthWest -> Direction.NorthEast;
      case NorthWest -> Direction.SouthEast;
      case SouthEast -> Direction.NorthWest;
    };
  }

  /**
   * Returns the integer offset associated with this direction.
   *
   * @return The index shift value (positive or negative).
   */
  public int getValue() {
    return v;
  }
}
>>>>>>> main:src/main/java/fr/univ/bordeaux/agoncore/bitboard/Direction.java
