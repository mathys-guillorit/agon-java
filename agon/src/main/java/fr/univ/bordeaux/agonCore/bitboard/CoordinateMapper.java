package fr.univ.bordeaux.agonCore.bitboard;

public class CoordinateMapper {

  public static int toIndex(char letter, int col) {
    int Base = 'A';
    return (((int) letter - Base) * (11)) + (col - 1);
  }
}
