package fr.univ.bordeaux.technical.io.storage;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.technical.config.GameConfig;
import fr.univ.bordeaux.technical.io.Serializer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Handles the serialization of a game state into a save file (.asv).
 * <p>
 * This class writes the {@link GameSaveData} into three distinct sections:
 * {@code [settings]} for the configuration, {@code [game]} for the board state,
 * and {@code [history]} for the played moves.
 * </p>
 */
public class GameSaveSerializer implements Serializer<GameSaveData> {

    @Override
    public void save(GameSaveData saveData, String filePath) throws IOException {
        Path path = Paths.get(filePath);

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            writer.write("[settings]\n");
            writeConfig(writer, saveData.getConfig());
            writer.write("\n");

            writer.write("[game]\n");

            char playerChar = (saveData.getCurrentPlayer() == Color.BLACK) ? 'X' : 'O';
            writer.write(playerChar + "\n");

            for (String line : saveData.getBoardLines()) {
                writer.write(line + "\n");
            }
            writer.write("\n");

            writer.write("[history]\n");
            List<String> moves = saveData.getHistoryMoves();

            for (int i = 0; i < moves.size(); i++) {
                writer.write(moves.get(i) + ";");

                if ((i + 1) % 2 == 0) {
                    writer.write("\n");
                }
                else if (i < moves.size() - 1) {
                    writer.write(" ");
                }
            }

            if (!moves.isEmpty() && moves.size() % 2 != 0) {
                writer.write("\n");
            }
        }
    }

    /**
     * Helper method to accurately dump the GameConfig into key=value format.
     */
    private void writeConfig(BufferedWriter writer, GameConfig config) throws IOException {
        writer.write("verbose = " + config.isVerbose() + "\n");
        writer.write("debug = " + config.isDebug() + "\n");
        writer.write("placement = " + config.isManualPlacement() + "\n");
        writer.write("blitz = " + config.isBlitzMode() + "\n");
        writer.write("timeout = " + config.getTimeout() + "\n");
        writer.write("ai = " + config.isAiActive() + "\n");

        String colorStr = "NONE";
        if (config.isWhiteAI() && config.isBlackAI()) colorStr = "ALL";
        else if (config.isWhiteAI()) colorStr = "WHITE";
        else if (config.isBlackAI()) colorStr = "BLACK";
        writer.write("ai_color = " + colorStr + "\n");

        writer.write("ai_mode = " + config.getAiMode() + "\n");
        writer.write("ai_depth = " + config.getAiDepth() + "\n");
        writer.write("ai_time_limit = " + config.getAiTimeLimit() + "\n");
        writer.write("ai_iterative_deepening = " + config.isAiIterativeDeepening() + "\n");
        writer.write("ai_heuristic = " + config.getAiHeuristic() + "\n");
    }
}