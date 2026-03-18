package fr.univ.bordeaux.technical.io.storage;

import fr.univ.bordeaux.technical.io.AbstractFileParser;
import fr.univ.bordeaux.technical.io.storage.states.*;
import java.io.IOException;
import java.util.List;

/**
 * Parses an Agon game save file into a {@link GameSaveData} object.
 * <p>
 * This parser uses a <b>State Machine</b> to handle the different formats
 * of each section ({@code [settings]}, {@code [game]}, {@code [history]}).
 * </p>
 */
public class GameSaveParser extends AbstractFileParser<GameSaveData> {

    private SaveParserState currentState;

    @Override
    protected GameSaveData processCleanLines(List<String> cleanLines) {
        GameSaveBuilder builder = new GameSaveBuilder();

        this.currentState = new IdleState();

        for (String line : cleanLines) {
            try {
                if (line.startsWith("[") && line.endsWith("]")) {
                    switchState(line.toLowerCase());
                    continue;
                }

                this.currentState.parseLine(line, builder);

            } catch (IOException e) {
                System.err.println("Save parsing error: " + e.getMessage());
            }
        }

        try {
            return builder.build();
        } catch (IOException e) {
            System.err.println("Failed to build save data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Switches the internal state based on the section header.
     */
    private void switchState(String header) throws IOException {
        switch (header) {
            case "[settings]":
            case "[system]":
            case "[ai_setup]":
            case "[ai_tuning]":
                this.currentState = new SettingsState();
                break;
            case "[game]":
                this.currentState = new GameState();
                break;
            case "[history]":
                this.currentState = new HistoryState();
                break;
            default:
                throw new IOException("Unknown section header: " + header);
        }
    }
}