package fr.univ.bordeaux.ui.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jdk.jfr.Description;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OptCompleterAdapterTest {

  private Options opts;

  @Test
  @Description("no Option in object Options")
  void nullableOption() {
    var a = new OptCompleterAdapter(null);
    assertThrows(NullPointerException.class, () -> a.getCompleter(""));
  }

  @BeforeEach
  @Description("load redundant options")
  void loadOptions() {
    this.opts = new Options();
    this.opts.addOption("v", "verbose", false, "");
    this.opts.addOption("h", "help", true, "");
    this.opts.addOption("d", "debug", false, "");
    this.opts.addOption("H", "hint", false, "");
    this.opts.addOption("V", "version", false, "");
    this.opts.addOption("k", "helm", false, "");
  }

  @Test
  @Description("should predict correct filled word from nothing")
  void predictFromNothing3() {
    var a = new OptCompleterAdapter(this.opts);
    Completer completer = a.getCompleter("cmd");
    String msg = "the Completer must be a ArgumentCompleter";
    assertEquals(ArgumentCompleter.class, completer.getClass(), msg);
    final DefaultParser parser = new DefaultParser();
    final String input = "cmd ";
    final ParsedLine line = parser.parse(input, input.length());
    final var reader = new FakeLineReader(input);
    final List<Candidate> candidates = new ArrayList<>();
    completer.complete(reader, line, candidates);
    final short optCount = 12;
    final List<String> uniqueOptions =
        candidates.stream().map(Candidate::value).distinct().toList();
    assertEquals(
        optCount,
        uniqueOptions.size(),
        "we must have " + optCount + " options defined in `loadOptions()`");
  }

  @Test
  @Description("should predict correct filled word from a letter")
  void predictFromLetter() {
    var a = new OptCompleterAdapter(this.opts);
    Completer completer = a.getCompleter("cmd");
    DefaultParser parser = new DefaultParser();
    String input = "cmd -";
    ParsedLine line = parser.parse(input, input.length());
    var reader = new FakeLineReader(input);
    List<Candidate> candidates = new ArrayList<>();
    completer.complete(reader, line, candidates);
    short optCount = 12;
    List<String> uniqueOptions = candidates.stream().map(Candidate::value).distinct().toList();
    assertEquals(
        optCount,
        uniqueOptions.size(),
        "we must have " + optCount + " options defined in `loadOptions()`");
  }

  @Test
  @Description("test case sensitive for opt name")
  void testCaseSensitive() {
    var a = new OptCompleterAdapter(this.opts);
    Completer completer = a.getCompleter("cmd");
    String msg = "the Completer must be a ArgumentCompleter";
    assertEquals(ArgumentCompleter.class, completer.getClass(), msg);
    DefaultParser parser = new DefaultParser();
    String input = "cmd -H";
    ParsedLine line = parser.parse(input, input.length());
    var reader = new FakeLineReader(input);
    List<Candidate> candidates = new ArrayList<>();
    completer.complete(reader, line, candidates);
    final short optCount = 1;
    final String currentWord = line.word();
    List<String> uniqueOptions =
        candidates.stream()
            .map(Candidate::value)
            .filter(val -> val.startsWith(currentWord))
            .distinct()
            .toList();
    msg = "options are: " + uniqueOptions;
    assertEquals(optCount, uniqueOptions.size(), msg);
    msg = "output is: \"" + uniqueOptions + "\" and must be: \"" + uniqueOptions + "\"";
    final String shortName = uniqueOptions.getFirst().replace("-", "");
    assertEquals("hint", this.opts.getOption(shortName).getLongOpt(), msg);
  }

  @Test
  @Description("should predict correct filled word from many letters")
  void predictFromLetters() {
    var a = new OptCompleterAdapter(this.opts);
    Completer completer = a.getCompleter("cmd");
    String msg = "the Completer must be a ArgumentCompleter";
    assertEquals(ArgumentCompleter.class, completer.getClass(), msg);
    DefaultParser parser = new DefaultParser();
    String input = "cmd --h";
    ParsedLine line = parser.parse(input, input.length());
    var reader = new FakeLineReader(input);
    List<Candidate> candidates = new ArrayList<>();
    completer.complete(reader, line, candidates);
    short optCount = 3;
    String currentWord = line.word();
    List<String> uniqueOptions =
        candidates.stream()
            .map(Candidate::value)
            .filter(val -> val.startsWith(currentWord))
            .distinct()
            .toList();
    boolean allLongOptions = uniqueOptions.stream().allMatch(s -> s.startsWith("-"));
    assertTrue(
        allLongOptions,
        "all candidates should start with '--' but it's not : '" + uniqueOptions + "'");
    assertEquals(
        optCount, uniqueOptions.size(), "options are: " + uniqueOptions + "all options are: ");
  }

  @Test
  @Description("predict only long options")
  void predictLongOptions() {
    var a = new OptCompleterAdapter(this.opts);
    Completer completer = a.getCompleter("cmd");
    String msg = "the Completer must be a ArgumentCompleter";
    assertEquals(ArgumentCompleter.class, completer.getClass(), msg);
    DefaultParser parser = new DefaultParser();
    String input = "cmd --";
    ParsedLine line = parser.parse(input, input.length());
    var reader = new FakeLineReader(input);
    List<Candidate> candidates = new ArrayList<>();
    completer.complete(reader, line, candidates);
    short optCount = 6;
    String currentWord = line.word();
    List<String> uniqueOptions =
        candidates.stream()
            .map(Candidate::value)
            .filter(val -> val.startsWith(currentWord))
            .distinct()
            .toList();
    boolean allLongOptions = uniqueOptions.stream().allMatch(s -> s.startsWith("--"));
    assertTrue(
        allLongOptions,
        "all candidates should start with '--' but it's not : '" + uniqueOptions + "'");
    assertEquals(
        optCount,
        uniqueOptions.size(),
        "we must have " + optCount + " options defined in `loadOptions()`");
  }

  // 3 last % of coverage (The "if") ("if" branch on method getCompleter(...))
  @Test
  @DisplayName("Should skip null or empty options during adapter conversion")
  void shouldIgnoreInvalidOptions() {
    Options corruptOpts =
        new Options() {
          @Override
          public Collection<Option> getOptions() {
            List<Option> optsList = new ArrayList<>(super.getOptions());
            optsList.add(null);
            return optsList;
          }
        };
    corruptOpts.addOption(new Option(null, null, false, "both null"));
    corruptOpts.addOption(new Option(null, "long-only", false, "long only"));
    corruptOpts.addOption(new Option("s", null, false, "short only"));
    corruptOpts.addOption(new Option("v", "both-valid", false, "both valid"));
    var adapter = new OptCompleterAdapter(corruptOpts);
    Completer completer = adapter.getCompleter("cmd");
    final List<Candidate> candidates = new ArrayList<>();
    completer.complete(new FakeLineReader(""), new DefaultParser().parse("cmd ", 4), candidates);
    List<String> values = candidates.stream().map(Candidate::value).toList();
    assertTrue(values.contains("-s"), "Should contain short-only: " + values);
    assertTrue(values.contains("--long-only"), "Should contain long-only");
    assertTrue(values.contains("-v"), "Should contain both-valid (short)");
    assertTrue(values.contains("--both-valid"), "Should contain both-valid (long)");
    assertFalse(values.contains("-null"), "Should never contain string '-null'");
    assertFalse(values.contains("--null"), "Should never contain string '--null'");
    assertEquals(4, values.size(), "Should have exactly 4 valid candidates");
  }
}
