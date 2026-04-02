package fr.univ.bordeaux.ui;

/** Explicit. */
public interface ObservableMatch {
  void setObserver(MatchObserver observer);

  void notifyUi();
}
