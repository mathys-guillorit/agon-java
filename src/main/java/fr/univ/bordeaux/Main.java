package fr.univ.bordeaux;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agoncore.bitboard.BitBoard;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.GameLauncher;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.ui.cli.AgonShell;

/**
 * @version Java 21 (Microsoft OpenJdk 21.0.9)<br> - test programm using <code>mvn test</code>
 * (don't require to compile before)<br> - compile program using <code>mvn compile</code> <br> -
 * exec using : <code>mvn exec:java</code> (require to compile before)<br> - create jar package :
 * <code>mvn package</code>
 * from "/agon" repertory.
 * @version Java 21 (Microsoft OpenJdk 21.0.9)<br>
 * - test programm using <code>mvn test</code> (don't require to compile before)<br>
 * - compile program using <code>mvn compile</code> <br>
 * - exec using : <code>mvn exec:java</code> (require to compile before)<br>
 * - create jar package : <code>mvn package</code>
 *
 * from "/agon" repertory
 */
public class Main {

  private static final int DEFAULT_PORT = 12345;

  public static void main(String[] args) throws Exception {

    // =========================================================
    // CASE 1: Daemon mode (-d / --daemon)
    // Starts the server without launching the interactive shell.
    // =========================================================
    if (args != null
            && args.length > 0
            && ("-d".equals(args[0]) || "--daemon".equals(args[0]))) {

      AgonServer server = new AgonServer(DEFAULT_PORT);

      if (server.start()) {
        System.out.println("[SERVER] Daemon mode enabled.");
        System.out.println("[SERVER] Running on port " + server.getPort() + " without interface.");

        while (server.isRunning()) {
          Thread.sleep(1000);
        }
      } else {
        System.out.println("[SERVER] Failed to start daemon mode.");
      }

      return;
    }

    // =========================================================
    // CASE 2: Server mode (-s [PORT] / --server [PORT])
    // Starts the server directly on the given port.
    // =========================================================
    if (args != null
            && args.length > 0
            && ("-s".equals(args[0]) || "--server".equals(args[0]))) {

      int port = DEFAULT_PORT;

      if (args.length > 1) {
        try {
          port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
          System.out.println("[SERVER] Invalid port. Using default port " + DEFAULT_PORT + ".");
        }
      }

      AgonServer server = new AgonServer(port);

      if (server.start()) {
        System.out.println("[SERVER] Server mode enabled.");
        System.out.println("[SERVER] Running on port " + server.getPort() + ".");

        while (server.isRunning()) {
          Thread.sleep(1000);
        }
      } else {
        System.out.println("[SERVER] Failed to start server mode.");
      }

      return;
    }

    // =========================================================
    // CASE 3: Normal mode
    // Delegates startup to the GameLauncher.
    // =========================================================
    GameLauncher launcher = new GameLauncher();
    launcher.launch(args);

    //System.out.println("prog principal OK + ajout JUnit");

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
    AgonBoardImpl board = new AgonBoardImpl(
      whiteQueen, blackQueen, whitePawns, blackPawns
    );
    board.printBoard();
    System.out.println(board.applyMove(new Move(
      CoordinateMapper.toIndex('G',9),
      CoordinateMapper.toIndex('F',9),
      Color.BLACK)
    ));
    System.out.println(board.applyMove(new Move(
      CoordinateMapper.toIndex('F',8),
      CoordinateMapper.toIndex('F',7),
      Color.WHITE)
    ));
    System.out.println(board.applyMove(new Move(
      CoordinateMapper.toIndex('F',9),
      CoordinateMapper.toIndex('E',8),
      Color.BLACK)
    ));
    board.printBoard();
    board.undoMove();
    board.printBoard();
    System.out.println("board avant mouvement");
    board.printBoard();
    board.applyMove(new Move(
      CoordinateMapper.toIndex('G',9),
      CoordinateMapper.toIndex('F',8),
      Color.BLACK
    ));
    System.out.println("board après mouvement");
    board.printBoard();*/
    /*var a = new GUIExample();
    a.launch(GUIExample.class, arg);*/

    //    var a = new LangService();
    //    a.translate("no");
    //    if(a.setLocale(Locale.FRENCH))
    //      System.out.println("lang found");
    //    System.out.println(a.translate("no"));
  }
}
