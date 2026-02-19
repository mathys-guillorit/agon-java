package fr.univ.bordeaux.technical.config;

import fr.univ.bordeaux.agonCore.agonElements.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigParser {
    public ConfigParser() {}

    /**
     * Parse le fichier de configuration .agonrc
     * @param filePath Le chemin vers le fichier (ex: "agon.rc")
     * @return Un objet GameConfig rempli
     * @throws IOException Si le fichier n'existe pas ou n'est pas lisible
     */
    public GameConfig parse(String filePath) throws IOException {

        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new IOException("Config file does not exist : " + filePath);
        }

        GameConfig config = new GameConfig();

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line, config);
            }
        }

        return config;
    }

    private void parseLine(String line, GameConfig config) throws IOException {
        String cleanLine = line.trim();

        if (cleanLine.isEmpty() || cleanLine.startsWith("#") || cleanLine.startsWith("[")) {
            return;
        }

        String[] parts = cleanLine.split("=", 2);

        if (parts.length != 2) {
            throw new IOException("Malformed line (no '=') : " + line);
        }

        String key = parts[0].trim().toLowerCase();
        String value = parts[1].trim();

        try {
            switch (key) {
                case "verbose":
                    config.setVerbose(Boolean.parseBoolean(value));
                    break;
                case "debug":
                    config.setDebug(Boolean.parseBoolean(value));
                    break;
                case "blitz":
                    config.setBlitzMode(Boolean.parseBoolean(value));
                    break;
                case "timeout":
                    config.setTimeout(Integer.parseInt(value));
                    break;
                case "ai":
                    config.setAi(Boolean.parseBoolean(value));
                    break;
                case "ai_color":
                    String val = value.toUpperCase();
                    switch (val) {
                        case "ALL" -> {
                            config.setWhiteAI(true);
                            config.setBlackAI(true);
                        }
                        case "WHITE" -> {
                            config.setWhiteAI(true);
                            config.setBlackAI(false);
                        }
                        case "BLACK" -> {
                            config.setWhiteAI(false);
                            config.setBlackAI(true);
                        }
                        default -> {
                            config.setWhiteAI(false);
                            config.setBlackAI(false);
                        }
                    }
                    break;
                case "ai_mode":
                    config.setAiMode(value);
                    break;
                case "ai_depth":
                    config.setAiDepth(Integer.parseInt(value));
                    break;
                case "ai_time_limit":
                    config.setAiTimeLimit(Integer.parseInt(value));
                    break;
                case "ai_iterative_deepening":
                    config.setAiIterativeDeepening(Boolean.parseBoolean(value));
                    break;
                case "ai_heuristic":
                    config.setAiHeuristic(value);
                    break;
                default:
                    throw new IOException("Invalid option : "+key);
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid value for option '" + key + "' : " + value);
        }
    }
}