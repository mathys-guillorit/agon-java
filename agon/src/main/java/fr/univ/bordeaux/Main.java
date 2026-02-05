package fr.univ.bordeaux;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.bitboard.BitBoard;
import fr.univ.bordeaux.agonCore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.ui.gui.controllers.GUIExample;
/**
 * @version Java 21 (Microsoft OpenJdk 21.0.9)<br>
 * - test programm using <code>mvn test</code> (don't require to compile before)<br>
 * - compile program using <code>mvn compile</code> <br>
 * - exec using : <code>mvn exec:java</code> (require to compile before)<br>
 * - create jar package : <code>mvn package</code>
 *
 * @short from "/agon" repertory
 */
public class Main {
  public static void main(String[] arg) throws Exception {
    System.out.println("prog principal OK + ajout JUnit");
    long[] whiteQueen=new long[2];
    long[] blackQueen=new long[2];
    long[] whitePawns=new long[2];
    long[] blackPawns=new long[2];
    whiteQueen[0] = 1L << 58;
    whiteQueen[1] = 0L;

    whitePawns[0] |= 1L << 50;
    whiteQueen[0] |= 1L << 63;
    blackQueen[1] |= 1L ;
    blackPawns[1] |= 1L << 74-64;
    whitePawns[0] |= 1L << 40;
    blackPawns[0] |= 1L << 38;
    whitePawns[0] |= 1L << 37;
    BitBoard board = new BitBoard(whiteQueen, blackQueen, whitePawns, blackPawns);
    System.out.println("board avant mouvement");
    board.printBoard();
    board.applyMove(CoordinateMapper.toIndex('G',9),CoordinateMapper.toIndex('F',8),Color.BLACK);
    System.out.println("Replacement blanc");
    board.printMask(board.generateLegalMoves(Color.WHITE));
    board.applyMove(-1,CoordinateMapper.toIndex('D',7),Color.WHITE);
    //board.applyMove(-1,CoordinateMapper.toIndex('B',1),Color.WHITE);
    System.out.println("board après mouvement");
    board.printBoard();
    /*System.out.println(CoordinateMapper.toIndex('G',0));
    System.out.println("Legal moves:");
    board.printMask(legalMoves);*/
    /*var a = new GUIExample();
    a.launch(GUIExample.class, arg);*/
  }
}
