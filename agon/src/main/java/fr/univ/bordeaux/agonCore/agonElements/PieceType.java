package fr.univ.bordeaux.agonCore.agonElements;

public enum PieceType {
  WHITE_QUEEN(Color.WHITE, true),
  BLACK_QUEEN(Color.BLACK, true),
  WHITE_PAWN(Color.WHITE, false),
  BLACK_PAWN(Color.BLACK, false);

  private final Color color;
  private final boolean isQueen;

  PieceType(Color color, boolean isQueen) {
    this.color = color;
    this.isQueen = isQueen;
  }
  public Color getColor() {
    return color;
  }

  public boolean isQueen() {
    return isQueen;
  }

  public boolean isPawn() {
    return !isQueen;
  }
}