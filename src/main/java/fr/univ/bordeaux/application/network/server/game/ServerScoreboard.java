package fr.univ.bordeaux.application.network.server.game;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Stores the scoreboard of the current server. */
public class ServerScoreboard {

  /** Scoreboard entries indexed by normalized player name. */
  private final Map<String, ServerPlayerStats> statsByPlayerName = new ConcurrentHashMap<>();

  /** Creates a new server scoreboard. */
  public ServerScoreboard() {
    // Explicit constructor required by PMD.
  }

  /**
   * Returns the stats for a player, creating them if necessary.
   *
   * @param playerName player name
   * @return stats associated with the player
   */
  public ServerPlayerStats getOrCreateStats(final String playerName) {
    return statsByPlayerName.computeIfAbsent(
        playerName.toLowerCase(Locale.ROOT), key -> new ServerPlayerStats(playerName));
  }

  /**
   * Records a win for the given player.
   *
   * @param playerName player name
   */
  public void recordWin(final String playerName) {
    getOrCreateStats(playerName).addWin();
  }

  /**
   * Records a loss for the given player.
   *
   * @param playerName player name
   */
  public void recordLoss(final String playerName) {
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
