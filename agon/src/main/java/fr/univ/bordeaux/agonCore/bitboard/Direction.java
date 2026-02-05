package fr.univ.bordeaux.agonCore.bitboard;

public enum Direction {
  East(1),
  West(-1),
  NorthEast(12),
  NorthWest(11),
  SouthEast(-11),
  SouthWest(-12);
  private final int v;

  Direction(int v) {
    this.v = v;
  }

  public static Direction getOpposite(Direction d) {
    return switch (d) {
      case East -> Direction.West;
      case West -> Direction.East;
      case NorthEast -> Direction.SouthWest;
      case SouthWest -> Direction.NorthEast;
      case NorthWest -> Direction.SouthEast;
      case SouthEast -> Direction.NorthWest;
      default -> null;
    };
  }

  public int getValue() {
    return v;
  }
}

