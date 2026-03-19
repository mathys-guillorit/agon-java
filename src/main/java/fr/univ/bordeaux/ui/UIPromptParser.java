package fr.univ.bordeaux.ui;

import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;

/**
 * Utility class to parse text into CmdAction using a command registry.
 */
public class UIPromptParser {

  private static final Parser parser = new DefaultParser();
  private static final Pattern MOVE_PATTERN =
      Pattern.compile("^([a-k])(\\d{1,2})([a-k])(\\d{1,2})$");
  private static final Pattern RELOCATION_PATTERN =
      Pattern.compile("^([a-k])(\\d{1,2})$");
  /**
   * Parse a line from the terminal and return the corresponding CmdAction.
   *
   * @param line     The input line from the terminal.
   * @param registry The registry containing available commands.
   * @return The CmdAction if found and valid, otherwise null.
   */
  public static CmdAction parse(final String line, AgonRegister<CmdAction> registry,GameUserInterface ui) {
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

    String cmdName = words.get(0).toLowerCase();
    String[] options = words.subList(1, words.size()).toArray(String[]::new);

    return registry.get(cmdName)
        .map(action -> action.createNew(options))
        .orElseGet(()->handleDefault(line,ui));
  }

  private static CmdAction handleDefault(String input,GameUserInterface ui) {
    Matcher matcher = MOVE_PATTERN.matcher(input);
    Matcher matcher2 = RELOCATION_PATTERN.matcher(input);
    if (!matcher.matches()) {
      if (matcher2.matches()) {
        char letterFrom = matcher2.group(1).charAt(0);
        int colFrom = Integer.parseInt(matcher2.group(2));
        if (colFrom < 0 || colFrom > 11) {
          System.out.println(
              "Erreur : a Relocation Move is created this way : lineFrom (a-k) + colFrom (1-11). For exemple : a1, k11 are valid while j2a1 are not.");
          return null;
        }
        return new CmdMove(-1, CoordinateMapper.toIndex(letterFrom, colFrom),ui);
      }
      return null;
    }

    char letterFrom = matcher.group(1).charAt(0);
    int colFrom = Integer.parseInt(matcher.group(2));

    char letterTo = matcher.group(3).charAt(0);
    int colTo = Integer.parseInt(matcher.group(4));
    if (colFrom < 1 || colFrom > 11 || colTo < 1 || colTo > 11) {
      System.out.println(
          "Erreur : a Move is create this way : lineFrom (a-k) + colFrom (1-11) + lineDestination (a-k) + colDestination (1-11). For exemple : a1b1, k11j10 are valid while z22aa1 are not.");
      return null;
    }

    // 5. Conversion et création de la commande
    int indexFrom = CoordinateMapper.toIndex(Character.toUpperCase(letterFrom), colFrom);
    int indexTo = CoordinateMapper.toIndex(Character.toUpperCase(letterTo), colTo);

    return new CmdMove(indexFrom, indexTo,ui);
  }
}
