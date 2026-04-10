package fr.univ.bordeaux.application.network.server.lifecycle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import fr.univ.bordeaux.application.network.server.ServerDiscovery;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ServerLifecycleManagerTest {

  private Field findField(Class<?> type, String fieldName) throws Exception {
    Class<?> current = type;
    while (current != null) {
      try {
        Field f = current.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f;
      } catch (NoSuchFieldException ignored) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException(fieldName);
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field f = findField(target.getClass(), fieldName);
    f.set(target, value);
  }

  private Object getField(Object target, String fieldName) throws Exception {
    Field f = findField(target.getClass(), fieldName);
    return f.get(target);
  }

  private Object invokePrivate(Object target, String methodName, Class<?>[] types, Object... args)
      throws Exception {
    Method m = target.getClass().getDeclaredMethod(methodName, types);
    m.setAccessible(true);
    return m.invoke(target, args);
  }

  static class FakeDiscovery extends ServerDiscovery {
    boolean startCalled = false;
    boolean stopCalled = false;

    FakeDiscovery() {
      super("test", 12345);
    }

    @Override
    public void start() {
      startCalled = true;
    }

    @Override
    public void stop() {
      stopCalled = true;
    }
  }

  static class AcceptThenFailServerSocket extends ServerSocket {
    private boolean firstAccept = true;
    boolean closeCalled = false;

    AcceptThenFailServerSocket() throws IOException {
      super();
    }

    @Override
    public Socket accept() throws IOException {
      if (firstAccept) {
        firstAccept = false;
        return new Socket();
      }
      throw new IOException("forced accept failure");
    }

    @Override
    public void close() throws IOException {
      closeCalled = true;
      super.close();
    }
  }

  static class FailingCloseServerSocket extends ServerSocket {
    boolean closeCalled = false;

    FailingCloseServerSocket() throws IOException {
      super();
    }

    @Override
    public void close() throws IOException {
      closeCalled = true;
      throw new IOException("forced close failure");
    }
  }

  @Test
  @DisplayName("constructeur + état initial")
  void constructorAndInitialStateTest() {
    ServerLifecycleManager manager = new ServerLifecycleManager(12345, "test");
    assertNotNull(manager);
    assertFalse(manager.isRunning());
  }

  @Test
  @DisplayName("start puis stop : cycle nominal")
  void startStopTest() {
    ServerLifecycleManager manager = new ServerLifecycleManager(0, "test");

    assertTrue(manager.start(socket -> {}));
    assertTrue(manager.isRunning());

    assertTrue(manager.stop());
    assertFalse(manager.isRunning());
  }

  @Test
  @DisplayName("start : déjà lancé retourne true")
  void startAlreadyRunningTest() {
    ServerLifecycleManager manager = new ServerLifecycleManager(0, "test");

    assertTrue(manager.start(socket -> {}));
    assertTrue(manager.start(socket -> fail("Le consumer ne doit pas être remplacé ici")));
    assertTrue(manager.isRunning());

    assertTrue(manager.stop());
    assertFalse(manager.isRunning());
  }

  @Test
  @DisplayName("start : échec si port déjà occupé")
  void startFailureTest() throws Exception {
    try (ServerSocket occupied = new ServerSocket(0)) {
      int busyPort = occupied.getLocalPort();
      ServerLifecycleManager manager = new ServerLifecycleManager(busyPort, "test");

      assertFalse(manager.start(socket -> {}));
      assertFalse(manager.isRunning());
    }
  }

  @Test
  @DisplayName("submitClientHandler : le runnable est bien exécuté")
  void submitClientHandlerTest() throws Exception {
    ServerLifecycleManager manager = new ServerLifecycleManager(12345, "test");
    CountDownLatch latch = new CountDownLatch(1);

    manager.submitClientHandler(latch::countDown);

    assertTrue(latch.await(2, TimeUnit.SECONDS));
    manager.stop();
  }

  @Test
  @DisplayName("stop : fonctionne même sans discovery ni server socket")
  void stopWithoutResourcesTest() {
    ServerLifecycleManager manager = new ServerLifecycleManager(12345, "test");

    assertTrue(manager.stop());
    assertFalse(manager.isRunning());
  }

  @Test
  @DisplayName("stopDiscovery : appelle stop si discovery non nulle")
  void stopDiscoveryWithDiscoveryTest() throws Exception {
    ServerLifecycleManager manager = new ServerLifecycleManager(12345, "test");
    FakeDiscovery discovery = new FakeDiscovery();

    setField(manager, "discovery", discovery);

    invokePrivate(manager, "stopDiscovery", new Class<?>[] {});

    assertTrue(discovery.stopCalled);
  }

  @Test
  @DisplayName("closeServerSocket : ferme la socket si non nulle")
  void closeServerSocketSuccessTest() throws Exception {
    ServerLifecycleManager manager = new ServerLifecycleManager(12345, "test");
    AcceptThenFailServerSocket fakeSocket = new AcceptThenFailServerSocket();

    setField(manager, "serverSocket", fakeSocket);

    invokePrivate(manager, "closeServerSocket", new Class<?>[] {});

    assertTrue(fakeSocket.closeCalled);
  }

  @Test
  @DisplayName("closeServerSocket : ignore IOException lors de la fermeture")
  void closeServerSocketFailureTest() throws Exception {
    ServerLifecycleManager manager = new ServerLifecycleManager(12345, "test");
    FailingCloseServerSocket fakeSocket = new FailingCloseServerSocket();

    setField(manager, "serverSocket", fakeSocket);

    assertDoesNotThrow(() -> invokePrivate(manager, "closeServerSocket", new Class<?>[] {}));
    assertTrue(fakeSocket.closeCalled);
  }

  @Test
  @DisplayName("acceptClientLoop : accepte une socket puis sort sur IOException")
  void acceptClientLoopSuccessThenFailureTest() throws Exception {
    ServerLifecycleManager manager = new ServerLifecycleManager(12345, "test");
    AcceptThenFailServerSocket fakeSocket = new AcceptThenFailServerSocket();
    AtomicInteger acceptedCount = new AtomicInteger(0);

    setField(manager, "serverSocket", fakeSocket);

    AtomicBoolean running = (AtomicBoolean) getField(manager, "running");
    running.set(true);

    Consumer<Socket> consumer =
        socket -> {
          acceptedCount.incrementAndGet();
          running.set(false);
        };

    assertDoesNotThrow(
        () ->
            invokePrivate(manager, "acceptClientLoop", new Class<?>[] {Consumer.class}, consumer));

    assertEquals(1, acceptedCount.get());
  }

  @Test
  @DisplayName("acceptClientLoop : sort proprement si IOException pendant running")
  void acceptClientLoopIOExceptionWhileRunningTest() throws Exception {
    ServerLifecycleManager manager = new ServerLifecycleManager(12345, "test");

    ServerSocket alwaysFailingSocket =
        new ServerSocket() {
          @Override
          public Socket accept() throws IOException {
            throw new IOException("forced");
          }
        };

    setField(manager, "serverSocket", alwaysFailingSocket);

    AtomicBoolean running = (AtomicBoolean) getField(manager, "running");
    running.set(true);

    AtomicInteger acceptedCount = new AtomicInteger(0);

    assertDoesNotThrow(
        () ->
            invokePrivate(
                manager,
                "acceptClientLoop",
                new Class<?>[] {Consumer.class},
                (Consumer<Socket>) socket -> acceptedCount.incrementAndGet()));

    assertEquals(0, acceptedCount.get());
  }

  @Test
  @DisplayName("acceptClientLoop : ne fait rien si running vaut false")
  void acceptClientLoopWhenNotRunningTest() throws Exception {
    ServerLifecycleManager manager = new ServerLifecycleManager(12345, "test");
    AcceptThenFailServerSocket fakeSocket = new AcceptThenFailServerSocket();

    setField(manager, "serverSocket", fakeSocket);

    AtomicBoolean running = (AtomicBoolean) getField(manager, "running");
    running.set(false);

    AtomicInteger acceptedCount = new AtomicInteger(0);

    assertDoesNotThrow(
        () ->
            invokePrivate(
                manager,
                "acceptClientLoop",
                new Class<?>[] {Consumer.class},
                (Consumer<Socket>) socket -> acceptedCount.incrementAndGet()));

    assertEquals(0, acceptedCount.get());
  }
}
