package fr.univ.bordeaux.application.network.client;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a local user profile, storing their name and unique identifiers
 * assigned by different game servers.
 */
public class LocalProfile {

    private String name;

    /** * Map linking server keys (e.g., "IP:PORT") to the specific ID
     * assigned to this user by that server.
     */
    private final Map<String, Integer> serverIds = new HashMap<>();

    /**
     * Creates a new local profile with a specified name.
     * * @param name the user's display name
     */
    public LocalProfile(String name) {
        this.name = name;
    }

    /**
     * Returns the user's name.
     * * @return the profile name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the user's name.
     * * @param name the new name for the profile
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retrieves the specific player ID for a given server.
     * * @param serverKey the unique key of the server (e.g., "127.0.0.1:12345")
     * @return the assigned ID, or null if no ID exists for this server
     */
    public Integer getIdForServer(String serverKey) {
        return serverIds.get(serverKey);
    }

    /**
     * Associates a player ID with a specific server.
     * * @param serverKey the unique key of the server
     * @param id the ID assigned by the server
     */
    public void setIdForServer(String serverKey, int id) {
        serverIds.put(serverKey, id);
    }

    /**
     * Returns the complete mapping of server keys to player IDs.
     * * @return a map of server-specific identifiers
     */
    public Map<String, Integer> getServerIds() {
        return serverIds;
    }
}