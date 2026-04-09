package fr.univ.bordeaux.application.network.server.invitation;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import fr.univ.bordeaux.application.network.server.player.PlayerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/** Stores and manages pending game invitations for the server. */
public class InvitationManager implements InvitationService {

  /** Invitation timeout in seconds. */
  private static final int INVITE_TIMEOUT = 300;

  /** Service used to access active players. */
  private final PlayerService playerService;

  /** Invitations indexed by inviter id. */
  private final Map<Integer, Invitation> sentInvitations = new ConcurrentHashMap<>();

  /** Invitations indexed by invited player id. */
  private final Map<Integer, Invitation> receivedByPlayer = new ConcurrentHashMap<>();

  /** Lock protecting compound invitation state updates. */
  private final ReentrantLock stateLock = new ReentrantLock();

  /**
   * Creates a new InvitationManager.
   *
   * @param playerService the player service used to access active players
   */
  public InvitationManager(final PlayerService playerService) {
    this.playerService = playerService;
  }

  /**
   * Returns the invitation sent by a player.
   *
   * @param inviterId the inviter player identifier
   * @return the invitation, or {@code null} if none exists
   */
  @Override
  public Invitation getInvitationByInviter(final int inviterId) {
    return sentInvitations.get(inviterId);
  }

  /**
   * Returns the invitation received by a player.
   *
   * @param invitedId the invited player identifier
   * @return the invitation, or {@code null} if none exists
   */
  @Override
  public Invitation getInvitationByInvited(final int invitedId) {
    return receivedByPlayer.get(invitedId);
  }

  /**
   * Creates a new invitation.
   *
   * @param inviterId the inviter player identifier
   * @param invitedId the invited player identifier
   * @return {@code true} if the invitation was created, {@code false} otherwise
   */
  @Override
  public boolean createInvitation(final int inviterId, final int invitedId) {
    stateLock.lock();
    try {
      final OnlinePlayer inviter = playerService.getPlayerById(inviterId);
      final OnlinePlayer invited = playerService.getPlayerById(invitedId);

      if (inviter == null || invited == null) {
        return false;
      }
      if (inviterId == invitedId) {
        return false;
      }
      if (!arePlayersIdle(inviter, invited)) {
        return false;
      }

      final long expiresAt = System.currentTimeMillis() + INVITE_TIMEOUT * 1000L;
      final Invitation invitation = new Invitation(inviterId, invitedId, expiresAt);

      sentInvitations.put(inviterId, invitation);
      receivedByPlayer.put(invitedId, invitation);

      inviter.setStatus(PlayerStatus.WAITGAME);
      invited.setStatus(PlayerStatus.WAITGAME);
      return true;
    } finally {
      stateLock.unlock();
    }
  }

  /**
   * Removes an invitation involving a player.
   *
   * @param playerId the inviter or invited player identifier
   * @return the removed invitation, or {@code null} if none existed
   */
  @Override
  public Invitation removeInvitation(final int playerId) {
    stateLock.lock();
    try {
      Invitation invitation = sentInvitations.get(playerId);

      if (invitation == null) {
        invitation = receivedByPlayer.get(playerId);
      }

      if (invitation != null) {
        clearInvitation(invitation);
      }

      return invitation;
    } finally {
      stateLock.unlock();
    }
  }

  /**
   * Accepts a pending invitation and removes it from invitation storage.
   *
   * @param invitedId the invited player identifier
   * @return the accepted invitation, or {@code null} if acceptance failed
   */
  @Override
  public Invitation acceptInvitation(final int invitedId) {
    stateLock.lock();
    try {
      final Invitation invitation = receivedByPlayer.get(invitedId);

      if (invitation == null) {
        return null;
      }

      final OnlinePlayer inviter = playerService.getPlayerById(invitation.getInviterId());
      final OnlinePlayer invited = playerService.getPlayerById(invitation.getInvitedId());

      if (invitation.isExpired() || inviter == null || invited == null) {
        clearInvitation(invitation);
        return null;
      }

      sentInvitations.remove(invitation.getInviterId());
      receivedByPlayer.remove(invitation.getInvitedId());
      return invitation;
    } finally {
      stateLock.unlock();
    }
  }

  /** Removes all expired invitations. */
  @Override
  public void clearExpiredInvitations() {
    stateLock.lock();
    try {
      final List<Invitation> expiredList = new ArrayList<>();

      for (final Invitation invitation : sentInvitations.values()) {
        if (invitation.isExpired()) {
          expiredList.add(invitation);
        }
      }

      for (final Invitation invitation : expiredList) {
        sentInvitations.remove(invitation.getInviterId());
        receivedByPlayer.remove(invitation.getInvitedId());

        final OnlinePlayer inviter = playerService.getPlayerById(invitation.getInviterId());
        final OnlinePlayer invited = playerService.getPlayerById(invitation.getInvitedId());

        resetWaitingPlayer(inviter);
        resetWaitingPlayer(invited);
      }
    } finally {
      stateLock.unlock();
    }
  }

  /**
   * Removes an invitation and resets both involved players to idle.
   *
   * @param invitation the invitation to clear
   */
  private void clearInvitation(final Invitation invitation) {
    if (invitation == null) {
      return;
    }

    sentInvitations.remove(invitation.getInviterId());
    receivedByPlayer.remove(invitation.getInvitedId());

    resetPlayerToIdle(playerService.getPlayerById(invitation.getInviterId()));
    resetPlayerToIdle(playerService.getPlayerById(invitation.getInvitedId()));
  }

  /**
   * Checks whether both players are idle.
   *
   * @param inviter the inviter player
   * @param invited the invited player
   * @return {@code true} if both players are idle, {@code false} otherwise
   */
  private boolean arePlayersIdle(final OnlinePlayer inviter, final OnlinePlayer invited) {
    return isIdle(inviter) && isIdle(invited);
  }

  /**
   * Returns whether a player is idle.
   *
   * @param player the player to test
   * @return {@code true} if the player is idle
   */
  private boolean isIdle(final OnlinePlayer player) {
    return player != null && player.getStatus() == PlayerStatus.IDLE;
  }

  /**
   * Resets a waiting player to idle if needed.
   *
   * @param player the player to reset
   */
  private void resetWaitingPlayer(final OnlinePlayer player) {
    if (isWaitingGame(player)) {
      player.setStatus(PlayerStatus.IDLE);
    }
  }

  /**
   * Returns whether a player is waiting for a game.
   *
   * @param player the player to test
   * @return {@code true} if the player is waiting for a game
   */
  private boolean isWaitingGame(final OnlinePlayer player) {
    return player != null && player.getStatus() == PlayerStatus.WAITGAME;
  }

  /**
   * Resets a player status to idle when the player exists.
   *
   * @param player the player to reset
   */
  private void resetPlayerToIdle(final OnlinePlayer player) {
    if (player != null) {
      player.setStatus(PlayerStatus.IDLE);
    }
  }
}
