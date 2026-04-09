package fr.univ.bordeaux.application.network.client.runtime;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Delayed;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ClientKeepAliveServiceTest {

  private static class DummyScheduledFuture implements ScheduledFuture<Object> {
    @Override
    public long getDelay(TimeUnit unit) {
      return 0;
    }

    @Override
    public int compareTo(Delayed o) {
      return 0;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isDone() {
      return false;
    }

    @Override
    public Object get() {
      return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) {
      return null;
    }
  }

  private ClientKeepAliveService service;

  @AfterEach
  void tearDown() {
    if (service != null) {
      service.stop();
    }
  }

  @Test
  void startAndStopChangeRunningState() {
    service = new ClientKeepAliveService();
    AtomicInteger calls = new AtomicInteger();

    assertFalse(service.isRunning());

    service.start(calls::incrementAndGet);
    assertTrue(service.isRunning());

    service.start(calls::incrementAndGet);
    assertTrue(service.isRunning());

    service.stop();
    assertFalse(service.isRunning());
  }

  private static class RecordingScheduler extends AbstractExecutorService
      implements ScheduledExecutorService {

    Runnable scheduledCommand;
    long initialDelay;
    long period;
    TimeUnit unit;
    boolean shutdownNowCalled;

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(
        Runnable command, long initialDelay, long period, TimeUnit unit) {
      this.scheduledCommand = command;
      this.initialDelay = initialDelay;
      this.period = period;
      this.unit = unit;
      return new DummyScheduledFuture();
    }

    @Override
    public List<Runnable> shutdownNow() {
      shutdownNowCalled = true;
      return Collections.emptyList();
    }

    @Override
    public void shutdown() {
      shutdownNowCalled = true;
    }

    @Override
    public boolean isShutdown() {
      return shutdownNowCalled;
    }

    @Override
    public boolean isTerminated() {
      return shutdownNowCalled;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
      return true;
    }

    @Override
    public void execute(Runnable command) {
      command.run();
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <V> ScheduledFuture<V> schedule(
        java.util.concurrent.Callable<V> callable, long delay, TimeUnit unit) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(
        Runnable command, long initialDelay, long delay, TimeUnit unit) {
      throw new UnsupportedOperationException();
    }
  }

  private static void injectScheduler(
      ClientKeepAliveService service, ScheduledExecutorService scheduler) throws Exception {
    Field field = ClientKeepAliveService.class.getDeclaredField("scheduler");
    field.setAccessible(true);
    field.set(service, scheduler);
  }

  @Test
  void startSchedulesTaskAndRunsItOnlyWhileRunning() throws Exception {
    ClientKeepAliveService service = new ClientKeepAliveService();
    RecordingScheduler scheduler = new RecordingScheduler();
    AtomicInteger calls = new AtomicInteger();

    injectScheduler(service, scheduler);

    assertFalse(service.isRunning());

    service.start(calls::incrementAndGet);

    assertTrue(service.isRunning());
    assertNotNull(scheduler.scheduledCommand);
    assertEquals(30, scheduler.initialDelay);
    assertEquals(30, scheduler.period);
    assertEquals(TimeUnit.SECONDS, scheduler.unit);

    scheduler.scheduledCommand.run();
    assertEquals(1, calls.get());

    service.stop();
    assertFalse(service.isRunning());
    assertTrue(scheduler.shutdownNowCalled);

    scheduler.scheduledCommand.run();
    assertEquals(1, calls.get());
  }

  @Test
  void secondStartDoesNotScheduleAgain() throws Exception {
    ClientKeepAliveService service = new ClientKeepAliveService();
    RecordingScheduler scheduler = new RecordingScheduler();
    AtomicInteger calls = new AtomicInteger();

    injectScheduler(service, scheduler);

    service.start(calls::incrementAndGet);
    Runnable firstCommand = scheduler.scheduledCommand;

    service.start(calls::incrementAndGet);

    assertSame(firstCommand, scheduler.scheduledCommand);
    assertTrue(service.isRunning());
  }
}
