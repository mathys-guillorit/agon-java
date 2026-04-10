package fr.univ.bordeaux.application.match;

/**
 * Represents the current operational state of an Agon match. *
 *
 * <p>This enum is used by the match engine to determine if the game is actively accepting moves or
 * if it has reached a terminal state.
 */
public enum MatchStatus {

  /**
   * * Indicates that the match is currently in progress. Players can still perform actions and the
   * game clock is active.
   */
  RUNNING,

  /**
   * * Indicates that the match has concluded. This state is reached when a winner is determined, a
   * draw is declared, or a player has resigned.
   */
  FINISHED,
}
