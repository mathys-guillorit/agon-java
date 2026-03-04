package fr.univ.bordeaux.ui.cli;

import java.util.ArrayList;

interface Bitboard{} //  temporary to fix problems
/**
 * delegate display
 * converts bitboard state into String grid
 */
public class ConsoleRenderer{

  private Bitboard board;
  private ArrayList<Character> lines;

  private static final short numberStartASCII = 48;

  public ConsoleRenderer(Bitboard board) {
    this.board = board;
    this.lines = new ArrayList<>();
    final short K_Letter = 75; // ASCII K
    final short A_Letter = 64; // ASCII A
    for (short c = K_Letter; c > A_Letter; c--)
      this.lines.add((char)c);
  }

  /**
   * do convolution function with integers only int opposite direction (\/)
   * <pre>
   *   \  /
   *    \/
   * </pre>
   * (f(x,k) = |-|x-k-b|+k| = |(-|x-k-b|+k)| )
   * @param x the variable passed
   * @param a the height of the "cone"
   * @param b the horizontal translation of the curve
   * @return int the result
   */
  private int coneReversed(int x, int a, int b){
    return Math.abs(-Math.abs(x-a-b)+a);
  }

  /**
   * do convolution function with integers only (/\)
   * <pre>
   *   /\
   *  /  \
   * </pre>
   * @param x the variable passed
   * @param a the height of the "cone"
   * @param b the horizontal translation of the curve
   * @return int the result
   */
  private int cone(int x, int a, int b){
    return Math.abs(Math.abs(x+a+b)-a);
  }

  /**
   * update board display to terminal
   * no arguments need board in constructor
   * (to be used by drawHexagon() first)
   */
  public void renderer(){
    int idxContent, spaceCount;
    System.out.println();
    final int linesCount = 11;
    final int midLine = (linesCount-1) / 2;
    for (int lines = 0; lines < linesCount; lines++) {
      for (spaceCount = 0; spaceCount < this.coneReversed(lines,0,5); spaceCount++)
        System.out.print(' ');
      if (lines > midLine)
        System.out.print(this.lines.get(lines)+" \\");
      else if (lines == midLine)
        System.out.print("F |");
      else
        System.out.print(this.lines.get(lines)+" /");
      for (idxContent = 0; idxContent < 6 + this.cone(lines,midLine,-10); idxContent++){
        if (coordinateValidator(lines,idxContent))
          this.drawHexagon(lines, idxContent);
        if (idxContent!=10-spaceCount) System.out.print(' ');
      }
      if (lines > midLine)
        System.out.print("/ " + (17-lines));
      else if (lines == midLine)
        System.out.print("| ");
      else
        System.out.print("\\");
      System.out.println();
    }
    System.out.println("        1 2 3 4 5 6");
  }

  /**
   * check if a coordinate is valid or not
   * (coordiante can be invalid example: k3)
   * @return true if it is false else
   */
  private boolean coordinateValidator(int x, int y){
//    if (x < 0 || x > 10) return false;
//    if (y < 0 || y > 10) return false;
    return true;
  }

  /**
   * draw and exagon without jump line
   * example: "c4" where c is 9 lines later (from the first one)
   * with 4 diagonal lines in vertical
   * final coordinates will be : x = 9 (c) and y = 4 (not converted)
   * @param x horizontal coordinate
   * @param y diagonal coordinate
   */
  private void drawHexagon(int x, int y){
    ///  TODO: from Bitboard board get
    ///  the character at the exact x y position and print it
    System.out.print(""+(x+6-y));
  }

}
