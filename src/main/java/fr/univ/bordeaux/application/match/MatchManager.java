package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.application.match.player.Player;

public interface MatchManager {

  boolean move(Move move);

  boolean undo();

  boolean redo();

  Player getCurrentPlayer();

  RestrictedAgonBoard getAgonBoard();

  Move hint();

  boolean isMatchOver();

  void quit();
}
