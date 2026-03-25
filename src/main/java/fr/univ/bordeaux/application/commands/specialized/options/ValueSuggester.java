package fr.univ.bordeaux.application.commands.specialized.options;

import java.util.List;
import org.jline.reader.Candidate;

/** used instead of Runnable (more specific behavior) */
@FunctionalInterface
public interface ValueSuggester {

  void suggest(String token, List<Candidate> candidates);
}
