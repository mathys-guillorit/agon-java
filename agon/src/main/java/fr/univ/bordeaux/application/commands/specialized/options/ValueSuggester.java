package fr.univ.bordeaux.application.commands.specialized.options;

import org.jline.reader.Candidate;

import java.util.List;

/**
 * used instead of Runnable (more specific behavior)
 */
@FunctionalInterface
public interface ValueSuggester {
    void suggest(String token, List<Candidate> candidates);
}
