package fr.univ.bordeaux.application.network.server.invitation;

/** Represents a pending invitation between two players. */
public class Invitation {

  /** Identifier of the player who sent the invitation. */
  private final int hostId;

  /** Identifier of the invited player. */
  private final int invitedId;

  /** Expiration timestamp of the invitation in milliseconds. */
  private final long expiresAt;

  /**
   * Creates a new invitation.
   *
   * @param hostId the host player ID
   * @param invitedId the invited player ID
   * @param expiresAt the expiration timestamp in milliseconds
   */
  public Invitation(final int hostId, final int invitedId, final long expiresAt) {
    this.hostId = hostId;
    this.invitedId = invitedId;
    this.expiresAt = expiresAt;
  }

  /**
   * Returns the inviter player ID.
   *
   * @return the inviter ID
   */
  public int getInviterId() {
    return hostId;
  }

  /**
   * Returns the invited player ID.
   *
   * @return the invited ID
   */
  public int getInvitedId() {
    return invitedId;
  }

  /**
   * Returns the expiration timestamp.
   *
   * @return the expiration time in milliseconds
   */
  public long getExpiresAt() {
    return expiresAt;
  }

  /**
   * Checks whether the invitation has expired.
   *
   * @return true if the invitation is expired, false otherwise
   */
  public boolean isExpired() {
    return System.currentTimeMillis() >= expiresAt;
  }
}
