package fr.univ.bordeaux.application.network.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a local user profile.
 *
 * <p>This profile stores:
 *
 * <ul>
 *   <li>the display name chosen by the user,
 *   <li>a unique client identifier used for server-side reconnection,
 *   <li>the player IDs assigned by each connected server.
 * </ul>
 *
 * <p>The display name is not used as a unique identity because multiple users may share the same
 * name.
 */
public class LocalProfile {

  /** User-visible display name. */
  private String name;

  private final String clientId;
  private final Map<String, Integer> serverIds = new HashMap<>();

  /**
   * Creates a new local profile with a specified display name.
   *
   * <p>A unique client ID is automatically generated.
   *
   * @param name the user's display name
   */
  public LocalProfile(String name) {
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
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the unique client identifier of this profile.
   *
   * <p>This identifier is sent to the server during LOGIN and is used to distinguish clients even
   * if they have the same name.
   *
   * @return the unique client ID
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * Retrieves the specific player ID assigned by a given server.
   *
   * @param serverKey the unique server key (e.g. "127.0.0.1:12345")
   * @return the assigned player ID, or null if no ID exists for this server
   */
  public Integer getIdForServer(String serverKey) {
    return serverIds.get(serverKey);
  }

  /**
   * Associates a player ID with a specific server.
   *
   * @param serverKey the unique server key
   * @param id the player ID assigned by the server
   */
  public void setIdForServer(String serverKey, int id) {
    serverIds.put(serverKey, id);
  }

  /**
   * Returns the complete mapping of server keys to player IDs.
   *
   * @return a map of server-specific identifiers
   */
  public Map<String, Integer> getServerIds() {
    return serverIds;
  }
}
