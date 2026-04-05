package fr.univ.bordeaux.ui;

import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;
import fr.univ.bordeaux.technical.utils.GameLogger; // Import ajouté
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;

/**
 * Utility class to parse terminal input into executable {@link CmdAction} instances.
 */
public class UiPromptParser {

  private static final Parser parser = new DefaultParser();

  private static final Pattern MOVE_PATTERN =
      Pattern.compile("^([a-k])(\\d{1,2})([a-k])(\\d{1,2})$");

  private static final Pattern RELOCATION_PATTERN = Pattern.compile("^([a-k])(\\d{1,2})$");

  /**
   * Parses a raw line from the terminal and returns the corresponding {@link CmdAction}.
   */
  public static CmdAction parse(
      final String line, AgonRegister<CmdAction> registry, GameUserInterface ui) {
    if (line == null || line.trim().isEmpty()) {
      return null;
    }

    final ParsedLine parsed;
    try {
      parsed = parser.parse(line, 0);
    } catch (Exception e) {
      GameLogger.error("UiPromptParser: JLine parsing failed for input: " + line);
      return null;
    }

    final List<String> words = parsed.words();
    if (words.isEmpty()) {
      return null;
    }

    String cmdName = words.get(0).toLowerCase();
    String[] options = words.subList(1, words.size()).toArray(String[]::new);

    // Tentative de trouver la commande dans le registre
    return registry
        .get(cmdName)
        .map(action -> {
          GameLogger.debug("UiPromptParser: Recognized registered command '" + cmdName + "'");
          return action.createNew(options);
        })
        .orElseGet(() -> {
          GameLogger.debug("UiPromptParser: No registered command found for '" + cmdName + "', falling back to regex.");
          return handleDefault(line, ui);
        });
  }

  /**
   * Fallback method to handle inputs that are not registered commands (Moves/Relocations).
   */
  private static CmdAction handleDefault(String input, GameUserInterface ui) {
    String lowerInput = input.toLowerCase().trim();
    Matcher moveMatcher = MOVE_PATTERN.matcher(lowerInput);
    Matcher relocationMatcher = RELOCATION_PATTERN.matcher(lowerInput);

    // Case 1: Standard Move (e.g., a1b2)
    if (moveMatcher.matches()) {
      GameLogger.debug("UiPromptParser: Input matches MOVE_PATTERN (" + lowerInput + ")");
      char letterFrom = moveMatcher.group(1).charAt(0);
      int colFrom = Integer.parseInt(moveMatcher.group(2));
      char letterTo = moveMatcher.group(3).charAt(0);
      int colTo = Integer.parseInt(moveMatcher.group(4));

      if (isValidCoord(colFrom) && isValidCoord(colTo)) {
        int indexFrom = CoordinateMapper.toIndex(Character.toUpperCase(letterFrom), colFrom);
        int indexTo = CoordinateMapper.toIndex(Character.toUpperCase(letterTo), colTo);
        return new CmdMove(indexFrom, indexTo, ui);
      } else {
        GameLogger.debug("UiPromptParser: Move coordinates out of bounds (1-11).");
        ui.showError("Error: Coordinates out of bounds (1-11). Example: 'a1b1'.\n");
        return null;
      }
    }

    // Case 2: Relocation (e.g., a1)
    if (relocationMatcher.matches()) {
      GameLogger.debug("UiPromptParser: Input matches RELOCATION_PATTERN (" + lowerInput + ")");
      char letter = relocationMatcher.group(1).charAt(0);
      int col = Integer.parseInt(relocationMatcher.group(2));

      if (isValidCoord(col)) {
        return new CmdMove(-1, CoordinateMapper.toIndex(Character.toUpperCase(letter), col), ui);
      } else {
        GameLogger.debug("UiPromptParser: Relocation coordinate out of bounds (1-11).");
        ui.showError("Error: Coordinate out of bounds (1-11). Example: 'a1'.\n");
        return null;
      }
    }

    GameLogger.debug("UiPromptParser: Input '" + lowerInput + "' did not match any command or move pattern.");
    ui.showError("Invalid command. Please use help to display more information.\n");
    return null;
  }

  private static boolean isValidCoord(int col) {
    return col >= 1 && col <= 11;
  }
}