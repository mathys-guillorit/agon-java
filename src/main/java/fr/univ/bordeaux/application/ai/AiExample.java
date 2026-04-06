package fr.univ.bordeaux.application.ai;

/** Run an Example using Ai. */
public class AiExample {

  /*private AgonBoardImpl createCustomBoard(
      int whiteQueenIdx,
      int blackQueenIdx,
      List<Integer> whitePawnsIdx,
      List<Integer> blackPawnsIdx) {
    BitBoard whiteQ = new BitBoard();
    BitBoard blackQ = new BitBoard();
    BitBoard whiteP = new BitBoard();
    BitBoard blackP = new BitBoard();

    if (whiteQueenIdx != -1) {
      whiteQ.setBit(whiteQueenIdx, 1L);
    }
    if (blackQueenIdx != -1) {
      blackQ.setBit(blackQueenIdx, 1L);
    }

    for (Integer idx : whitePawnsIdx) {
      whiteP.setBit(idx, 1L);
    }
    for (Integer idx : blackPawnsIdx) {
      blackP.setBit(idx, 1L);
    }

    return new AgonBoardImpl(whiteQ, blackQ, whiteP, blackP);
  }*/

  /*/** Explicit nothing to add.
  public void simulateGameAiVsRandomBot() {
    GameLogger.info("\n=== GAME SIMULATION : AI (WHITE) vs RANDOM (BLACK) ===");

    int whiteQ = CoordinateMapper.toIndex('A', 1);
    int blackQ = CoordinateMapper.toIndex('K', 11);
    List<Integer> whiteP =
        List.of(
            CoordinateMapper.toIndex('A', 2),
            CoordinateMapper.toIndex('A', 3),
            CoordinateMapper.toIndex('A', 4),
            CoordinateMapper.toIndex('A', 5),
            CoordinateMapper.toIndex('A', 6),
            CoordinateMapper.toIndex('B', 7));
    List<Integer> blackP =
        List.of(
            CoordinateMapper.toIndex('K', 10),
            CoordinateMapper.toIndex('K', 9),
            CoordinateMapper.toIndex('K', 8),
            CoordinateMapper.toIndex('K', 7),
            CoordinateMapper.toIndex('K', 6),
            CoordinateMapper.toIndex('J', 5));

    AgonBoardImpl board = createCustomBoard(whiteQ, blackQ, whiteP, blackP);

    Heuristic heuristic = new CentralityHeuristic();
    MinimaxStrategy ai = new MinimaxStrategy(heuristic, Color.WHITE, 4, false, 10000);

    int maxTurns = 40;
    for (int i = 1; i <= maxTurns; i++) {
      GameLogger.info("\n---------------- TURN " + i + " ----------------");

      GameLogger.info("AI playing :");
      Move aiMove = ai.getBestMove(board);

      if (aiMove == null) {
        GameLogger.info("AI cannot find a move");
        break;
      }

      board.applyMove(aiMove);
      board.printBoard();

      if (board.isGameWon(Color.WHITE)) {
        GameLogger.info("AI won");
        break;
      }

      GameLogger.info("Random playing :");
      List<Move> blackMoves = board.generateLegalMoves(Color.BLACK);

      if (blackMoves.isEmpty()) {
        GameLogger.info("Random cannot find a move");
        break;
      }

      Move randomMove = blackMoves.get((int) (Math.random() * blackMoves.size()));

      board.applyMove(randomMove);
      board.printBoard();

      if (board.isGameWon(Color.BLACK)) {
        GameLogger.info("Random won");
        break;
      }
    }
    GameLogger.info("\n=== END ===");
  }*/

  /*/** Explicit name nothing to add.
  public void simulateGameAiVsAi() {
    GameLogger.info("\n=== GAME SIMULATION : AI (WHITE) vs AI (BLACK) ===");

    int whiteQ = CoordinateMapper.toIndex('A', 1);
    int blackQ = CoordinateMapper.toIndex('K', 11);
    List<Integer> whiteP =
        List.of(
            CoordinateMapper.toIndex('A', 2),
            CoordinateMapper.toIndex('A', 3),
            CoordinateMapper.toIndex('A', 4),
            CoordinateMapper.toIndex('A', 5),
            CoordinateMapper.toIndex('A', 6),
            CoordinateMapper.toIndex('B', 7));
    List<Integer> blackP =
        List.of(
            CoordinateMapper.toIndex('K', 10),
            CoordinateMapper.toIndex('K', 9),
            CoordinateMapper.toIndex('K', 8),
            CoordinateMapper.toIndex('K', 7),
            CoordinateMapper.toIndex('K', 6),
            CoordinateMapper.toIndex('J', 5));

    AgonBoardImpl board = createCustomBoard(whiteQ, blackQ, whiteP, blackP);

    Heuristic heuristic = new MixedHeuristic(10, 1);
    MinimaxStrategy wwhiteAi = new MinimaxStrategy(heuristic, Color.WHITE, 4, false, 10000);
    MinimaxStrategy blackAi = new MinimaxStrategy(heuristic, Color.BLACK, 4, false, 10000);

    int maxTurns = 40;
    for (int i = 1; i <= maxTurns; i++) {
      GameLogger.info("\n---------------- TURN " + i + " ----------------");

      GameLogger.info("WHITE AI playing :");
      Move whiteAiMove =
          wwhiteAi.getBestMove(
              new AgonBoardImpl(
                  board.getWhiteQueen().copy2(),
                  board.getBlackQueen().copy2(),
                  board.getWhitePawns().copy2(),
                  board.getBlackPawns().copy2()));

      if (whiteAiMove == null) {
        GameLogger.info("WHITE AI cannot find a move");
        break;
      }

      board.applyMove(whiteAiMove);
      board.printBoard();

      if (board.isGameWon(Color.WHITE)) {
        GameLogger.info("WHITE AI won");
        break;
      }

      GameLogger.info("BLACK AI playing :");
      Move blackAiMove =
          blackAi.getBestMove(
              new AgonBoardImpl(
                  board.getWhiteQueen().copy2(),
                  board.getBlackQueen().copy2(),
                  board.getWhitePawns().copy2(),
                  board.getBlackPawns().copy2()));

      if (blackAiMove == null) {
        GameLogger.info("BLACK AI cannot find a move");
        break;
      }

      board.applyMove(blackAiMove);
      board.printBoard();

      if (board.isGameWon(Color.BLACK)) {
        GameLogger.info("BLACK AI won");
        break;
      }
    }
    GameLogger.info("\n=== END ===");
  }*/

  /*/**
   * Run ai.
   *
   * @param args options for setting the Ai (configuration).

  public static void main(String[] args) {
    AiExample aiExample = new AiExample();

    aiExample.simulateGameAiVsAi();

    aiExample.simulateGameAiVsRandomBot();
  }*/
}
