package fr.univ.bordeaux.application.network.server.player;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Stores and manages active and reconnectable players for the server. */
public class PlayerRegistry implements PlayerService {

  /** Generator used to assign unique player identifiers. */
  private final AtomicInteger nextPlayerId = new AtomicInteger(1);

  /** Registered players indexed by their persistent client id. */
  private final Map<String, OnlinePlayer> playersByClientId = new ConcurrentHashMap<>();

  /** Active connected players indexed by runtime player id. */
  private final Map<Integer, OnlinePlayer> activePlayers = new ConcurrentHashMap<>();

  /** Creates a new player registry. */
  public PlayerRegistry() {
    // Default constructor.
  }

  /**
   * Registers a player or reconnects an existing one.
   *
   * @param clientId the persistent client identifier
   * @param name the display name of the player
   * @param handler the active client handler bound to the player
   * @return the registered player, or {@code null} if registration fails
   */
  @Override
  public OnlinePlayer registerPlayer(
      final String clientId, final String name, final ClientHandler handler) {
    if (!isValidRegistration(clientId, name)) {
      return null;
    }

    final String cleanClientId = clientId.trim();
    final OnlinePlayer existingPlayer = playersByClientId.get(cleanClientId);

    if (existingPlayer != null) {
      reconnectPlayer(existingPlayer, handler);
      return existingPlayer;
    }

    return createNewPlayer(cleanClientId, name, handler);
  }

  /**
   * Detaches a player from the active server state.
   *
   * @param player the player to detach
   */
  @Override
  public void detachPlayer(final OnlinePlayer player) {
    if (player == null) {
      return;
    }

    player.setHandler(null);
    player.setStatus(PlayerStatus.IDLE);
    activePlayers.remove(player.getId());
  }

  /**
   * Returns an active player by id.
   *
   * @param playerId the player identifier
   * @return the active player, or {@code null} if not found
   */
  @Override
  public OnlinePlayer getPlayerById(final int playerId) {
    return activePlayers.get(playerId);
  }

  /**
   * Returns all active players currently connected to the server.
   *
   * @return the active players collection
   */
  @Override
  public Collection<OnlinePlayer> getActivePlayers() {
    return activePlayers.values();
  }

  /**
   * Returns the number of active players.
   *
   * @return the active player count
   */
  @Override
  public int getPlayerCount() {
    return activePlayers.size();
  }

  /** Clears the active players map. */
  @Override
  public void clearActivePlayers() {
    activePlayers.clear();
  }

  /**
   * Checks whether the registration payload is valid.
   *
   * @param clientId the persistent client identifier
   * @param name the player display name
   * @return {@code true} if the registration payload is valid, {@code false} otherwise
   */
  private boolean isValidRegistration(final String clientId, final String name) {
    return clientId != null && !clientId.isBlank() && name != null && !name.isBlank();
  }

  /**
   * Reconnects an already known player with a new handler.
   *
   * @param existingPlayer the player to reconnect
   * @param handler the new active handler
   */
  private void reconnectPlayer(final OnlinePlayer existingPlayer, final ClientHandler handler) {
    existingPlayer.setHandler(handler);
    existingPlayer.setStatus(PlayerStatus.IDLE);
    activePlayers.put(existingPlayer.getId(), existingPlayer);
  }

  /**
   * Creates and stores a new player.
   *
   * @param cleanClientId the normalized client identifier
   * @param name the player display name
   * @param handler the active handler associated with the player
   * @return the newly created player
   */
  private OnlinePlayer createNewPlayer(
      final String cleanClientId, final String name, final ClientHandler handler) {
    final int playerId = nextPlayerId.getAndIncrement();
    final OnlinePlayer player =
        new OnlinePlayer(playerId, cleanClientId, name.trim(), PlayerStatus.IDLE, handler);

    playersByClientId.put(cleanClientId, player);
    activePlayers.put(playerId, player);

    return player;
  }
}
