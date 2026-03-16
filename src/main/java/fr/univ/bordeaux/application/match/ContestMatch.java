package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.application.ai.strategy.AbstractAgonAi;
import fr.univ.bordeaux.application.ai.strategy.AiFactory;
import fr.univ.bordeaux.technical.utils.SaveParser;

/**
 * Independent executor for the "Contest" mode of the Agon game.
 * <p>
 * This class provides a streamlined execution path that bypasses the heavy-weight
 * logic of the standard Match class. It is specifically designed for automated
 * evaluation servers where only a single AI-calculated move is required from
 * a given board state.
 * </p>
 * * @see fr.univ.bordeaux.application.ai.strategy.AiFactory
 * @see fr.univ.bordeaux.technical.utils.SaveParser
 */
public class ContestMatch {

    /**
     * Executes a single move calculation for a contest scenario.
     * <p>
     * The process follows these steps:
     * <ol>
     * <li>Parses the game state from the provided file path.</li>
     * <li>Instantiates a high-performance AI strategy (Hint AI) for the current player.</li>
     * <li>Calculates the optimal move based on the current board configuration.</li>
     * <li>Outputs the move's string representation to standard output (System.out).</li>
     * </ol>
     * If no valid move is found, an error message is printed to standard error.
     * </p>
     *
     * @param filePath The absolute or relative path to the Agon save file (.txt).
     * @throws Exception If an error occurs during file reading, parsing, or AI calculation.
     */
    public static void executeContest(String filePath) throws Exception {
        SaveParser parser = new SaveParser();
        SaveParser.GameState state = parser.parse(filePath);
        Color playerColor = state.currentPlayer();
        char playerChar  = (playerColor == Color.BLACK) ? 'X' : 'O';
        AbstractAgonAi aiStrategy = AiFactory.createHintAi(playerColor);
        Move bestMove = aiStrategy.getBestMove(state.board());

        if (bestMove != null) {
            String move = playerChar + " " + indexToCoordinate(bestMove.getFrom()) + " " + indexToCoordinate(bestMove.getTo());
            System.out.println(move);
        } else {
            System.err.println("[ERROR] The AI could not find any valid move.");
        }
    }

    /**
     * Converts a 1D bitboard index into a human-readable board coordinate string.
     * <p>
     * This method reverses the linear mapping logic to find the original 2D position
     * on an 11-column grid. It assumes that:
     * <ul>
     * <li>Rows are represented by letters starting from 'A' (calculated using integer division: {@code index / 11}).</li>
     * <li>Columns are represented by 1-based integers (calculated using the modulo operator: {@code (index % 11) + 1}).</li>
     * </ul>
     * For example, it translates an internal index back into a format like "F6" or "H5".
     * </p>
     *
     * @param index The 0-indexed position within the 1D bitboard array.
     * @return A string representing the algebraic coordinate on the board.
     */
    private static String indexToCoordinate(int index) {
        char letter = (char) ('A' + (index / 11));
        int col = (index % 11) + 1;
        return "" + letter + col;
    }
}