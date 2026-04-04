package fr.univ.bordeaux.application.network.player;

import fr.univ.bordeaux.application.network.server.ClientHandler;

/** Represents a player registered on a server. */
public class OnlinePlayer {

  private final int id;
  private final String clientId;
  private final String name;
  private PlayerStatus status;

  /** Client handler currently associated with this player. */
  private ClientHandler handler;

  /**
   * Creates a new online player.
   *
   * @param id unique server-local id
   * @param name player display name
   * @param status initial player status
   * @param handler associated client handler
   */
  public OnlinePlayer(
      int id, String clientId, String name, PlayerStatus status, ClientHandler handler) {
    this.id = id;
    this.clientId = clientId;
    this.name = name;
    this.status = status;
    this.handler = handler;
  }

  /**
   * Returns the server-local player id.
   *
   * @return player id
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the server-local client id.
   *
   * @return client id
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * Returns the player display name.
   *
   * @return player name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the current player status.
   *
   * @return player status
   */
  public PlayerStatus getStatus() {
    return status;
  }

  /**
   * Updates the current player status.
   *
   * @param status new player status
   */
  public void setStatus(PlayerStatus status) {
    this.status = status;
  }

  /**
   * Returns the associated client handler.
   *
   * @return associated handler
   */
  public ClientHandler getHandler() {
    return handler;
  }

  /**
   * Updates the associated client handler.
   *
   * @param handler new handler
   */
  public void setHandler(ClientHandler handler) {
    this.handler = handler;
  }

  /**
   * Returns whether the player is available for a new game.
   *
   * @return true if the player status is IDLE, false otherwise
   */
  public boolean isAvailable() {
    return status == PlayerStatus.IDLE;
  }

  /**
   * Returns whether the player is currently away.
   *
   * @return true if the player status is AWAY, false otherwise
   */
  public boolean isAway() {
    return status == PlayerStatus.AWAY;
  }
}
