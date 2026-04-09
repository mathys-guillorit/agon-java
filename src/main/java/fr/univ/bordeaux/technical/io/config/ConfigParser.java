package fr.univ.bordeaux.technical.io.config;

import fr.univ.bordeaux.technical.io.AbstractFileParser;
import java.io.IOException;
import java.util.List;

/**
 * Parses the game configuration file (typically {@code .agonrc}).
 *
 * <p>This class inherits file reading and comment scrubbing from {@link AbstractFileParser}. It
 * focuses solely on extracting key-value pairs to populate a {@link GameConfig}.
 */
public class ConfigParser extends AbstractFileParser<GameConfig> {

  /** Prefix indicating a new section in the configuration file. */
  private static final String SECTION_PREFIX = "[";

  /**
   * Processes the cleaned lines to construct the GameConfig object.
   *
   * @param cleanLines A list of strings free of comments and empty lines.
   * @return A newly created {@link GameConfig} object.
   */
  @Override
  protected GameConfig processCleanLines(final List<String> cleanLines) throws IOException {
    final GameConfig config = new GameConfig();

    for (final String line : cleanLines) {
      if (line.startsWith(SECTION_PREFIX)) {
        continue;
      }
      parseLine(line, config);
    }

    return config;
  }

  /** Parses a single clean line formatted as key=value. */
  public void parseLine(final String cleanLine, final GameConfig config) throws IOException {
    final String[] parts = cleanLine.split("=", 2);

    if (parts.length != 2) {
      throw new IOException("Malformed line (no '=') : " + cleanLine);
    }

    final String key = parts[0].trim().toLowerCase();
    final String value = parts[1].trim();

    if (key.startsWith("shortcut_")) {
      config.addShortcut(key, value);
      return;
    }

    if (key.startsWith("shortcut_")) {
      config.addShortcut(key, value);
      return;
    }

    try {
      applySetting(key, value, config);
    } catch (NumberFormatException e) {
      throw new IOException("Invalid value for option '" + key + "' : " + value, e);
    }
  }

  /**
   * Applies a specific setting to the configuration object.
   *
   * @param key    The configuration key.
   * @param value  The configuration value to be parsed.
   * @param config The configuration object to update.
   * @throws IOException If the key is unknown.
   */
  private void applySetting(final String key, final String value, final GameConfig config) throws IOException {
    switch (key) {
      case "verbose" -> config.setVerbose(Boolean.parseBoolean(value));
      case "debug" -> config.setDebug(Boolean.parseBoolean(value));
      case "placement" -> config.setManualPlacement(Boolean.parseBoolean(value));
      case "blitz" -> config.setBlitzMode(Boolean.parseBoolean(value));
      case "timeout" -> config.setTimeout(Integer.parseInt(value));
      case "ai" -> config.setAi(Boolean.parseBoolean(value));
      case "ai_color" -> applyAiColor(value, config);
      case "ai_mode" -> config.setAiMode(value);
      case "ai_depth" -> config.setAiDepth(Integer.parseInt(value));
      case "ai_time_limit" -> config.setAiTimeLimit(Integer.parseInt(value));
      case "ai_iterative_deepening" -> config.setAiIterativeDeepening(Boolean.parseBoolean(value));
      case "ai_heuristic" -> config.setAiHeuristic(value);
      default -> throw new IOException("Invalid option : " + key);
    }
  }

  /**
   * Applies the AI color setting.
   *
   * @param value  The color configuration value.
   * @param config The configuration object to update.
   * @throws IOException If the color value is invalid or conflicts with AI status.
   */
  private void applyAiColor(final String value, final GameConfig config) throws IOException {
    final String val = value.toUpperCase();
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
      default -> throw new IOException("Invalid value for option 'ai_color' : " + value);
    }
  }
}