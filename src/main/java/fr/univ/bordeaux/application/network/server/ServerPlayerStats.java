package fr.univ.bordeaux.application.network.server;

/**
 * Stores one player's statistics on the current server.
 */
public class ServerPlayerStats {

    private final String playerName;
    private int wins;
    private int losses;
    private int played;

    /**
     * Creates an empty stats entry for one player.
     *
     * @param playerName player name
     */
    public ServerPlayerStats(String playerName) {
        this.playerName = playerName;
    }

    /**
     * @return the player name
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @return number of wins
     */
    public int getWins() {
        return wins;
    }

    /**
     * @return number of losses
     */
    public int getLosses() {
        return losses;
    }

    /**
     * @return number of played games
     */
    public int getPlayed() {
        return played;
    }

    /**
     * Adds one win and one played game.
     */
    public void addWin() {
        wins++;
        played++;
    }

    /**
     * Adds one loss and one played game.
     */
    public void addLoss() {
        losses++;
        played++;
    }

    /**
     * Adds one played game without changing wins/losses.
     * Useful if you later support draws or unfinished games.
     */
    public void addPlayed() {
        played++;
    }
}