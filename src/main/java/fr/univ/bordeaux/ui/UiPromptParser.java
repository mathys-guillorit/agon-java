package fr.univ.bordeaux.ui;

import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;
import fr.univ.bordeaux.technical.utils.GameLogger;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;

/**
 * Utility class to parse terminal input into executable {@link CmdAction} instances.
 *
 * <p>The parser follows a multi-step logic:
 * <ol>
 * <li>Checks for compound commands (e.g., "server start" translated to "server_start").</li>
 * <li>Checks if the input matches a standard registered command (e.g., "help", "save").</li>
 * <li>Attempts to parse the input as a move (e.g., "a1b2") or a relocation (e.g., "a1")
 * using Regular Expressions if no command matches.</li>
 * </ol>
 */
public class UiPromptParser {

  /** JLine parser used to split input lines into words, handling quotes and escapes. */
  private static final Parser PARSER = new DefaultParser();

  /** * Regex pattern for standard moves: origin (letter + digit) + destination (letter + digit).
   * Example: "a1b2", "k11a1".
   */
  private static final Pattern MOVE_PATTERN =
      Pattern.compile("^([a-k])(\\d{1,2})([a-k])(\\d{1,2})$");

  /** * Regex pattern for relocation moves (single coordinate).
   * Example: "a1", "f6".
   */
  private static final Pattern RELOCATION_PATTERN = Pattern.compile("^([a-k])(\\d{1,2})$");

  /**
   * Parses a raw line from the terminal and returns the corresponding {@link CmdAction}.
   *
   * @param line The raw string entered by the user in the shell.
   * @param registry The {@link AgonRegister} containing all available command keywords.
   * @param ui The {@link GameUserInterface} context to be injected into the created command.
   * @return A {@link CmdAction} ready for execution, or {@code null} if the input is invalid.
   */
  public static CmdAction parse(
      final String line, AgonRegister<CmdAction> registry, GameUserInterface ui) {
    if (line == null || line.trim().isEmpty()) {
      return null;
    }

    final ParsedLine parsed;
    try {
      parsed = PARSER.parse(line, 0);
    } catch (Exception e) {
      GameLogger.error("UiPromptParser: JLine parsing failed for input: " + line);
      return null;
    }

    final List<String> words = parsed.words();
    if (words.isEmpty()) {
      return null;
    }

    String firstWord = words.get(0).toLowerCase();

    if (words.size() >= 2) {
      String compoundCmdName = firstWord + "_" + words.get(1).toLowerCase();
      String[] compoundOptions = words.subList(2, words.size()).toArray(String[]::new);

      Optional<CmdAction> compoundCmd = registry.get(compoundCmdName);
      if (compoundCmd.isPresent()) {
        return compoundCmd.get().createNew(compoundOptions);
      }
    }

    String[] options = words.subList(1, words.size()).toArray(String[]::new);

    return registry
        .get(firstWord)
        .map(
            action -> {
              GameLogger.debug("UiPromptParser: Recognized registered command '" + firstWord + "'");
              return action.createNew(options);
            })
        .orElseGet(
            () -> {
              GameLogger.debug(
                  "UiPromptParser: No registered command found for '"
                      + firstWord
                      + "', falling back to regex.");
              return handleDefault(line, ui);
            });
  }

  /**
   * Fallback method to handle inputs that are not registered commands.
   *
   * <p>Uses {@code MOVE_PATTERN} and {@code RELOCATION_PATTERN} to detect if the user typed raw
   * coordinates to move a piece. If a match is found, it calculates the bitboard indices
   * using {@link CoordinateMapper}.
   *
   * @param input The raw input string from the user.
   * @param ui The {@link GameUserInterface} context for error reporting.
   * @return A {@link CmdMove} instance if coordinates are valid, {@code null} otherwise.
   */
  private static CmdAction handleDefault(String input, GameUserInterface ui) {
    String lowerInput = input.toLowerCase().trim();
    Matcher moveMatcher = MOVE_PATTERN.matcher(lowerInput);
    Matcher relocationMatcher = RELOCATION_PATTERN.matcher(lowerInput);

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

    GameLogger.debug(
        "UiPromptParser: Input '" + lowerInput + "' did not match any command or move pattern.");
    ui.showError("Invalid command. Please use help to display more information.\n");
    return null;
  }

  /**
   * Checks if a column number is within the valid Agon board range (1 to 11).
   *
   * @param col The column value to validate.
   * @return {@code true} if the coordinate is within the board boundaries.
   */
  private static boolean isValidCoord(int col) {
    return col >= 1 && col <= 11;
  }
}