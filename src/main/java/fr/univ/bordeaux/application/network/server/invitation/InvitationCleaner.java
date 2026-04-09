package fr.univ.bordeaux.application.network.server.invitation;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Periodically triggers invitation cleanup in the background. */
public class InvitationCleaner {

  /** Background scheduler used for periodic invitation cleanup. */
  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(
          runnable -> {
            final Thread thread = new Thread(runnable, "invitation-cleaner");
            thread.setDaemon(true);
            return thread;
          });

  /** Service used to remove expired invitations. */
  private final InvitationService invitationService;

  /**
   * Creates a new InvitationCleaner.
   *
   * @param invitationService the invitation service to clean periodically
   */
  public InvitationCleaner(final InvitationService invitationService) {
    this.invitationService = invitationService;
  }

  /** Starts the periodic cleanup task. */
  public void start() {
    scheduler.scheduleAtFixedRate(
        invitationService::clearExpiredInvitations, 1, 1, TimeUnit.SECONDS);
  }

  /** Stops the periodic cleanup task. */
  public void stop() {
    scheduler.shutdownNow();
  }
}
