package fr.univ.bordeaux.application.network.server.game;

import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.match.MatchFactory;
import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.player.PlayerStatus;
import fr.univ.bordeaux.application.network.server.player.PlayerService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/** Manages game sessions, lobbies, and scoreboard state for the server. */
public class GameManager implements GameService {

  /** Service used to access connected players. */
  private final PlayerService playerService;

  /** Component responsible for sending end-of-game notifications. */
  private final GameResultNotifier notifier;

  /** Persistent scoreboard of all known players. */
  private final ServerScoreboard scoreboard = new ServerScoreboard();

  /** Active game sessions indexed by game id. */
  private final Map<Integer, ServerGameSession> activeGames = new ConcurrentHashMap<>();

  /** Mapping from player id to current game id. */
  private final Map<Integer, Integer> playerToGame = new ConcurrentHashMap<>();

  /** Lobby associated with each involved player. */
  private final Map<Integer, GameLobby> lobbiesByPlayer = new ConcurrentHashMap<>();

  /** Generator used to assign unique game identifiers. */
  private final AtomicInteger nextGameId = new AtomicInteger(1);

  /** Lock protecting compound game and lobby state updates. */
  private final ReentrantLock stateLock = new ReentrantLock();

  /**
   * Creates a new GameManager.
   *
   * @param playerService the player service used to access connected players
   * @param notifier the notifier used to send game result messages
   */
  public GameManager(final PlayerService playerService, final GameResultNotifier notifier) {
    this.playerService = playerService;
    this.notifier = notifier;
  }

  /**
   * Returns the formatted scoreboard response.
   *
   * @return a protocol-compatible scoreboard response
   */
  @Override
  public String getScoreboard() {
    final String scoreboardText;

    if (scoreboard.isEmpty()) {
      scoreboardText = "SCOREBOARD_EMPTY\nEND";
    } else {
      final StringBuilder builder = new StringBuilder(128);
      builder.append("=== SCOREBOARD ===\n");

      for (final ServerPlayerStats stats : scoreboard.getAllStats()) {
        appendScoreboardLine(builder, stats);
      }

      builder.append("==================\nEND");
      scoreboardText = builder.toString();
    }

    return scoreboardText;
  }

  /**
   * Returns the number of currently active game sessions.
   *
   * @return the active game count
   */
  @Override
  public int getActiveGameCount() {
    return activeGames.size();
  }

  /**
   * Indicates whether a player is currently involved in a game.
   *
   * @param playerId the player identifier
   * @return {@code true} if the player is currently in a game, {@code false} otherwise
   */
  @Override
  public boolean isPlayerInGame(final int playerId) {
    return playerToGame.containsKey(playerId);
  }

  /**
   * Returns the game id associated with a player.
   *
   * @param playerId the player identifier
   * @return the game id, or {@code null} if the player is not in a game
   */
  @Override
  public Integer getGameIdByPlayer(final int playerId) {
    return playerToGame.get(playerId);
  }

  /**
   * Returns a game session by id.
   *
   * @param gameId the game identifier
   * @return the corresponding game session, or {@code null} if not found
   */
  @Override
  public ServerGameSession getGameById(final int gameId) {
    return activeGames.get(gameId);
  }

