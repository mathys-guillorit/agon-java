package fr.univ.bordeaux.application.network.server.game;

/** Stores the persistent statistics of one player on the server. */
public class ServerPlayerStats {

  /** Display name of the player associated with this scoreboard entry. */
  private final String playerName;

  /** Total number of wins recorded for this player. */
  private int wins;

  /** Total number of losses recorded for this player. */
  private int losses;

  /** Total number of games played by this player. */
  private int games;

  /**
   * Creates a new scoreboard entry for a player.
   *
   * @param playerName player name
   */
  public ServerPlayerStats(final String playerName) {
    this.playerName = playerName;
  }

  /**
   * Returns the player name associated with this scoreboard entry.
   *
   * @return player name
   */
  public String getPlayerName() {
    return playerName;
  }

  /**
   * Returns the total number of wins.
   *
   * @return total wins
   */
  public int getWins() {
    return wins;
  }

  /**
   * Returns the total number of losses.
   *
   * @return total losses
   */
  public int getLosses() {
    return losses;
  }

  /**
   * Returns the total number of played games.
   *
   * @return total games
   */
  public int getGames() {
    return games;
  }

  /** Records a win and increments the number of played games. */
  public void addWin() {
    wins++;
    games++;
  }

  /** Records a loss and increments the number of played games. */
  public void addLoss() {
    losses++;
    games++;
  }
}
