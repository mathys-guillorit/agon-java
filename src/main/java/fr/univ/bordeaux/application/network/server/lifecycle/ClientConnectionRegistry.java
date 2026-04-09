package fr.univ.bordeaux.application.network.server.lifecycle;

import fr.univ.bordeaux.application.network.server.ClientHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores the active client handlers currently connected to the server. */
public class ClientConnectionRegistry {

  /** Active client handlers. */
  private final List<ClientHandler> currentClients =
      Collections.synchronizedList(new ArrayList<>());

  /** Creates a new client connection registry. */
  public ClientConnectionRegistry() {
    // Default constructor.
  }

  /**
   * Adds a connected client handler.
   *
   * @param handler the handler to register
   */
  public void add(final ClientHandler handler) {
    currentClients.add(handler);
  }

  /**
   * Removes a connected client handler.
   *
   * @param handler the handler to remove
   */
  public void remove(final ClientHandler handler) {
    currentClients.remove(handler);
  }

  /**
   * Returns the number of connected clients.
   *
   * @return the connected client count
   */
  public int count() {
    return currentClients.size();
  }

  /**
   * Creates a stable snapshot of currently connected client handlers.
   *
   * @return a copy of the connected client handlers
   */
  public List<ClientHandler> snapshot() {
    synchronized (currentClients) {
      return new ArrayList<>(currentClients);
    }
  }

  /** Clears the registry of connected clients. */
  public void clear() {
    currentClients.clear();
  }
}
