package fr.univ.bordeaux;

/**
 * @version Java 21 (Microsoft OpenJdk 21.0.9)<br>
 *     - test programm using <code>mvn test</code> (don't require to compile before)<br>
 *     - compile program using <code>mvn compile</code> <br>
 *     - exec using : <code>mvn exec:java</code> (require to compile before)<br>
 *     - create jar package : <code>mvn package</code> from "/agon" repertory.
 * @version Java 21 (Microsoft OpenJdk 21.0.9)<br>
 *     - test programm using <code>mvn test</code> (don't require to compile before)<br>
 *     - compile program using <code>mvn compile</code> <br>
 *     - exec using : <code>mvn exec:java</code> (require to compile before)<br>
 *     - create jar package : <code>mvn package</code> from "/agon" repertory
 */
public class Main {

  /**
   * run main program
   *
   * @param arg arguments passed from terminal (cli)
   * @throws Exception if resources or initialization items not found or (dev for now) problems
   */
  public static void main(String[] arg) throws Exception {
    System.out.println("prog principal OK + ajout JUnit");
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
    board.printBoard();
    System.out.println(board.applyMove(
      new Move(CoordinateMapper.toIndex('G',9),CoordinateMapper.toIndex('F',9),Color.BLACK)
    ));
    System.out.println(board.applyMove(
      new Move(CoordinateMapper.toIndex('F',8),CoordinateMapper.toIndex('F',7),Color.WHITE)
    ));
    System.out.println(board.applyMove(
      new Move(CoordinateMapper.toIndex('F',9),CoordinateMapper.toIndex('E',8),Color.BLACK)
    ));
    board.printBoard();
    board.undoMove();
    board.printBoard();
    System.out.println("board avant mouvement");
    board.printBoard();
    board.applyMove(
      new Move(CoordinateMapper.toIndex('G',9),CoordinateMapper.toIndex('F',8),Color.BLACK
    ));
    System.out.println("board après mouvement");
    board.printBoard();*/
    /*var a = new GUIExample();
    a.launch(GUIExample.class, arg);*/
  }
}
