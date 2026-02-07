package fr.univ.bordeaux.agonCore.agonElements;

public class Move {
  int from;
  int to;
  Color color;
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
