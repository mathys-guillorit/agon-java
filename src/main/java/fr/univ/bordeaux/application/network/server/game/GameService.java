package fr.univ.bordeaux.application.network.server.game;

/** Defines the game-related operations exposed by the server game subsystem. */
public interface GameService {

  /**
   * Returns the formatted scoreboard response.
   *
   * @return a protocol-compatible scoreboard response
   */
  String getScoreboard();

  /**
   * Returns the number of currently active game sessions.
   *
   * @return the active game count
   */
  int getActiveGameCount();

  /**
   * Indicates whether a player is currently involved in a game.
   *
   * @param playerId the player identifier
   * @return {@code true} if the player is currently in a game, {@code false} otherwise
   */
  boolean isPlayerInGame(int playerId);

  /**
   * Returns the game id associated with a player.
   *
   * @param playerId the player identifier
   * @return the game id, or {@code null} if the player is not in a game
   */
  Integer getGameIdByPlayer(int playerId);

  /**
   * Returns a game session by id.
   *
   * @param gameId the game identifier
   * @return the corresponding game session, or {@code null} if not found
   */
  ServerGameSession getGameById(int gameId);

  /**
   * Starts a new game between two players.
   *
   * @param requesterId the identifier of the requesting player
   * @param targetId the identifier of the opponent player
   * @param mode the selected game mode
   * @return the created game session, or {@code null} if creation failed
   */
  ServerGameSession startNewGame(int requesterId, int targetId, GameMode mode);

  /**
   * Finishes an active game session.
   *
   * @param session the game session to finish
   * @param winnerPlayerId the winner player identifier
   * @param reason the end-of-game reason
   */
  void finishGame(ServerGameSession session, int winnerPlayerId, String reason);

  /**
   * Returns the lobby associated with a player.
   *
   * @param playerId the player identifier
   * @return the corresponding lobby, or {@code null} if not found
   */
  GameLobby getLobbyByPlayer(int playerId);

  /**
   * Creates a new lobby involving two players.
   *
   * @param hostId the host player identifier
   * @param guestId the guest player identifier
   * @return the created lobby
   */
  GameLobby createLobby(int hostId, int guestId);

  /**
   * Removes the lobby associated with a player and resets the involved players to idle.
   *
   * @param playerId the player identifier
   * @return the removed lobby, or {@code null} if no lobby existed
   */
  GameLobby removeLobbyForPlayer(int playerId);

  /**
   * Starts a game from a host-owned lobby.
   *
   * @param hostId the host player identifier
   * @param mode the selected game mode
   * @return the created game session, or {@code null} if creation failed
   */
  ServerGameSession chooseMode(int hostId, GameMode mode);

  /**
   * Returns the scoreboard statistics of a player, creating them if missing.
   *
   * @param playerName the player display name
   * @return the player statistics entry
   */
  ServerPlayerStats getPlayerStats(String playerName);

  /** Clears all runtime game and lobby state. */
  void clearRuntimeState();
}
