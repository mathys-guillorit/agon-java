package fr.univ.bordeaux.application.network.server;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Stores the scoreboard of the current server. */
public class ServerScoreboard {

  private final Map<String, ServerPlayerStats> statsByPlayerName = new LinkedHashMap<>();

  /**
   * Returns the stats for a player, creating them if necessary.
   *
   * @param playerName player name
   * @return stats associated with the player
   */
  public ServerPlayerStats getOrCreateStats(String playerName) {
    return statsByPlayerName.computeIfAbsent(
        playerName.toLowerCase(), key -> new ServerPlayerStats(playerName));
  }

  /**
   * Records a win for the given player.
   *
   * @param playerName player name
   */
  public void recordWin(String playerName) {
    getOrCreateStats(playerName).addWin();
  }

  /**
   * Records a loss for the given player.
   *
   * @param playerName player name
   */
  public void recordLoss(String playerName) {
    getOrCreateStats(playerName).addLoss();
  }

  /**
   * Returns all scoreboard entries.
   *
   * @return all scoreboard entries
   */
  public Collection<ServerPlayerStats> getAllStats() {
    return statsByPlayerName.values();
  }

  /**
   * Indicates whether the scoreboard is empty.
   *
   * @return true if no player stats are stored
   */
  public boolean isEmpty() {
    return statsByPlayerName.isEmpty();
  }
}
