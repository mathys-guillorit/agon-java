package fr.univ.bordeaux.technical.config;

import fr.univ.bordeaux.technical.io.AbstractFileParser;
import java.io.IOException;
import java.util.List;

/**
 * Parses the game configuration file (typically {@code .agonrc}).
 * <p>
 * This class inherits file reading and comment scrubbing from {@link AbstractFileParser}.
 * It focuses solely on extracting key-value pairs to populate a {@link GameConfig}.
 * </p>
 */
public class ConfigParser extends AbstractFileParser<GameConfig> {

    /**
     * Constructs a new {@code ConfigParser}.
     */
    public ConfigParser() {}

    /**
     * Processes the cleaned lines to construct the GameConfig object.
     *
     * @param cleanLines A list of strings free of comments and empty lines.
     * @return A newly created {@link GameConfig} object.
     */
    @Override
    protected GameConfig processCleanLines(List<String> cleanLines) {
        GameConfig config = new GameConfig();

        for (String line : cleanLines) {
            if (line.startsWith("[")) {
                continue;
            }

            try {
                parseLine(line, config);
            } catch (IOException e) {
                System.err.println("Warning: " + e.getMessage());
            }
        }

        return config;
    }

    /**
     * Parses a single clean line formatted as key=value.
     */
    public void parseLine(String cleanLine, GameConfig config) throws IOException {
        String[] parts = cleanLine.split("=", 2);

        if (parts.length != 2) {
            throw new IOException("Malformed line (no '=') : " + cleanLine);
        }

        String key = parts[0].trim().toLowerCase();
        String value = parts[1].trim();

        try {
            switch (key) {
                case "verbose" -> config.setVerbose(Boolean.parseBoolean(value));
                case "debug" -> config.setDebug(Boolean.parseBoolean(value));
                case "placement" -> config.setManualPlacement(Boolean.parseBoolean(value));
                case "blitz" -> config.setBlitzMode(Boolean.parseBoolean(value));
                case "timeout" -> config.setTimeout(Integer.parseInt(value));
                case "ai" -> config.setAi(Boolean.parseBoolean(value));
                case "ai_color" -> {
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
                        case "NONE" -> {
                            if (config.isAiActive()) {
                                throw new IOException("Ai mode is active but is not assigned to any color");
                            } else {
                                config.setWhiteAI(false);
                                config.setBlackAI(false);
                            }
                        }
                        default -> throw new IOException("Invalid value for option '" + key + "' : " + value);
                    }
                }
                case "ai_mode" -> config.setAiMode(value);
                case "ai_depth" -> config.setAiDepth(Integer.parseInt(value));
                case "ai_time_limit" -> config.setAiTimeLimit(Integer.parseInt(value));
                case "ai_iterative_deepening" -> config.setAiIterativeDeepening(Boolean.parseBoolean(value));
                case "ai_heuristic" -> config.setAiHeuristic(value);
                default -> throw new IOException("Invalid option : " + key);
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid value for option '" + key + "' : " + value);
        }
    }
}