  /**
   * Starts a new game between two players.
   *
   * @param requesterId the identifier of the requesting player
   * @param targetId the identifier of the opponent player
   * @param mode the selected game mode
   * @return the created game session, or {@code null} if creation failed
   */
  @Override
  public ServerGameSession startNewGame(
      final int requesterId, final int targetId, final GameMode mode) {
    final OnlinePlayer requester = playerService.getPlayerById(requesterId);
    final OnlinePlayer target = playerService.getPlayerById(targetId);

    if (requester == null || target == null || mode == null) {
      return null;
    }

    final int gameId = nextGameId.getAndIncrement();
    final boolean requesterWhite = Math.random() < 0.5;
    final OnlinePlayer whitePlayer = requesterWhite ? requester : target;
    final OnlinePlayer blackPlayer = requesterWhite ? target : requester;

    final Match match =
        MatchFactory.createOnlineMatch(
            whitePlayer.getName(), blackPlayer.getName(), mode == GameMode.BLITZ);

    final ServerGameSession session =
        new ServerGameSession(gameId, whitePlayer, blackPlayer, match);

    activeGames.put(gameId, session);
    playerToGame.put(requester.getId(), gameId);
    playerToGame.put(target.getId(), gameId);

    requester.setStatus(PlayerStatus.INGAME);
    target.setStatus(PlayerStatus.INGAME);

    scoreboard.getOrCreateStats(requester.getName());
    scoreboard.getOrCreateStats(target.getName());

    return session;
  }

  /**
   * Finishes an active game session.
   *
   * @param session the game session to finish
   * @param winnerPlayerId the winner player identifier
   * @param reason the end-of-game reason
   */
  @Override
  public void finishGame(
      final ServerGameSession session, final int winnerPlayerId, final String reason) {
    stateLock.lock();
    try {
      final ServerGameSession removedSession = removeActiveSession(session);
      if (removedSession != null) {
        finalizeRemovedSession(removedSession, winnerPlayerId, reason);
      }
    } finally {
      stateLock.unlock();
    }
  }

  /**
   * Returns the lobby associated with a player.
   *
   * @param playerId the player identifier
   * @return the corresponding lobby, or {@code null} if not found
   */
  @Override
  public GameLobby getLobbyByPlayer(final int playerId) {
    return lobbiesByPlayer.get(playerId);
  }

  /**
   * Creates a new lobby involving two players.
   *
   * @param hostId the host player identifier
   * @param guestId the guest player identifier
   * @return the created lobby
   */
  @Override
  public GameLobby createLobby(final int hostId, final int guestId) {
    final GameLobby lobby = new GameLobby(hostId, guestId);

    stateLock.lock();
    try {
      lobbiesByPlayer.put(hostId, lobby);
      lobbiesByPlayer.put(guestId, lobby);
    } finally {
      stateLock.unlock();
    }

    return lobby;
  }

  /**
   * Removes the lobby associated with a player and resets the involved players to idle.
   *
   * @param playerId the player identifier
   * @return the removed lobby, or {@code null} if no lobby existed
   */
  @Override
  public GameLobby removeLobbyForPlayer(final int playerId) {
    final GameLobby lobby;

    stateLock.lock();
    try {
      lobby = lobbiesByPlayer.remove(playerId);

      if (lobby != null) {
        lobbiesByPlayer.remove(lobby.getHostId());
        lobbiesByPlayer.remove(lobby.getGuestId());

        resetPlayerToIdle(playerService.getPlayerById(lobby.getHostId()));
        resetPlayerToIdle(playerService.getPlayerById(lobby.getGuestId()));
      }
    } finally {
      stateLock.unlock();
    }

    return lobby;
  }

  /**
   * Starts a game from a host-owned lobby.
   *
   * @param hostId the host player identifier
   * @param mode the selected game mode
   * @return the created game session, or {@code null} if creation failed
   */
  @Override
  public ServerGameSession chooseMode(final int hostId, final GameMode mode) {
    ServerGameSession session = null;

    stateLock.lock();
    try {
      if (mode != null) {
        final GameLobby lobby = lobbiesByPlayer.get(hostId);

        if (lobby != null && lobby.getHostId() == hostId) {
          final int guestId = lobby.getGuestId();

          lobbiesByPlayer.remove(hostId);
          lobbiesByPlayer.remove(guestId);

          session = startNewGame(hostId, guestId, mode);

          if (session == null) {
            resetPlayerToIdle(playerService.getPlayerById(hostId));
            resetPlayerToIdle(playerService.getPlayerById(guestId));
          }
        }
      }
    } finally {
      stateLock.unlock();
    }

    return session;
  }

