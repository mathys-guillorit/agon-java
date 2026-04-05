package fr.univ.bordeaux.agoncore.bitboard;

import fr.univ.bordeaux.technical.utils.GameLogger;

/**
 * Utility class for converting board coordinates between human-readable formats and internal
 * bitboard indices.
 *
 * <p>This mapper assumes an axial-to-linear mapping based on an 11-column grid where rows are
 * represented by letters (A-K) and columns by integers (1-11).
 */
public class CoordinateMapper {
  /** Default constructor for the CoordinateMapper utility. */
  public CoordinateMapper() {}

  /**
   * Converts a coordinate pair (Letter, Column) into a 1D bitboard index.
   *
   * @param letter The character representing the row (e.g., 'A', 'B', 'C'...).
   * @param col The integer representing the column (1-indexed).
   * @return The corresponding 0-indexed position in the 1D bitboard array.
   */
  public static int toIndex(char letter, int col) {
    if (letter > 'K' || letter < 'A' || col > 11 || col < 1) {
      GameLogger.debug("CoordinateMapper: Invalid input " + letter + col);
      return -1;
    }
    int base = 'A';
    int index = (((int) letter - base) * (11)) + (col - 1);
    GameLogger.debug("CoordinateMapper: " + letter + col + " -> index " + index);
    return index;
  }

  /**
   * Converts a linear BitBoard index into AbaPro coordinate notation.
   *
   * @param index the numerical identifier of the cell.
   * @return a {@link String} representing the coordinate in letter + digit format.
   */
  public static String toAbaPro(int index) {
    if (index < 0) return "reloc";
    char letter = (char) ('A' + (index / 11));
    int col = (index % 11) + 1;
    String res = "" + letter + col;
    GameLogger.debug("CoordinateMapper: index " + index + " -> AbaPro " + res);
    return res;
  }

  /**
   * Converts a 1D bitboard index back into a human-readable coordinate string (e.g., "c3").
   *
   * @param index The 0-indexed position in the 1D bitboard array.
   * @return The coordinate string in lowercase.
   */
  public static String toCoordinate(int index) {
    if (index < 0) {
      return "reloc";
    }

    int rowOffset = index / 11;
    int col = (index % 11) + 1;
    char letter = (char) ('a' + rowOffset);

    String res = "" + letter + col;
    GameLogger.debug("CoordinateMapper: index " + index + " -> coord " + res);
    return res;
  }

  /**
   * Helper method to convert a string coordinate directly to an index.
   *
   * @param coord The coordinate string (e.g., "c3" or "C3").
   * @return The bitboard index.
   */
  public static int fromCoordinateString(String coord) {
    try {
      char letter = Character.toUpperCase(coord.charAt(0));
      int col = Integer.parseInt(coord.substring(1));
      int index = toIndex(letter, col);
      GameLogger.debug("CoordinateMapper: parsed string " + coord + " to index " + index);
      return index;
    } catch (Exception e) {
      GameLogger.debug("CoordinateMapper: Failed to parse coordinate string: " + coord);
      return -1;
    }
  }
}