package fr.univ.bordeaux.application.match;

/**
 * Represents the current lifecycle state of a match.
 *
 * <p>This enumeration is used to track whether a game is actively being played 
 * or if it has reached a terminal state (victory, draw, or termination).</p>
 */
public enum MatchStatus {
  /** The match is currently in progress and accepting moves. */
  RUNNING,

  /** The match has concluded and no further actions can be performed. */
  FINISHED,
}