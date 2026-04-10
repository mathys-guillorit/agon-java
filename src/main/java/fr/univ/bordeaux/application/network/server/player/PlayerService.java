package fr.univ.bordeaux.application.network.server.player;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import java.util.Collection;

/** Defines player registration and access operations for the server. */
public interface PlayerService {

  /**
   * Registers a player or reconnects an existing one.
   *
   * @param clientId the persistent client identifier
   * @param name the display name of the player
   * @param handler the active client handler bound to the player
   * @return the registered player, or {@code null} if registration fails
   */
  OnlinePlayer registerPlayer(String clientId, String name, ClientHandler handler);

  /**
   * Detaches a player from the active server state.
   *
   * @param player the player to detach
   */
  void detachPlayer(OnlinePlayer player);

  /**
   * Returns an active player by id.
   *
   * @param playerId the player identifier
   * @return the active player, or {@code null} if not found
   */
  OnlinePlayer getPlayerById(int playerId);

  /**
   * Returns all active players currently connected to the server.
   *
   * @return the active players collection
   */
  Collection<OnlinePlayer> getActivePlayers();

  /**
   * Returns the number of active players.
   *
   * @return the active player count
   */
  int getPlayerCount();

  /** Clears the active players map. */
  void clearActivePlayers();
}
