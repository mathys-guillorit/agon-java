package fr.univ.bordeaux.agonCore.bitboard;

/**
 * Utility class for converting board coordinates between human-readable formats
 * and internal bitboard indices.
 * * <p>This mapper assumes an axial-to-linear mapping based on an 11-column grid
 * where rows are represented by letters (A-K) and columns by integers (1-11).</p>
 */
public class CoordinateMapper {
  /**
   * Default constructor for the CoordinateMapper utility.
   */
  public CoordinateMapper(){
  }
  /**
   * Converts a coordinate pair (Letter, Column) into a 1D bitboard index.
   * * <p>The mapping follows the formula: <code>index = (row_offset * 11) + (column - 1)</code>,
   * where 'A' corresponds to row 0. This creates a contiguous range of indices
   * suitable for bitwise operations and array access.</p>
   * *
   *
   * @param letter The character representing the row (e.g., 'A', 'B', 'C'...).
   * @param col    The integer representing the column (1-indexed).
   * @return The corresponding 0-indexed position in the 1D bitboard array.
   */
  public static int toIndex(char letter, int col) {
    int Base = 'A';
    return (((int) letter - Base) * (11)) + (col - 1);
  }
}