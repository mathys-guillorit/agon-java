package fr.univ.bordeaux.ui;

public interface ObservableMatch {
  void setObserver(MatchObserver observer);
  void notifyObserver();
}
