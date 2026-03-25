package fr.univ.bordeaux.application.network;

/**
 * Represents a player independently from the game mode.
 * A player may be used in local mode, AI mode, or network mode.
 */
public class OnlinePlayer {

    private final int id;
    private final String name;
    private String status;

    /**
     * Creates a player with a default idle status.
     *
     * @param id unique player identifier
     * @param name player name
     */
    public OnlinePlayer(int id, String name) {
        this.id = id;
        this.name = name;
        this.status = "idle";
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}