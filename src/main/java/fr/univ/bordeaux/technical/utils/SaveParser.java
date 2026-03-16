package fr.univ.bordeaux.technical.utils;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agonCore.bitboard.BitBoard;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for parsing Agon game save files.
 * <p>
 * Refactored to match the design pattern of ConfigParser:
 * Reads line by line, maintains parsing state, and delegates processing.
 * </p>
 */
public class SaveParser {

    /**
     * Accumulates the parsed characters representing the game board.
     */
    private final StringBuilder gameData;
    /**
     * Indicates whether the parser is currently reading inside the [game] section.
     */
    private boolean inGameSection;
    /**
     * Indicates whether the parser is currently ignoring a multi-line block comment.
     */
    private boolean inBlockComment;

    /**
     * Constructs a new {@code SaveParser} with initialized default states.
     */
    public SaveParser() {
        this.inGameSection = false;
        this.inBlockComment = false;
        this.gameData = new StringBuilder();
    }

    /**
     * Parses the save file located at the specified file path.
     *
     * @param filePath The path to the save file.
     * @return A {@link GameState} object containing the parsed board and the active player.
     * @throws IOException If the file does not exist or cannot be read.
     * @throws IllegalArgumentException If the "[game]" section is missing or empty.
     */
    public GameState parse(String filePath) throws IOException, IllegalArgumentException {
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new IOException("Save file does not exist : " + filePath);
        }
        this.inGameSection = false;
        this.inBlockComment = false;
        this.gameData.setLength(0);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
        }

        if (gameData.isEmpty()) {
            throw new IllegalArgumentException("Missing or empty [game] section in the file.");
        }

        return buildGameState(gameData.toString());
    }

    /**
     * Parses a single line, strips comments, and collects game data if within
     * the valid game section.
     *
     * @param line The raw line read from the file to parse.
     */
    private void parseLine(String line) {
        String cleanedLine = removeComments(line).replaceAll("\\s+", "");
        if (cleanedLine.isEmpty()) {
            return;
        }
        if (cleanedLine.equals("[game]")) {
            inGameSection = true;
            return;
        } else if (cleanedLine.startsWith("[")) {
            inGameSection = false;
            return;
        }
        if (inGameSection) {
            gameData.append(cleanedLine);
        }
    }

    /**
     * Removes line comments starting with {@code #} and handles multi-line
     * block comments enclosed in {@code {}}.
     * * @param line The string line from which comments should be removed.
     *
     * @return A clean string containing no comment characters.
     */
    private String removeComments(String line) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inBlockComment) {
                if (c == '}') inBlockComment = false;
                continue;
            }
            if (c == '{') {
                inBlockComment = true;
                continue;
            }
            if (c == '#') {
                break;
            }

            result.append(c);
        }
        return result.toString();
    }

    /**
     * Reconstructs the game state from the accumulated clean characters.
     * * @param cleaned The contiguous string of parsed characters representing the board state.
     *
     * @return A populated {@link GameState} instance.
     */
    private GameState buildGameState(String cleaned) {
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
     *
     * @param board         The reconstructed game board.
     * @param currentPlayer The color of the player whose turn it is to move.
     */
    public record GameState(AgonBoardImpl board, Color currentPlayer) {
    }
}