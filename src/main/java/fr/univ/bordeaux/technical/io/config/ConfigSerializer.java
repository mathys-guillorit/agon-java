package fr.univ.bordeaux.technical.io.config;

import fr.univ.bordeaux.technical.io.Serializer;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Handles the serialization of game configuration settings into a file.
 *
 * <p>This class is responsible for writing a {@link GameConfig} object into a plain text file using
 * an INI-like format (with sections like {@code [system]} or {@code [game]}). It can also generate
 * a default configuration file if one does not already exist.
 */
public class ConfigSerializer implements Serializer<GameConfig> {

  /** Constructs a new {@code ConfigSerializer}. */
  public ConfigSerializer() {}

  /**
   * Creates a configuration file populated with the default settings.
   *
   * <p>This method instantiates a new {@link GameConfig} with its default values and immediately
   * serializes it to the specified file path.
   *
   * @param filePath The destination path for the default configuration file.
   * @throws IOException If the file already exists, or if an I/O error occurs while writing.
   */
  public void createDefault(final String filePath) throws IOException {
    if (Files.exists(Paths.get(filePath))) {
      throw new IOException("File " + filePath + " already exists");
    }
    final GameConfig defaultConfig = new GameConfig();
    save(defaultConfig, filePath);
  }

  /**
   * Serializes a {@link GameConfig} object and writes it to a file in an INI format.
   *
   * <p>The output file will be organized into logical sections such as {@code [system]}, {@code
   * [game]}, {@code [ai_setup]}, and {@code [ai_tuning]}. If the file already exists, it will be
   * overwritten.
   *
   * @param config The {@link GameConfig} instance containing the settings to save.
   * @param filePath The destination path where the configuration file will be saved.
   * @throws IOException If an I/O error occurs while opening or writing to the file.
   */
  @Override
  public void save(final GameConfig config, final String filePath) throws IOException {
    final Path path = Paths.get(filePath);

    try (BufferedWriter writer = Files.newBufferedWriter(path)) {

      writer.write("[system]\n");
      writer.write("verbose = " + config.isVerbose() + "\n");
      writer.write("debug = " + config.isDebug() + "\n");
      writer.write("placement = " + config.isManualPlacement() + "\n\n");

      writer.write("[game]\n");
      writer.write("blitz = " + config.isBlitzMode() + "\n");
      writer.write("timeout = " + config.getTimeout() + "\n\n");

      writer.write("[ai_setup]\n");
      writer.write("ai = " + config.isAiActive() + "\n");
      String colorStr = "NONE";
      if (config.isWhiteAi() && config.isBlackAi()) {
        colorStr = "ALL";
      } else if (config.isWhiteAi()) {
        colorStr = "WHITE";
      } else if (config.isBlackAi()) {
        colorStr = "BLACK";
      }
      writer.write("ai_color = " + colorStr + "\n\n");

      writer.write("[ai_tuning]\n");
      writer.write("ai_mode = " + config.getAiMode() + "\n");
      writer.write("ai_depth = " + config.getAiDepth() + "\n");
      writer.write("ai_time_limit = " + config.getAiTimeLimit() + "\n");
      writer.write("ai_iterative_deepening = " + config.isAiIterativeDeepening() + "\n");
      writer.write("ai_heuristic = " + config.getAiHeuristic() + "\n");

      writer.write("[shortcuts]\n");
      final Map<String, String> shortcuts = config.getShortcuts();
      for (final Map.Entry<String, String> entry : shortcuts.entrySet()) {
        final String key = entry.getKey();
        final String value = entry.getValue();
        writer.write(key + " = " + value + "\n");
      }

    }
  }
}
