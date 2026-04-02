package fr.univ.bordeaux.ui;

import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;

/**
 * Utility class to parse terminal input into executable {@link CmdAction} instances. The parser
 * follows a two-step logic:
 *
 * <ol>
 *   <li>It checks if the input matches a command registered in the {@link AgonRegister} (e.g.,
 *       "help", "save").
 *   <li>If no command matches, it attempts to parse the input as a move (e.g., "a1b2") or a
 *       relocation (e.g., "a1") using Regex patterns.
 * </ol>
 */
public class UiPromptParser {

  /** JLine parser used to split input lines into words, handling quotes and escapes. */
  private static final Parser parser = new DefaultParser();

  /** Pattern for standard moves: origin (letter+digit) + destination (letter+digit). Ex: "a1b2" */
  private static final Pattern MOVE_PATTERN =
      Pattern.compile("^([a-k])(\\d{1,2})([a-k])(\\d{1,2})$");

  /** Pattern for relocation moves (one coordinate). Ex: "a1" */
  private static final Pattern RELOCATION_PATTERN = Pattern.compile("^([a-k])(\\d{1,2})$");

  /**
   * Parses a raw line from the terminal and returns the corresponding {@link CmdAction}.
   *
   * @param line The raw string entered by the user.
   * @param registry The command registry containing available keywords.
   * @param ui The user interface context to pass to newly created commands.
   * @return A {@link CmdAction} ready for execution, or {@code null} if the input is invalid or
   *     empty.
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
      return null;
    }

    final List<String> words = parsed.words();
    if (words.isEmpty()) {
      return null;
    }

    String firstWord = words.get(0).toLowerCase();

    // 1. Try compound commands such as "server start" -> "server_start"
    if (words.size() >= 2) {
      String compoundCmdName = firstWord + "_" + words.get(1).toLowerCase();
      String[] compoundOptions = words.subList(2, words.size()).toArray(String[]::new);

      Optional<CmdAction> compoundCmd = registry.get(compoundCmdName);
      if (compoundCmd.isPresent()) {
        return compoundCmd.get().createNew(compoundOptions);
      }
    }

    // 2. Fallback to classic one-word commands such as "join"
    String[] options = words.subList(1, words.size()).toArray(String[]::new);

    return registry
            .get(firstWord)
            .map(action -> action.createNew(options))
            .orElseGet(() -> handleDefault(line, ui));
  }

  /**
   * Fallback method to handle inputs that are not registered commands.
   *
   * <p>Uses {@code MOVE_PATTERN} and {@code RELOCATION_PATTERN} to detect if the user typed raw
   * coordinates to move a piece.
   *
   * @param input The raw input string.
   * @param ui The UI context.
   * @return A {@link CmdMove} instance if coordinates are valid, {@code null} otherwise.
   */
  private static CmdAction handleDefault(String input, GameUserInterface ui) {
    Matcher moveMatcher = MOVE_PATTERN.matcher(input.toLowerCase());
    Matcher relocationMatcher = RELOCATION_PATTERN.matcher(input.toLowerCase());

    // Case 1: Standard Move (e.g., a1b2)
    if (moveMatcher.matches()) {
      char letterFrom = moveMatcher.group(1).charAt(0);
      int colFrom = Integer.parseInt(moveMatcher.group(2));
      char letterTo = moveMatcher.group(3).charAt(0);
      int colTo = Integer.parseInt(moveMatcher.group(4));

      if (isValidCoord(colFrom) && isValidCoord(colTo)) {
        int indexFrom = CoordinateMapper.toIndex(Character.toUpperCase(letterFrom), colFrom);
        int indexTo = CoordinateMapper.toIndex(Character.toUpperCase(letterTo), colTo);
        return new CmdMove(indexFrom, indexTo, ui);
      } else {
        ui.showError("Error: Coordinates out of bounds (1-11). Example: 'a1b1'.\n");
        return null;
      }
    }

    // Case 2: Relocation (e.g., a1)
    if (relocationMatcher.matches()) {
      char letter = relocationMatcher.group(1).charAt(0);
      int col = Integer.parseInt(relocationMatcher.group(2));

      if (isValidCoord(col)) {
        return new CmdMove(-1, CoordinateMapper.toIndex(Character.toUpperCase(letter), col), ui);
      } else {
        ui.showError("Error: Coordinate out of bounds (1-11). Example: 'a1'.\n");
        return null;
      }
    }

    return null;
  }

  /**
   * Checks if a column number is within the valid Agon board range.
   *
   * @param col The column index to check.
   * @return true if between 1 and 11 inclusive.
   */
  private static boolean isValidCoord(int col) {
    return col >= 1 && col <= 11;
  }
}
