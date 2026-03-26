package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.application.match.player.Player;
import java.util.List;

public interface MatchManager {

  boolean move(Move move);

  boolean undo();

  boolean redo();

  boolean pause();

  Player getCurrentPlayer();

  RestrictedAgonBoard getAgonBoard();

  Move hint();

  String getRemainingTime();

  boolean isMatchOver();

  List<MoveDTO> getHistory();

  void quit();
}
