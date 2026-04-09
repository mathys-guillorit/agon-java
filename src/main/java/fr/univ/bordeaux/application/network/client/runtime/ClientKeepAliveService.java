package fr.univ.bordeaux.application.network.client.runtime;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Sends periodic keep-alive requests to the remote server. */
public class ClientKeepAliveService {

  /** Indicates whether the keep-alive loop is currently running. */
  private final AtomicBoolean running = new AtomicBoolean(false);

  /** Scheduler used to execute keep-alive tasks periodically. */
  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(
          runnable -> {
            final Thread thread = new Thread(runnable, "AgonClient-KeepAlive");
            thread.setDaemon(true);
            return thread;
          });

  public ClientKeepAliveService() {}

  /**
   * Starts the keep-alive loop.
   *
   * @param keepAliveTask task executed periodically while the service is running
   */
  public void start(final Runnable keepAliveTask) {
    if (running.compareAndSet(false, true)) {
      scheduler.scheduleAtFixedRate(
          () -> {
            if (running.get()) {
              keepAliveTask.run();
            }
          },
          30,
          30,
          TimeUnit.SECONDS);
    }
  }

  /** Stops the keep-alive loop. */
  public void stop() {
    running.set(false);
    scheduler.shutdownNow();
  }

  /**
   * Indicates whether the keep-alive loop is active.
   *
   * @return {@code true} if the keep-alive loop is active, {@code false} otherwise
   */
  public boolean isRunning() {
    return running.get();
  }
}
