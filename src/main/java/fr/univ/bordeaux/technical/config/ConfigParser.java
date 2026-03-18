package fr.univ.bordeaux.technical.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Parses the game configuration file (typically {@code .agonrc}).
 *
 * <p>This class is responsible for reading the configuration file line by line, ignoring comments
 * and section headers, and populating a {@link GameConfig} instance with the extracted key-value
 * pairs.
 */
public class ConfigParser {

  /** Constructs a new {@code ConfigParser}. */
  public ConfigParser() {}

  /**
   * Parses the configuration file located at the specified file path. * @param filePath The path to
   * the configuration file (e.g., {@code ".agonrc"}).
   *
   * @return A newly created {@link GameConfig} object populated with the parsed settings.
   * @throws IOException If the configuration file does not exist, cannot be read, or contains
   *     malformed data.
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

  /**
   * Parses a single line from the configuration file and applies the setting to the provided {@link
   * GameConfig} object.
   *
   * <p>This method safely ignores empty lines, comments (starting with {@code #}), and section
   * headers (starting with {@code [}). Valid configuration lines must strictly follow the {@code
   * key=value} format. * @param line The configuration line to parse.
   *
   * @param config The {@link GameConfig} instance to update.
   * @throws IOException If the line is malformed (missing the {@code =} delimiter), if a value
   *     cannot be parsed into its expected data type (e.g., {@link NumberFormatException}), or if
   *     an unknown key is provided.
   */
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
        case "placement":
          config.setManualPlacement(Boolean.parseBoolean(value));
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
              config.setWhiteAi(true);
              config.setBlackAi(true);
            }
            case "WHITE" -> {
              config.setWhiteAi(true);
              config.setBlackAi(false);
            }
            case "BLACK" -> {
              config.setWhiteAi(false);
              config.setBlackAi(true);
            }
            case "NONE" -> {
              if (config.isAiActive()) {
                throw new IOException("Ai mode is active but is not assigned to any color");
              } else {
                config.setWhiteAi(false);
                config.setBlackAi(false);
              }
            }
            default -> {
              throw new IOException("Invalid value for option '" + key + "' : " + value);
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
          throw new IOException("Invalid option : " + key);
      }
    } catch (NumberFormatException e) {
      throw new IOException("Invalid value for option '" + key + "' : " + value);
    }
  }
}
