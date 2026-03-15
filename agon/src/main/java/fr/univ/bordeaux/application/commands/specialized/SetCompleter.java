package fr.univ.bordeaux.application.commands.specialized;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

/**
 * completer for set command (specific args with assignments)
 */
public class SetCompleter implements Completer {

  private Set<String> optNames;

  /**
   * complete set command with options without "--" at the beginning and set with a "="
   *
   * @param opts options to get "longOpt()" as option name
   * @throws IllegalArgumentException all options must have filled "longOpt()" else it will raise an
   *                                  error
   */
  public SetCompleter(Options opts) throws IllegalArgumentException {
    this.optNames = new HashSet<>();
    String optName;
    for (Option opt : opts.getOptions()) {
      optName = opt.getLongOpt();
      if (optName == null) {
        throw new IllegalArgumentException("Missing required option");
      }
      this.optNames.add(optName);
    }
  }

  @Override
  public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
    String word = line.word();
    int wordIndex = line.wordIndex();
    // after "set" (cmdName)
    if (wordIndex != 1) {
      return;
    }
    String current = line.word();
    Candidate candidate;
    for (String key : this.optNames) {
      if (key.startsWith(current)) {
        // candidate contains no " " after completion
        candidate = new Candidate(key + "=", key + "=", null, null, null, null, false);
        candidates.add(candidate);
      }
    }
  }
}
