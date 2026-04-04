package fr.univ.bordeaux.application.network.protocol;

import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class used to parse compact move protocol messages. Such as:
 *
 * <ul>
 *   <li>e2e4
 *   <li>e10f11
 *   <li>e3
 * </ul>
 *
 * <p>The parser supports:
 *
 * <ul>
 *   <li>a normal move: [letter][1 or 2 digits][letter][1 or 2 digits]
 *   <li>a replacement move: [letter][1 or 2 digits]
 * </ul>
 */
public final class MoveProtocolParser {

  /**
   * Pattern matching a compact normal move. source letter + source number + destination letter +
   * destination number.
   */
  private static final Pattern FULL_MOVE_PATTERN =
      Pattern.compile("^([a-kA-K])(\\d{1,2})([a-kA-K])(\\d{1,2})$");

  /** Pattern matching a compact replacement move: destination letter + destination number. */
  private static final Pattern SHORT_MOVE_PATTERN = Pattern.compile("^([a-kA-K])(\\d{1,2})$");

  /** Utility class: no public constructor. */
  private MoveProtocolParser() {}

  /**
   * Parses a compact move string and converts it into internal board indices.
   *
   * @param rawMove the compact move string
   * @return a {@link MoveParsed} object if parsing succeeds, or null if invalid
   */
  public static MoveParsed parse(String rawMove) {
    if (rawMove == null || rawMove.isBlank()) {
      return null;
    }

    String moveText = rawMove.trim();

    Matcher fullMatcher = FULL_MOVE_PATTERN.matcher(moveText);
    if (fullMatcher.matches()) {
      char sourceLetter = Character.toUpperCase(fullMatcher.group(1).charAt(0));
      int sourceNumber = Integer.parseInt(fullMatcher.group(2));

      char destinationLetter = Character.toUpperCase(fullMatcher.group(3).charAt(0));
      int destinationNumber = Integer.parseInt(fullMatcher.group(4));

      if (!isValidRow(sourceNumber) || !isValidRow(destinationNumber)) {
        return null;
      }

      try {
        int fromIndex = CoordinateMapper.toIndex(sourceLetter, sourceNumber);
        int toIndex = CoordinateMapper.toIndex(destinationLetter, destinationNumber);

        String sourceText = "" + Character.toLowerCase(sourceLetter) + sourceNumber;
        String destinationText = "" + Character.toLowerCase(destinationLetter) + destinationNumber;

        return new MoveParsed(sourceText, destinationText, fromIndex, toIndex);
      } catch (Exception e) {
        return null;
      }
    }

    Matcher shortMatcher = SHORT_MOVE_PATTERN.matcher(moveText);
    if (shortMatcher.matches()) {
      char destinationLetter = Character.toUpperCase(shortMatcher.group(1).charAt(0));
      int destinationNumber = Integer.parseInt(shortMatcher.group(2));

      if (!isValidRow(destinationNumber)) {
        return null;
      }

      try {
        int toIndex = CoordinateMapper.toIndex(destinationLetter, destinationNumber);
        String destinationText = "" + Character.toLowerCase(destinationLetter) + destinationNumber;

        return new MoveParsed(null, destinationText, -1, toIndex);
      } catch (Exception e) {
        return null;
      }
    }

    return null;
  }

  /**
   * Checks whether a board row number is valid for Agon.
   *
   * @param value the numeric coordinate to validate
   * @return true if the value is between 1 and 11 inclusive
   */
  private static boolean isValidRow(int value) {
    return value >= 1 && value <= 11;
  }
}
