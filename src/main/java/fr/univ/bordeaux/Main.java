package fr.univ.bordeaux;

import fr.univ.bordeaux.application.GameLauncher;


/**
 * EntryPoint class.
 */
public class Main {


  /**
   * Main method to run the Agon project on.
   *
   * @param arg user input from terminal
   *
   * @throws Exception if a problem occur.
   */
  public static void main(String[] arg) throws Exception {
    //BitBoard whiteQueen=new BitBoard();
    //BitBoard blackQueen=new BitBoard();
    //BitBoard whitePawns=new BitBoard();
    //BitBoard blackPawns=new BitBoard();
    //whiteQueen.setBit(62,1L);
    //blackQueen.setBit(64,1L);
    //blackPawns.setBit(74,1L);
    //whitePawns.setBit(40,1L);
    //blackPawns.setBit(36,1L);
    //whitePawns.setBit(37,1L);
    //AgonBoardImpl board = new AgonBoardImpl(
    //        whiteQueen, blackQueen, whitePawns, blackPawns
    //);
    //board.printBoard();
    //System.out.println(board.applyMove(new Move(
    //  CoordinateMapper.toIndex('G',9),
    //  CoordinateMapper.toIndex('F',9),
    //  Color.BLACK
    //)));
    //System.out.println(board.applyMove(new Move(
    //  CoordinateMapper.toIndex('F',8),
    //  CoordinateMapper.toIndex('F',7),
    //  Color.WHITE
    //)));
    //System.out.println(board.applyMove(new Move(
    //  CoordinateMapper.toIndex('F',9),
    //  CoordinateMapper.toIndex('E',8),
    //  Color.BLACK
    //)));
    //board.printBoard();
    //board.undoMove();
    //board.printBoard();
    //System.out.println("board avant mouvement");
    //board.printBoard();
    //board.applyMove(new Move(
    //  CoordinateMapper.toIndex('G',9),
    //  CoordinateMapper.toIndex('F',8),
    //  Color.BLACK
    //));
    //System.out.println("board après mouvement");
    //board.printBoard();
    /*var a = new GUIExample();
    a.launch(GUIExample.class, arg);*/
    new GameLauncher().launch(arg);
  }
}
