package fr.univ.bordeaux.agoncore.bitboard;

/**
 * Utility class for converting board coordinates between human-readable formats and internal
 * bitboard indices. *
 *
 * <p>This mapper assumes an axial-to-linear mapping based on an 11-column grid where rows are
 * represented by letters (A-K) and columns by integers (1-11).
 */
public class CoordinateMapper {
  /** Default constructor for the CoordinateMapper utility. */
  public CoordinateMapper() {}

  /**
   * Converts a coordinate pair (Letter, Column) into a 1D bitboard index. *
   *
   * <p>The mapping follows the formula: <code>index = (row_offset * 11) + (column - 1)</code>,
   * where 'A' corresponds to row 0. This creates a contiguous range of indices suitable for bitwise
   * operations and array access. *
   *
   * @param letter The character representing the row (e.g., 'A', 'B', 'C'...).
   * @param col The integer representing the column (1-indexed).
   * @return The corresponding 0-indexed position in the 1D bitboard array.
   */
  public static int toIndex(char letter, int col) {
    if (letter > 'K' || letter < 'A' || col > 11 || col < 1) {
      return -1;
    }
    int base = 'A';
    return ((int) letter - base) * 11 + col - 1;
  }

  /**
   * Converts a linear BitBoard index into AbaPro coordinate notation. AbaPro notation uses a letter
   * for the ring or zone (A, B, C...) followed by a number for the column (1 to 11).
   *
   * @param index the numerical identifier of the cell.
   * @return a {@link String} representing the coordinate in letter + digit format.
   */
  public static String toAbaPro(int index) {
    char letter = (char) ('A' + (index / 11));
    int col = (index % 11) + 1;
    return "" + letter + col;
  }

  /**
   * Converts a 1D bitboard index back into a human-readable coordinate string (e.g., "c3").
   * * @param index The 0-indexed position in the 1D bitboard array.
   *
   * @return The coordinate string in lowercase (as per ABA-pro notation).
   */
  public static String toCoordinate(int index) {
    if (index < 0) {
      return "reloc";
    }

    int rowOffset = index / 11;
    int col = (index % 11) + 1;
    char letter = (char) ('a' + rowOffset);

    return "" + letter + col;
  }

  /**
   * Helper method to convert a string coordinate directly to an index. * @param coord The
   * coordinate string (e.g., "c3" or "C3").
   *
   * @return The bitboard index.
   */
  public static int fromCoordinateString(String coord) {
    char letter = Character.toUpperCase(coord.charAt(0));
    int col = Integer.parseInt(coord.substring(1));
    return toIndex(letter, col);
  }
}
