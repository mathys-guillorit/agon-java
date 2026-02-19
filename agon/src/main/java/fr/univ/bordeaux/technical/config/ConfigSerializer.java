package fr.univ.bordeaux.technical.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles the serialization of game configuration settings into a file.
 * <p>
 * This class is responsible for writing a {@link GameConfig} object into a
 * plain text file using an INI-like format (with sections like {@code [system]}
 * or {@code [game]}). It can also generate a default configuration file if
 * one does not already exist.
 * </p>
 */
public class ConfigSerializer {

    /**
     * Constructs a new {@code ConfigSerializer}.
     */
    public ConfigSerializer() {}

    /**
     * Creates a configuration file populated with the default settings.
     * <p>
     * This method instantiates a new {@link GameConfig} with its default
     * values and immediately serializes it to the specified file path.
     * </p>
     *
     * @param filePath The destination path for the default configuration file.
     * @throws IOException If an I/O error occurs while creating or writing to the file.
     */
    public void createDefault(String filePath) throws IOException {
        GameConfig defaultConfig = new GameConfig();
        save(defaultConfig, filePath);
    }

    /**
     * Serializes a {@link GameConfig} object and writes it to a file in an INI format.
     * <p>
     * The output file will be organized into logical sections such as {@code [system]},
     * {@code [game]}, {@code [ai_setup]}, and {@code [ai_tuning]}. If the file
     * already exists, it will be overwritten.
     * </p>
     *
     * @param config   The {@link GameConfig} instance containing the settings to save.
     * @param filePath The destination path where the configuration file will be saved.
     * @throws IOException If an I/O error occurs while opening or writing to the file.
     */
    public void save(GameConfig config, String filePath) throws IOException {
        Path path = Paths.get(filePath);

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            writer.write("[system]\n");
            writer.write("verbose = " + config.isVerbose() + "\n");
            writer.write("debug = " + config.isDebug() + "\n\n");

            writer.write("[game]\n");
            writer.write("blitz = " + config.isBlitzMode() + "\n");
            writer.write("timeout = " + config.getTimeout() + "\n\n");

            writer.write("[ai_setup]\n");
            writer.write("ai = " + config.isAiActive() + "\n");
            String colorStr = "NONE";
            if (config.isWhiteAI() && config.isBlackAI()) colorStr = "ALL";
            else if (config.isWhiteAI()) colorStr = "WHITE";
            else if (config.isBlackAI()) colorStr = "BLACK";
            writer.write("ai_color = " + colorStr + "\n\n");

            writer.write("[ai_tuning]\n");
            writer.write("ai_mode = " + config.getAiMode() + "\n");
            writer.write("ai_depth = " + config.getAiDepth() + "\n");
            writer.write("ai_time_limit = " + config.getAiTimeLimit() + "\n");
            writer.write("ai_iterative_deepening = " + config.isAiIterativeDeepening() + "\n");
            writer.write("ai_heuristic = " + config.getAiHeuristic() + "\n");
        }
    }
}