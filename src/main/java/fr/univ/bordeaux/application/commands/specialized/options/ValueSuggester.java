package fr.univ.bordeaux.application.commands.specialized.options;

import java.util.List;
import org.jline.reader.Candidate;

/** Used instead of Runnable (more specific behavior). */
@FunctionalInterface
public interface ValueSuggester {

  /**
   * Predict next characters over the actual pattern.
   *
   * @param token user's actual text pattern.
   *
   * @param candidates possibles word(s) for completion
   */
  void suggest(String token, List<Candidate> candidates);
}
