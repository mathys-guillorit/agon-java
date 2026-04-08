package fr.univ.bordeaux.ui.gui.components;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class handling hexagonal grid mathematics. Provides static methods to convert pixel
 * coordinates to hexagonal axial coordinates and calculate the layout of the Agon board.
 */
public final class HexMath {

  /** The standard radius/size of a single hexagon on the board. */
  public static final double HEX_SIZE = 30.0;

  /** Precalculated square root of 3, used extensively for hexagonal axial math. */
  public static final double SQRT_3 = Math.sqrt(3);

  private HexMath() {}

  /**
   * Converts raw pixel coordinates from the canvas into a logical hexagonal coordinate.
   *
   * @param x The X pixel coordinate of the mouse click.
   * @param y The Y pixel coordinate of the mouse click.
   * @param width The total width of the canvas.
   * @param height The total height of the canvas.
   * @return The logical board coordinate (e.g., "F6"), or null if the click is out of bounds.
   */
  public static String pixelToHex(
      final double x, final double y, final double width, final double height) {
    final double ptX = x - (width / 2);
    final double ptY = y - (height / 2);
    final double fracQ = (SQRT_3 / 3.0 * ptX - 1.0 / 3.0 * ptY) / HEX_SIZE;
    final double fracR = 2.0 / 3.0 * ptY / HEX_SIZE;

    final int[] rounded = axialRound(fracQ, fracR);
    final int q = rounded[0];
    final int r = rounded[1];

    String hexCoord = null;
    if (Math.abs(q) <= 5 && Math.abs(r) <= 5 && Math.abs(-q - r) <= 5) {
      final char rowChar = (char) (65 + 5 - r);
      final int logicalCol = q + 6;
      hexCoord = "" + rowChar + logicalCol;
    }
    return hexCoord;
  }

  /**
   * Rounds fractional axial coordinates to the nearest valid integer hexagon coordinates.
   *
   * @param qFrac The fractional Q coordinate.
   * @param rFrac The fractional R coordinate.
   * @return An array containing the rounded [q, r] coordinates.
   */
  public static int[] axialRound(final double qFrac, final double rFrac) {
    final double sFrac = -qFrac - rFrac;
    int q = (int) Math.round(qFrac);
    int r = (int) Math.round(rFrac);
    final int s = (int) Math.round(sFrac);

    final double diffQ = Math.abs(q - qFrac);
    final double diffR = Math.abs(r - rFrac);
    final double diffS = Math.abs(s - sFrac);

    if (diffQ > diffR && diffQ > diffS) {
      q = -r - s;
    } else if (diffR > diffS) {
      r = -q - s;
    }
    return new int[] {q, r};
  }

  /**
   * Generates a list of all valid axial coordinates [q, r] that make up the Agon hexagonal board.
   *
   * @return A list of integer arrays representing the board's grid.
   */
  public static List<int[]> getBoardCoordinates() {
    final List<int[]> coords = new ArrayList<>();
    final int boardRadius = 5;
    for (int q = -boardRadius; q <= boardRadius; q++) {
      final int r1 = Math.max(-boardRadius, -q - boardRadius);
      final int r2 = Math.min(boardRadius, -q + boardRadius);
      for (int r = r1; r <= r2; r++) {
        coords.add(new int[] {q, r});
      }
    }
    return coords;
  }
}
