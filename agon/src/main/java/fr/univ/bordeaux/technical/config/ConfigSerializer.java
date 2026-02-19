package fr.univ.bordeaux.technical.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigSerializer {

    public ConfigSerializer() {}

    /**
     * Crée un fichier de configuration avec les valeurs par défaut.
     */
    public void createDefault(String filePath) throws IOException {
        GameConfig defaultConfig = new GameConfig();
        save(defaultConfig, filePath);
    }

    /**
     * Sérialise un objet GameConfig dans un fichier au format INI.
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