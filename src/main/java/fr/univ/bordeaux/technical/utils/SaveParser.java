package fr.univ.bordeaux.technical.utils;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agonCore.bitboard.BitBoard;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Utility class for parsing Agon game save files.
 * <p>
 * This parser extracts the board state and the current player from a structured
 * text file. It handles comment stripping (both line and block comments) and
 * reconstructs the hexagonal board using bitboard representations.
 * </p>
 */
public class SaveParser {

    /**
     * Loads and parses an Agon save file to reconstruct the game state.
     * <p>
     * The method identifies the "[game]" section, determines the current player
     * based on the first character ('X' for Black, others for White), and maps
     * the character grid to 64-bit BitBoards.
     * </p>
     * * <b>Supported characters:</b>
     * <ul>
     * <li>'X': Black Pawn</li>
     * <li>'q': Black Queen</li>
     * <li>'O': White Pawn</li>
     * <li>'Q': White Queen</li>
     * <li>'.': Empty cell</li>
     * </ul>
     *
     * @param filePath Path to the save file.
     * @return A {@link GameState} object containing the board and active player.
     * @throws IOException If the file cannot be read.
     * @throws IllegalArgumentException If the "[game]" section is missing.
     */
    public static GameState loadGame(String filePath) throws IOException, IllegalArgumentException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));

        content = content.replaceAll("(?s)\\{.*?\\}", "").replaceAll("#.*", "");

        int gameIdx = content.indexOf("[game]");
        if (gameIdx == -1) throw new IllegalArgumentException("Missing [game] section in the file.");

        int historyIdx = content.indexOf("[history]");
        String gameSection = (historyIdx != -1 && historyIdx > gameIdx) ?
                content.substring(gameIdx + 6, historyIdx) : content.substring(gameIdx + 6);
        String cleaned = gameSection.replaceAll("\\s+", "");
        char playerChar = cleaned.charAt(0);
        Color currentPlayer = (playerChar == 'X' || playerChar == 'x') ? Color.BLACK : Color.WHITE;
        BitBoard wQ = new BitBoard(), bQ = new BitBoard(), wP = new BitBoard(), bP = new BitBoard();
        int charIndex = 1;

        for (int r = 0; r <= 10; r++) {
            for (int c = 0; c <= 10; c++) {
                int q = c - 5, rAxial = r - 5;
                if (Math.max(Math.abs(q), Math.max(Math.abs(rAxial), Math.abs(q + rAxial))) <= 5 && charIndex < cleaned.length()) {
                    char cell = cleaned.charAt(charIndex++);
                    int bitIndex = r * 11 + c;
                    switch (cell) {
                        case 'X': bP.setBit(bitIndex, 1L); break;
                        case 'q': bQ.setBit(bitIndex, 1L); break;
                        case 'O': wP.setBit(bitIndex, 1L); break;
                        case 'Q': wQ.setBit(bitIndex, 1L); break;
                        case '.': break; // Case vide
                    }
                }
            }
        }
        return new GameState(new AgonBoardImpl(wQ, bQ, wP, bP), currentPlayer);
    }

    /**
         * Data wrapper representing the state of a game after parsing.
         */
        public record GameState(AgonBoardImpl board, Color currentPlayer) {
        /**
         * Constructs a GameState.
         * * @param board The initialized bitboard-based board.
         * @param currentPlayer The color of the active player.
         */
        public GameState {
        }
        }
}