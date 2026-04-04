package fr.univ.bordeaux.application.network.server;

/** Stores the persistent statistics of one player on the server. */
public class ServerPlayerStats {

  private final String playerName;
  private int wins;
  private int losses;
  private int games;

  /**
   * Creates a new scoreboard entry for a player.
   *
   * @param playerName player name
   */
  public ServerPlayerStats(String playerName) {
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
