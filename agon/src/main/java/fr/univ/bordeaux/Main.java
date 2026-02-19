package fr.univ.bordeaux;

import fr.univ.bordeaux.application.GameLauncher;
import fr.univ.bordeaux.ui.cli.AgonShell;

/**
 * @version Java 21 (Microsoft OpenJdk 21.0.9)<br>
 *     - test programm using <code>mvn test</code> (don't require to compile before)<br>
 *     - compile program using <code>mvn compile</code> <br>
 *     - exec using : <code>mvn exec:java</code> (require to compile before)<br>
 *     - create jar package : <code>mvn package</code>
 * @short from "/agon" repertory
 */
public class Main {

  public static void main(String[] arg) throws Exception {
    /*BitBoard whiteQueen=new BitBoard();
    BitBoard blackQueen=new BitBoard();
    BitBoard whitePawns=new BitBoard();
    BitBoard blackPawns=new BitBoard();
    whiteQueen.setBit(62,1L);
    blackQueen.setBit(64,1L);
    blackPawns.setBit(74,1L);
    whitePawns.setBit(40,1L);
    blackPawns.setBit(36,1L);
    whitePawns.setBit(37,1L);
    AgonBoardImpl board = new AgonBoardImpl(whiteQueen, blackQueen, whitePawns, blackPawns);
    System.out.println("board avant mouvement");
    board.printBoard();
    board.applyMove(new Move(CoordinateMapper.toIndex('G',9),CoordinateMapper.toIndex('F',8),Color.BLACK));
    System.out.println("board après mouvement");
    board.printBoard();*/
    /*var a = new GUIExample();
    a.launch(GUIExample.class, arg);*/

    new GameLauncher().launch(arg);
  }

  /** test using real case and later with junit */
  public static void testCli() {
    AgonShell shell = new AgonShell();
    shell.start();
  }
}
