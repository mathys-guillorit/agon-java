package fr.univ.bordeaux.technical.io.storage;

import fr.univ.bordeaux.technical.io.AbstractFileParser;
import fr.univ.bordeaux.technical.io.storage.states.GameState;
import fr.univ.bordeaux.technical.io.storage.states.HistoryState;
import fr.univ.bordeaux.technical.io.storage.states.IdleState;
import fr.univ.bordeaux.technical.io.storage.states.SaveParserState;
import fr.univ.bordeaux.technical.io.storage.states.SettingsState;
import java.io.IOException;
import java.util.List;

/**
 * Parses a cleaned Agon game save file into a structured {@link GameSaveData} object.
 *
 * <p>This parser extends {@link AbstractFileParser} and leverages a <b>State Machine</b> pattern
 * (via {@link SaveParserState}) to sequentially process the distinct sections of a save file. As
 * the parser reads through the file, it dynamically changes its parsing behavior depending on the
 * current section header being processed ({@code [settings]}, {@code [game]}, or {@code
 * [history]}).
 *
 * <p>Data aggregation is delegated to a {@link GameSaveBuilder}, which acts as a central repository
 * for the extracted data and ultimately validates the file's structural integrity before
 * instantiating the final object.
 */
public class GameSaveParser extends AbstractFileParser<GameSaveData> {

  /** The current state of the parser, dictating how the next line of text should be interpreted. */
  private SaveParserState currentState;

  /**
   * Processes a list of pre-cleaned strings (with comments and empty lines removed) to construct a
   * complete {@link GameSaveData} instance.
   *
   * <p>This method iterates through the lines sequentially. If a section header is detected (e.g.,
   * {@code [game]}), it delegates to {@link #switchState(String, GameSaveBuilder)} to update the
   * active parsing state. Otherwise, it delegates the line parsing to the {@code currentState}. If
   * an individual line fails to parse, the error is logged and the parser continues. If the final
   * construction fails due to missing critical sections, it returns {@code null}.
   *
   * @param cleanLines The list of formatted string lines from the save file.
   * @return A fully populated {@link GameSaveData} object, or {@code null} if the file is
   *     structurally invalid.
   */
  @Override
  protected GameSaveData processCleanLines(List<String> cleanLines) {
    GameSaveBuilder builder = new GameSaveBuilder();

    this.currentState = new IdleState();

    for (String line : cleanLines) {
      try {
        if (line.startsWith("[") && line.endsWith("]")) {
          switchState(line.toLowerCase(), builder);
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
   * Switches the internal state machine based on the encountered section header.
   *
   * <p>This method also interacts with the {@link GameSaveBuilder} to flag the presence of
   * essential file sections, ensuring that missing or fully truncated sections are caught during
   * the final validation phase.
   *
   * @param header The section header string (e.g., {@code "[settings]"}) in lowercase.
   * @param builder The builder accumulating the game data, used here to mark section presence.
   * @throws IOException If the provided header string does not match any known valid sections.
   */
  private void switchState(String header, GameSaveBuilder builder) throws IOException {
    switch (header) {
      case "[settings]":
        builder.markSettingsSection();
        this.currentState = new SettingsState();
        break;
      case "[game]":
        builder.markGameSection();
        this.currentState = new GameState();
        break;
      case "[history]":
        builder.markHistorySection();
        this.currentState = new HistoryState();
        break;
      default:
        throw new IOException("Unknown section header: " + header);
    }
  }
}
