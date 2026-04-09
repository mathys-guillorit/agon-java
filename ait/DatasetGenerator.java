import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAi;
import fr.univ.bordeaux.application.ai.strategy.AiFactory;
import fr.univ.bordeaux.technical.io.config.GameConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility class to generate a dataset for Machine Learning training.
 * <p>
 * This script runs MCTS instances against each other to produce thousands of valid
 * game states. To ensure robust learning, the board is scrambled with random moves
 * before recording begins, providing a wide variety of mid-game scenarios.
 */
public class DatasetGenerator {

    /** Number of simulated games to run for data extraction. */
    private static final int NUMBER_OF_GAMES = 100;

    /** Safety limit to prevent infinite loops if the AIs get stuck. */
    private static final int MAX_TURNS_PER_GAME = 350;

    public static void main(String[] args) {
        String csvFilePath = "tfdata/agon_dataset.csv";

        GameConfig config = new GameConfig();
        config.setAiMode("mcts");
        config.setAiHeuristic("uct");

        try (FileWriter writer = new FileWriter(csvFilePath)) {
            writer.write("WhiteQueenDist,BlackQueenDist,GuardPosAdvantage,CaptureAdvantage,WhiteMobility,BlackMobility,Winner\n");

            int successfulGames = 0;
            for (int i = 0; i < NUMBER_OF_GAMES; i++) {
                System.out.print("Simulating game " + (i + 1) + " / " + NUMBER_OF_GAMES + " ... ");
                boolean gameFinished = playAndRecordGame(config, writer);

                if (gameFinished) {
                    successfulGames++;
                    System.out.println("Finished with a winner!");
                } else {
                    System.out.println("Draw/Stuck (Ignored)");
                }
            }

            System.out.println("\nGeneration complete! " + successfulGames + " valid games saved to: " + csvFilePath);

        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    /**
     * Simulates a single game between two AIs and records the board features at each turn.
     *
     * @param config The game configuration for the AIs.
     * @param writer The FileWriter to output the CSV lines.
     * @return {@code true} if the game ended with a clear winner, {@code false} if it reached the turn limit.
     * @throws IOException If an error occurs during file writing.
     */
    private static boolean playAndRecordGame(GameConfig config, FileWriter writer) throws IOException {
        AgonBoardImpl board = new AgonBoardImpl();
        board.initBaseConfiguration();

        int randomScrambleDepth = 5 + new Random().nextInt(16);
        Color startingPlayer = scrambleBoard(board, randomScrambleDepth);

        AbstractAgonAi whiteAi = AiFactory.createAi(config, Color.WHITE);
        AbstractAgonAi blackAi = AiFactory.createAi(config, Color.BLACK);

        whiteAi.setTimeLimit(800);
        blackAi.setTimeLimit(800);

        List<String> gameHistoryFeatures = new ArrayList<>();

        Color currentPlayer = startingPlayer;
        int turnCount = 0;

        while (!board.isGameWon(Color.WHITE) && !board.isGameWon(Color.BLACK)) {
            if (turnCount >= MAX_TURNS_PER_GAME) {
                return false;
            }

            String currentBoardFeatures = extractFeatures(board);
            gameHistoryFeatures.add(currentBoardFeatures);

            AbstractAgonAi currentAi = (currentPlayer == Color.WHITE) ? whiteAi : blackAi;
            Move bestMove = currentAi.getBestMove(board);

            if (bestMove == null) return false;

            board.applyMove(bestMove);

            currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
            turnCount++;
        }

        int winnerLabel = board.isGameWon(Color.WHITE) ? 1 : 0;

        for (String features : gameHistoryFeatures) {
            writer.write(features + "," + winnerLabel + "\n");
        }

        return true;
    }

    /**
     * Plays a sequence of random but legal moves to scramble the board.
     * This prevents the dataset from only learning from standard opening theories.
     *
     * @param board The board to scramble.
     * @param numberOfRandomMovesToPlay The number of random moves to apply.
     * @return The {@link Color} of the player whose turn it is next.
     */
    private static Color scrambleBoard(AgonBoard board, int numberOfRandomMovesToPlay) {
        Random random = new Random();
        Color currentPlayer = Color.WHITE;

        for (int i = 0; i < numberOfRandomMovesToPlay; i++) {
            if (board.isGameWon(Color.WHITE) || board.isGameWon(Color.BLACK)) {
                break;
            }

            List<Move> legalMoves = board.generateLegalMoves(currentPlayer);
            if (legalMoves.isEmpty()) break;

            Move randomMove = legalMoves.get(random.nextInt(legalMoves.size()));
            board.applyMove(randomMove);

            currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
        }

        return currentPlayer;
    }

    /**
     * Extracts numerical features from the board state to feed the Machine Learning algorithm.
     *
     * @param board The current game board.
     * @return A comma-separated string containing the feature values.
     */
    private static String extractFeatures(AgonBoard board) {
        int whiteQueenDist = 6;
        int blackQueenDist = 6;

        int whiteGuardsOnBoard = 0;
        int blackGuardsOnBoard = 0;

        int whiteGuardsCentralitySum = 0;
        int blackGuardsCentralitySum = 0;

        for (int i = 0; i <= 120; i++) {
            PieceType piece = board.getPieceAt(i);
            if (piece == null) continue;

            if (piece == PieceType.WHITE_QUEEN) {
                whiteQueenDist = board.getCentrality(i);
            } else if (piece == PieceType.BLACK_QUEEN) {
                blackQueenDist = board.getCentrality(i);
            } else if (piece == PieceType.WHITE_PAWN) {
                whiteGuardsOnBoard++;
                whiteGuardsCentralitySum += board.getCentrality(i);
            } else if (piece == PieceType.BLACK_PAWN) {
                blackGuardsOnBoard++;
                blackGuardsCentralitySum += board.getCentrality(i);
            }
        }

        int guardPositionAdvantage = whiteGuardsCentralitySum - blackGuardsCentralitySum;

        int captureAdvantage = whiteGuardsOnBoard - blackGuardsOnBoard;

        int whiteMobility = board.generateLegalMoves(Color.WHITE).size();
        int blackMobility = board.generateLegalMoves(Color.BLACK).size();

        return whiteQueenDist + "," + blackQueenDist + "," + guardPositionAdvantage + "," + captureAdvantage + "," + whiteMobility + "," + blackMobility;
    }
}