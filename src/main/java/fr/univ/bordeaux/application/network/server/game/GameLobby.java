package fr.univ.bordeaux.application.network.server.game;

/** Represents a lobby between two players before the game starts. */
public class GameLobby {

  /** Identifier of the host player who created the lobby. */
  private final int hostId;

  /** Identifier of the guest player invited into the lobby. */
  private final int guestId;

  /**
   * Creates a new lobby.
   *
   * @param hostId the host player ID
   * @param guestId the guest player ID
   */
  public GameLobby(final int hostId, final int guestId) {
    this.hostId = hostId;
    this.guestId = guestId;
  }

  /**
   * Returns the host player ID.
   *
   * @return the host ID
   */
  public int getHostId() {
    return hostId;
  }

  /**
   * Returns the guest player ID.
   *
   * @return the guest ID
   */
  public int getGuestId() {
    return guestId;
  }
}
