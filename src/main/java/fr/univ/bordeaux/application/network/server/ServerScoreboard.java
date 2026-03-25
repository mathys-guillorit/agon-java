package fr.univ.bordeaux.application.network.server;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stores the scoreboard of the current server.
 */
public class ServerScoreboard {

    private final Map<String, ServerPlayerStats> statsByPlayerName = new LinkedHashMap<>();

    /**
     * Returns the stats for a player, creating them if necessary.
     *
     * @param playerName player name
     * @return stats associated with the player
     */
    public ServerPlayerStats getOrCreateStats(String playerName) {
        return statsByPlayerName.computeIfAbsent(playerName, ServerPlayerStats::new);
    }

    /**
     * @return all scoreboard entries
     */
    public Collection<ServerPlayerStats> getAllStats() {
        return statsByPlayerName.values();
    }
}