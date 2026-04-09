package fr.univ.bordeaux.application.network.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a local user profile. Stores the display name, a unique client identifier, and the
 * player IDs assigned by connected servers.
 */
public class LocalProfile {

  /** User-visible display name. */
  private String name;

  /** Unique client identifier used for reconnection and identification. */
  private final String clientId;

  /** Mapping between a server key and its assigned player ID. */
  private final Map<String, Integer> serverIds = new ConcurrentHashMap<>();

  /**
   * Creates a new local profile with a specified display name.
   *
   * @param name the user's display name
   */
  public LocalProfile(final String name) {
    this.name = name;
    this.clientId = UUID.randomUUID().toString();
  }

  /**
   * Returns the user's display name.
   *
   * @return the profile name
   */
  public String getName() {
    return name;
  }

  /**
   * Updates the user's display name.
   *
   * @param name the new name for the profile
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Returns the unique client identifier of this profile.
   *
   * @return the unique client ID
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * Retrieves the player ID assigned by a given server.
   *
   * @param serverKey the unique server key
   * @return the assigned player ID, or null if none exists
   */
  public Integer getIdForServer(final String serverKey) {
    return serverIds.get(serverKey);
  }

  /**
   * Associates a player ID with a specific server.
   *
   * @param serverKey the unique server key
   * @param playerId the player ID assigned by the server
   */
  public void setIdForServer(final String serverKey, final int playerId) {
    serverIds.put(serverKey, playerId);
  }

  /**
   * Returns the mapping of server keys to player IDs.
   *
   * @return a map of server-specific identifiers
   */
  public Map<String, Integer> getServerIds() {
    return serverIds;
  }
}
