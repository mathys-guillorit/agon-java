package fr.univ.bordeaux.ui;

import fr.univ.bordeaux.application.match.ReadOnlyMatch;

/** Explicit. */
public interface MatchObserver {

  void onMatchUpdate(ReadOnlyMatch match);
}
