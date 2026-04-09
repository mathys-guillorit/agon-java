package fr.univ.bordeaux.application.network.client.runtime;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Defines the low-level transport operations used by the TCP game client. */
public interface ClientTransport {

  /**
   * Opens a TCP connection to the remote server.
   *
   * @param host the remote server host
   * @param port the remote server port
   * @throws IOException if the connection cannot be opened
   */
  void connect(String host, int port) throws IOException;

  /**
   * Indicates whether the transport is currently connected.
   *
   * @return {@code true} if the transport is connected, {@code false} otherwise
   */
  boolean isConnected();

  /**
   * Sends one protocol line to the server.
   *
   * @param message the protocol line to send
   * @throws IOException if the line cannot be written
   */
  void sendLine(String message) throws IOException;

  /**
   * Waits for the next synchronous server response.
   *
   * @param timeoutMs maximum wait time in milliseconds
   * @return the received response line, or {@code null} if timeout or disconnection occurs
   */
  String waitResponse(long timeoutMs);

  /** Starts the background reader for incoming protocol lines. */
  void startReader();

  /** Stops the transport and closes underlying resources. */
  void stop();

  /**
   * Registers the predicate used to identify asynchronous protocol events.
   *
   * @param eventPredicate predicate testing whether a line is asynchronous
   */
  void setAsyncEventPredicate(Predicate<String> eventPredicate);

  /**
   * Registers the callback used to handle asynchronous protocol events.
   *
   * @param eventConsumer callback receiving asynchronous lines
   */
  void setAsyncEventConsumer(Consumer<String> eventConsumer);
}
