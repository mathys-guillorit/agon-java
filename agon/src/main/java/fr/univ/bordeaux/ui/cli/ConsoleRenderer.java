package fr.univ.bordeaux.ui.cli;

import fr.univ.bordeaux.agonCore.agonElements.PieceType;
import fr.univ.bordeaux.agonCore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.agonCore.bitboard.RestrictedAgonBoard;
import java.util.ArrayList;

/**
 * Delegate class responsible for converting the internal BitBoard state into a String grid.
 *
 * <p>This class acts as a View component in the architecture, transforming the {@link
 * RestrictedAgonBoard} data into a visual ASCII-art hexagon suitable for the console.
 */
public class ConsoleRenderer {

  /*private RestrictedAgonBoard board;
  private ArrayList<Character> lines;

  **
   * Constructs a new ConsoleRenderer.
   *
   * <p>Initializes the list of row identifiers from 'K' (top) down to 'A' (bottom) to match the
   * visual representation of the Agon board.
   *
   * @param board The game board data source (read-only interface).
   *
  public ConsoleRenderer(RestrictedAgonBoard board) {
    this.board = board;
    this.lines = new ArrayList<>();
    final short K_Letter = 75; // ASCII K
    final short A_Letter = 65; // ASCII A
    for (short c = K_Letter; c >= A_Letter; c--) this.lines.add((char) c);
  }*/

  /**
   * Calculates the indentation (spaces) needed for each line to form the hexagonal shape.
   *
   * <p>This corresponds to a convolution function acting in the opposite direction (\/). The
   * formula approximates: f(x,k) = |-|x-k-b|+k|
   *
   * <pre>
   * \  /
   * \/
   * </pre>
   *
   * @param x The current line index (variable).
   * @param a The height/amplitude of the "cone".
   * @param b The horizontal translation of the curve.
   * @return The number of spaces to prepend to the line.
   */
  private static int coneReversed(int x, int a, int b) {
    return Math.abs(-Math.abs(x - a - b) + a);
  }

  /**
   * Calculates the width (number of cells) of the hexagon at a specific line index.
   *
   * <p>This corresponds to a convolution function acting in the standard direction (/\).
   *
   * <pre>
   * /\
   * /  \
   * </pre>
   *
   * @param x The current line index (variable).
   * @param a The height/amplitude of the "cone".
   * @param b The horizontal translation of the curve.
   * @return The number of active cells to draw on this line.
   */
  private static int cone(int x, int a, int b) {
    return Math.abs(Math.abs(x + a + b) - a);
  }

  /**
   * Generates the complete string representation of the board.
   *
   * <p>This method builds the grid line by line, handling:
   *
   * <ul>
   *   <li>Indentation (using {@link #coneReversed(int, int, int)})
   *   <li>Left borders and Row identifiers (e.g., "K /")
   *   <li>Cell content (using {@link #getSymbolAt(RestrictedAgonBoard,int, int,ArrayList)})
   *   <li>Right borders and Row numbers
   * </ul>
   *
   * @return A formatted String representing the current game state ready for display.
   */
  public static String getBoardRepresentation(RestrictedAgonBoard board) {
    ArrayList<Character> arrayLines = new ArrayList<>();
    final short K_Letter = 75; // ASCII K
    final short A_Letter = 65; // ASCII A
    for (short c = K_Letter; c >= A_Letter; c--) arrayLines.add((char) c);
    StringBuilder sb = new StringBuilder();
    sb.append('\n');
    int idxContent, spaceCount;
    final int linesCount = 11;
    final int midLine = (linesCount - 1) / 2;

    for (int lines = 0; lines < linesCount; lines++) {
      for (spaceCount = 0; spaceCount < coneReversed(lines, 0, 5); spaceCount++)
        sb.append(' ');

      if (lines > midLine) sb.append(arrayLines.get(lines)).append(" \\");
      else if (lines == midLine) sb.append("F |");
      else sb.append(arrayLines.get(lines)).append(" /");

      int width = 6 + cone(lines, midLine, -10);
      for (idxContent = 0; idxContent < width; idxContent++) {
        char symbol = getSymbolAt(board,lines, idxContent,arrayLines);
        sb.append(symbol);
        if (idxContent != width - 1) {
          sb.append(' ');
        }
      }
      if (lines > midLine) {
        sb.append("/ ").append(17 - lines);
      } else if (lines == midLine) {
        sb.append("| ");
      } else {
        sb.append("\\");
      }
      sb.append("\n");
    }
    sb.append("        1 2 3 4 5 6\n");
    return sb.toString();
  }

  /**
   * Retrieves the character symbol for a piece at a specific visual coordinate.
   *
   * <p>This method translates the visual grid coordinates (row index, logical column) into the
   * internal bitboard index using {@link CoordinateMapper}.
   *
   * @param x The vertical line index (corresponding to letters K..A).
   * @param y The diagonal/logical column index on that line.
   * @return The char representing the piece, or '.' if empty/error.
   */
  private static char getSymbolAt(RestrictedAgonBoard board,int x, int y,ArrayList<Character> arrayLines) {
    try {
      char rowChar = arrayLines.get(x);
      int logicalCol = y + 1;
      int index = CoordinateMapper.toIndex(rowChar, logicalCol);
      PieceType piece = board.getPieceAt(index);
      return getSymbolFromPiece(piece);
    } catch (Exception e) {
      return '.';
    }
  }

  /**
   * Converts a {@link PieceType} enum into a single ASCII character.
   *
   * @param piece The piece to convert.
   * @return 'O' for White Pawn, 'X' for Black Pawn, 'Q' for White Queen, 'q' for Black Queen, '.'
   *     for empty.
   */
  private static char getSymbolFromPiece(PieceType piece) {
    if (piece == null) return '.';
    return switch (piece) {
      case WHITE_PAWN -> 'O';
      case BLACK_PAWN -> 'X';
      case WHITE_QUEEN -> 'Q';
      case BLACK_QUEEN -> 'q';
      default -> '.';
    };
  }
}
