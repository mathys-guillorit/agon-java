package fr.univ.bordeaux.application.network.client.runtime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Implements the TCP transport used by the online game client. */
@SuppressWarnings("PMD.DoNotUseThreads")
public class SocketClientTransport implements ClientTransport {

  private static final String BYE_MESSAGE = "BYE";

  /** TCP socket connected to the remote server. */
  private Socket socket;

  /** Reader used to consume server protocol lines. */
  private BufferedReader serverReader;

  /** Writer used to send protocol lines to the server. */
  private BufferedWriter serverWriter;

  /** Indicates whether the reader loop is still active. */
  private final AtomicBoolean readerRunning = new AtomicBoolean(false);

  /** Queue storing synchronous responses in arrival order. */
  private final Deque<String> pendingResponses = new LinkedList<>();

  /** Lock used to wait for incoming synchronous responses. */
  private final ReentrantLock responseLock = new ReentrantLock();

  /** Condition used to signal arrival of synchronous responses. */
  private final Condition responseAvailable = responseLock.newCondition();

  /** Lock used to serialize writes to the server output stream. */
  private final ReentrantLock sendLock = new ReentrantLock();

  /** Single background executor dedicated to the socket reader loop. */
  private final ExecutorService readerExecutor =
      Executors.newSingleThreadExecutor(
          runnable -> {
            final Thread thread = new Thread(runnable, "AgonClient-Reader");
            thread.setDaemon(true);
            return thread;
          });

  /** Predicate used to detect asynchronous protocol events. */
  private Predicate<String> eventPredicate = line -> false;

  /** Consumer used to handle asynchronous protocol events. */
  private Consumer<String> eventConsumer = line -> {};

  /** Creates a new socket client transport. */
  public SocketClientTransport() {
    // Explicit constructor required by PMD.
  }

  /**
   * Opens a TCP connection to the remote server.
   *
   * @param host the remote server host
   * @param port the remote server port
   * @throws IOException if the connection cannot be opened
   */
  @Override
  public void connect(final String host, final int port) throws IOException {
    socket = new Socket(host, port);
    socket.setSoTimeout(0);

    serverReader =
        new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
    serverWriter =
        new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));
  }

  /**
   * Indicates whether the transport is currently connected.
   *
   * @return {@code true} if the transport is connected, {@code false} otherwise
   */
  @Override
  public boolean isConnected() {
    return socket != null && socket.isConnected() && !socket.isClosed();
  }

  /**
   * Sends one protocol line to the server.
   *
   * @param message the protocol line to send
   * @throws IOException if the line cannot be written
   */
  @Override
  public void sendLine(final String message) throws IOException {
    sendLock.lock();
    try {
      if (serverWriter == null) {
        throw new IOException("Not connected");
      }

      serverWriter.write(message);
      serverWriter.write('\n');
      serverWriter.flush();
    } finally {
      sendLock.unlock();
    }
  }

  /**
   * Waits for the next synchronous server response.
   *
   * @param timeoutMs maximum wait time in milliseconds
   * @return the received response line, or {@code null} if timeout/disconnection occurs
   */
  @Override
  public String waitResponse(final long timeoutMs) {
    final long deadline = System.currentTimeMillis() + timeoutMs;

    responseLock.lock();
    try {
      return waitForResponse(deadline);
    } finally {
      responseLock.unlock();
    }
  }

  /**
   * Waits until a valid response is available, timeout expires or connection closes.
   *
   * @param deadline absolute timeout timestamp in milliseconds
   * @return a non-blank response line, or {@code null} if none is received
   */
  private String waitForResponse(final long deadline) {
    String responseLine = pollNextResponse();

    while (responseLine == null && isConnected()) {
      final long remainingTime = deadline - System.currentTimeMillis();
      if (remainingTime <= 0) {
        return null;
      }

      if (!awaitResponse(remainingTime)) {
        return null;
      }

      responseLine = pollNextResponse();
    }

    return responseLine;
  }

  /**
   * Polls the next non-blank response from the queue.
   *
   * @return the next valid response, or {@code null} if none is available
   */
  private String pollNextResponse() {
    while (!pendingResponses.isEmpty()) {
      final String queuedLine = pendingResponses.removeFirst();
      if (queuedLine != null && !queuedLine.isBlank()) {
        return queuedLine;
      }
    }
    return null;
  }

  /**
   * Waits for a response signal during the specified duration.
   *
   * @param remainingTime maximum wait time in milliseconds
   * @return {@code true} if waiting completed normally, {@code false} if interrupted
   */
  private boolean awaitResponse(final long remainingTime) {
    try {
      responseAvailable.awaitNanos(remainingTime * 1_000_000L);
      return true;
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  /** Starts the background reader responsible for dispatching incoming protocol lines. */
  @Override
  public void startReader() {
    if (readerRunning.compareAndSet(false, true)) {
      readerExecutor.submit(this::readLoop);
    }
  }

  /** Stops the transport and closes underlying resources. */
  @Override
  public void stop() {
    readerRunning.set(false);
    clearPendingResponses();
    closeSocketQuietly();
  }

  /**
   * Registers the predicate used to identify asynchronous protocol events.
   *
   * @param eventPredicate predicate testing whether a line is asynchronous
   */
  @Override
  public void setAsyncEventPredicate(final Predicate<String> eventPredicate) {
    this.eventPredicate = eventPredicate;
  }

  /**
   * Registers the callback used to handle asynchronous protocol events.
   *
   * @param eventConsumer callback receiving asynchronous lines
   */
  @Override
  public void setAsyncEventConsumer(final Consumer<String> eventConsumer) {
    this.eventConsumer = eventConsumer;
  }

  /** Main background reader loop. */
  private void readLoop() {
    try {
      while (readerRunning.get() && isConnected()) {
        final String receivedLine = serverReader.readLine();

        if (mustStopReading(receivedLine)) {
          stopReaderLoop();
        } else if (eventPredicate.test(receivedLine)) {
          eventConsumer.accept(receivedLine);
        } else {
          enqueueSynchronousResponse(receivedLine);
        }
      }
    } catch (IOException exception) {
      stop();
    }
  }

  /**
   * Indicates whether the reader loop should stop for the received line.
   *
   * @param receivedLine the received line
   * @return {@code true} if the loop must stop, {@code false} otherwise
   */
  private boolean mustStopReading(final String receivedLine) {
    return receivedLine == null || BYE_MESSAGE.equalsIgnoreCase(receivedLine.trim());
  }

  /** Stops the reader loop and transport. */
  private void stopReaderLoop() {
    readerRunning.set(false);
    stop();
  }

  /**
   * Enqueues one synchronous response line and wakes waiting threads.
   *
   * @param responseLine the response line to enqueue
   */
  private void enqueueSynchronousResponse(final String responseLine) {
    responseLock.lock();
    try {
      pendingResponses.addLast(responseLine);
      responseAvailable.signalAll();
    } finally {
      responseLock.unlock();
    }
  }

  /** Clears pending responses and wakes waiting threads. */
  private void clearPendingResponses() {
    responseLock.lock();
    try {
      pendingResponses.clear();
      responseAvailable.signalAll();
    } finally {
      responseLock.unlock();
    }
  }

  /** Closes the current socket if it exists. */
  private void closeSocketQuietly() {
    try {
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException ignored) {
      // Ignored
    }
  }
}
