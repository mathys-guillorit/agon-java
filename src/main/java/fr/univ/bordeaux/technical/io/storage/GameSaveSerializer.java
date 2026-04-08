package fr.univ.bordeaux.technical.io.storage;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.technical.io.Serializer;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Handles the serialization of a game state into a save file (.asv).
 *
 * <p>This class writes the {@link GameSaveData} into three distinct sections: {@code [settings]}
 * for the configuration, {@code [game]} for the board state, and {@code [history]} for the played
 * moves.
 */
public class GameSaveSerializer implements Serializer<GameSaveData> {

  /** Constructs a new {@code GameSaveSerializer}. */
  public GameSaveSerializer() {}

  @Override
  public void save(final GameSaveData saveData, final String filePath) throws IOException {
    final Path path = Paths.get(filePath);
    final GameConfig config = saveData.getConfig();
    final Color currentPlayer = saveData.getCurrentPlayer();
    final List<String> boardLines = saveData.getBoardLines();
    final List<String> historyMoves = saveData.getHistoryMoves();

    try (BufferedWriter writer = Files.newBufferedWriter(path)) {

      writer.write("[settings]\n");
      writeConfig(writer, config);
      writer.write("\n");

      writer.write("[game]\n");

      final char playerChar = (currentPlayer == Color.BLACK) ? 'X' : 'O';
      writer.write(playerChar + "\n");

      for (final String line : boardLines) {
        writer.write(line + "\n");
      }
      writer.write("\n");

      writer.write("[history]\n");

      for (int i = 0; i < historyMoves.size(); i++) {
        writer.write(historyMoves.get(i) + ";");

        if ((i + 1) % 2 == 0) {
          writer.write("\n");
        } else if (i < historyMoves.size() - 1) {
          writer.write(" ");
        }
      }

      if (!historyMoves.isEmpty() && historyMoves.size() % 2 != 0) {
        writer.write("\n");
      }
    }
  }

  /**
   * Helper method to accurately dump the GameConfig into key=value format.
   *
   * @param writer The BufferedWriter used for writing to the file.
   * @param config The GameConfig object to serialize.
   * @throws IOException If an I/O error occurs during writing.
   */
  private void writeConfig(final BufferedWriter writer, final GameConfig config) throws IOException {
    writer.write("verbose = " + config.isVerbose() + "\n");
    writer.write("debug = " + config.isDebug() + "\n");
    writer.write("placement = " + config.isManualPlacement() + "\n");
    writer.write("blitz = " + config.isBlitzMode() + "\n");
    writer.write("timeout = " + config.getTimeout() + "\n");
    writer.write("ai = " + config.isAiActive() + "\n");

    String colorStr = "NONE";
    if (config.isWhiteAi() && config.isBlackAi()) {
      colorStr = "ALL";
    } else if (config.isWhiteAi()) {
      colorStr = "WHITE";
    } else if (config.isBlackAi()) {
      colorStr = "BLACK";
    }
    writer.write("ai_color = " + colorStr + "\n");

    writer.write("ai_mode = " + config.getAiMode() + "\n");
    writer.write("ai_depth = " + config.getAiDepth() + "\n");
    writer.write("ai_time_limit = " + config.getAiTimeLimit() + "\n");
    writer.write("ai_iterative_deepening = " + config.isAiIterativeDeepening() + "\n");
    writer.write("ai_heuristic = " + config.getAiHeuristic() + "\n");
  }
}