  /**
   * Returns the scoreboard statistics of a player, creating them if missing.
   *
   * @param playerName the player display name
   * @return the player statistics entry
   */
  @Override
  public ServerPlayerStats getPlayerStats(final String playerName) {
    return scoreboard.getOrCreateStats(playerName);
  }

  /** Clears all runtime game and lobby state. */
  @Override
  public void clearRuntimeState() {
    stateLock.lock();
    try {
      activeGames.clear();
      playerToGame.clear();
      lobbiesByPlayer.clear();
    } finally {
      stateLock.unlock();
    }
  }

  /**
   * Removes an active session from storage.
   *
   * @param session the session to remove
   * @return the removed session, or {@code null} if none was removed
   */
  private ServerGameSession removeActiveSession(final ServerGameSession session) {
    ServerGameSession removedSession = null;

    if (session != null) {
      removedSession = activeGames.remove(session.getGameId());
    }

    return removedSession;
  }

  /**
   * Finalizes the cleanup and notifications for a removed session.
   *
   * @param session the removed session
   * @param winnerPlayerId the winner player id
   * @param reason the end-of-game reason
   */
  private void finalizeRemovedSession(
      final ServerGameSession session, final int winnerPlayerId, final String reason) {
    final OnlinePlayer whitePlayer = session.getwhitePlayer();
    final OnlinePlayer blackPlayer = session.getblackPlayer();

    playerToGame.remove(whitePlayer.getId());
    playerToGame.remove(blackPlayer.getId());

    whitePlayer.setStatus(PlayerStatus.IDLE);
    blackPlayer.setStatus(PlayerStatus.IDLE);

    final OnlinePlayer winner = findWinner(whitePlayer, blackPlayer, winnerPlayerId);
    final OnlinePlayer loser = session.getOpponent(winnerPlayerId);

    if (winner != null && loser != null) {
      updateScoreboard(winner, loser);
      notifier.notifyGameResult(winner, loser, reason, true);
      notifier.notifyGameResult(loser, winner, reason, false);
    }
  }

  /**
   * Appends one scoreboard line to the output buffer.
   *
   * @param builder the target response builder
   * @param stats the player statistics to append
   */
  private void appendScoreboardLine(final StringBuilder builder, final ServerPlayerStats stats) {
    builder
        .append("NAME=")
        .append(stats.getPlayerName())
        .append(" WINS=")
        .append(stats.getWins())
        .append(" LOSSES=")
        .append(stats.getLosses())
        .append(" GAMES=")
        .append(stats.getGames())
        .append('\n');
  }

  /**
   * Finds the winner player from the two players in a finished game.
   *
   * @param whitePlayer the white player
   * @param blackPlayer the black player
   * @param winnerPlayerId the winner player identifier
   * @return the winner player, or {@code null} if the id does not match any game participant
   */
  private OnlinePlayer findWinner(
      final OnlinePlayer whitePlayer, final OnlinePlayer blackPlayer, final int winnerPlayerId) {
    OnlinePlayer winner = null;

    if (whitePlayer.getId() == winnerPlayerId) {
      winner = whitePlayer;
    } else if (blackPlayer.getId() == winnerPlayerId) {
      winner = blackPlayer;
    }

    return winner;
  }

  /**
   * Updates the persistent scoreboard after a finished game.
   *
   * @param winner the winning player
   * @param loser the losing player
   */
  private void updateScoreboard(final OnlinePlayer winner, final OnlinePlayer loser) {
    scoreboard.getOrCreateStats(winner.getName()).addWin();
    scoreboard.getOrCreateStats(loser.getName()).addLoss();
  }

  /**
   * Resets a player status to idle when the player exists.
   *
   * @param player the player to reset
   */
  private void resetPlayerToIdle(final OnlinePlayer player) {
    if (player != null) {
      player.setStatus(PlayerStatus.IDLE);
    }
  }
}
