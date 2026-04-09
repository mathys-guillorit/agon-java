package fr.univ.bordeaux.application.network.server.invitation;

/** Defines invitation-related operations exposed by the server invitation subsystem. */
public interface InvitationService {

  /**
   * Returns the invitation sent by a player.
   *
   * @param inviterId the inviter player identifier
   * @return the invitation, or {@code null} if none exists
   */
  Invitation getInvitationByInviter(int inviterId);

  /**
   * Returns the invitation received by a player.
   *
   * @param invitedId the invited player identifier
   * @return the invitation, or {@code null} if none exists
   */
  Invitation getInvitationByInvited(int invitedId);

  /**
   * Creates a new invitation.
   *
   * @param inviterId the inviter player identifier
   * @param invitedId the invited player identifier
   * @return {@code true} if the invitation was created, {@code false} otherwise
   */
  boolean createInvitation(int inviterId, int invitedId);

  /**
   * Removes an invitation involving a player.
   *
   * @param playerId the inviter or invited player identifier
   * @return the removed invitation, or {@code null} if none existed
   */
  Invitation removeInvitation(int playerId);

  /**
   * Accepts a pending invitation and removes it from invitation storage.
   *
   * @param invitedId the invited player identifier
   * @return the accepted invitation, or {@code null} if acceptance failed
   */
  Invitation acceptInvitation(int invitedId);

  /** Removes all expired invitations. */
  void clearExpiredInvitations();
}
