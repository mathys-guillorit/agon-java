package fr.univ.bordeaux.application.network.player;

import fr.univ.bordeaux.application.network.server.ClientHandler;

/** Represents a player registered on a server. */
public class OnlinePlayer {

  /** Unique server-local player identifier. */
  private final int playerId;

  /** Unique client identifier used for reconnection. */
  private final String clientId;

  /** Display name of the player. */
  private final String name;

  /** Current status of the player. */
  private PlayerStatus status;

  /** Client handler currently associated with this player. */
  private ClientHandler handler;

  /**
   * Creates a new online player.
   *
   * @param playerId unique server-local id
   * @param clientId unique client id
   * @param name player display name
   * @param status initial player status
   * @param handler associated client handler
   */
  public OnlinePlayer(
      final int playerId,
      final String clientId,
      final String name,
      final PlayerStatus status,
      final ClientHandler handler) {
    this.playerId = playerId;
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
    return playerId;
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
  public void setStatus(final PlayerStatus status) {
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
  public void setHandler(final ClientHandler handler) {
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
