package fr.univ.bordeaux.application;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.match.MatchFactory;
import fr.univ.bordeaux.application.network.OnlineGameInfo;
import fr.univ.bordeaux.application.network.OnlineGameStartListener;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.client.ClientDiscovery;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.application.network.protocol.MoveParsed;
import fr.univ.bordeaux.application.network.protocol.MoveProtocolParser;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.technical.utils.GameLogger;
import java.io.IOException;
import java.util.logging.Logger;

/** Application shared context. */
public class AppContext implements OnlineGameStartListener {
  /** Local user profile. */
  private LocalProfile profile;

  /** Shared TCP client instance. */
  private final AgonClient client;

  /** Current local server instance. */
  private AgonServer server;

  /** UDP discovery instance used by the client. */
  private ClientDiscovery discovery;

  /** Current operating mode of the application. */
  private AppMode mode = AppMode.LOCAL;

  /** Indicates whether an online game is currently active. */
  private boolean onlineGameActive;

  /** Identifier of the active online game, or -1 if none. */
  private int onlineGameId = -1;

  /** Color assigned to the local player in the online game. */
  private Color localOnlineColor;

  /** Indicates whether it is currently the local player's turn. */
  private boolean myOnlineTurn;

  /** Current local visual match for the online game. */
  private Match onlineMatch;

  /** Game engine used to preview and refresh the board. */
  private GameEngine gameEngine;

  /**
   * Creates an application context from an existing local profile.
   *
   * @param profile the local profile of the user
   */
  public AppContext(final LocalProfile profile) {
    this.profile = profile;
    this.client = new AgonClient(profile);
    this.client.setOnlineGameStartListener(this);
  }

  /**
   * Returns the shared TCP client instance.
   *
   * @return the {@link AgonClient} instance
   */
  public AgonClient getClient() {
    return client;
  }

  /**
   * Returns the local profile.
   *
   * @return the {@link LocalProfile} instance
   */
  public LocalProfile getProfile() {
    return profile;
  }

  /**
   * Returns the current local server instance.
   *
   * @return the {@link AgonServer} instance, or {@code null} if none
   */
  public AgonServer getServer() {
    return server;
  }

  /**
   * Sets the current local server instance.
   *
   * @param server the {@link AgonServer} instance to store
   */
  public void setServer(final AgonServer server) {
    this.server = server;
  }

  /**
   * Convenience method to check whether the client is connected.
   *
   * @return true if the client is connected
   */
  public boolean isConnected() {
    return client.isConnected();
  }

  /**
   * Returns the UDP discovery instance, if already started.
   *
   * @return the {@link ClientDiscovery} instance, or {@code null}
   */
  public ClientDiscovery getDiscovery() {
    return discovery;
  }

  /**
   * Starts UDP discovery if not already started.
   *
   * @throws IOException if discovery cannot be started
   */
  public void ensureDiscoveryStarted() throws IOException {
    if (discovery == null) {
      discovery = new ClientDiscovery();
      discovery.start();
    }
  }

  /**
   * Returns the current application mode.
   *
   * @return the active application mode
   */
  public AppMode getMode() {
    return mode;
  }

  /**
   * Updates the current application mode.
   *
   * @param mode the new application mode
   */
  public void setMode(final AppMode mode) {
    this.mode = mode;
  }

  /**
   * Registers the game engine associated with this application context.
   *
   * @param gameEngine the application game engine
   */
  public void setGameEngine(final GameEngine gameEngine) {
    this.gameEngine = gameEngine;
  }

  /**
   * Called when an online game has started.
   *
   * <p>This method creates the local visual match used by the UI, stores the online game state, and
   * injects the match into the game engine.
   *
   * @param info the parsed online game information
   */
  @Override
  public void onOnlineGameStarted(final OnlineGameInfo info) {
    final Match localMatch =
        MatchFactory.createOnlineMatch(
            info.getWhitePlayerName(), info.getBlackPlayerName(), info.isBlitzMode());
    final Color localColor = extractLocalColor(info);

    this.onlineGameActive = true;
    this.onlineGameId = info.getGameId();
    this.localOnlineColor = localColor;
    this.myOnlineTurn = info.isMyTurn();
    this.onlineMatch = localMatch;

    GameLogger.info("[ONLINE] Game started. GAME_ID=" + info.getGameId());
    GameLogger.info("[ONLINE] You are " + localColor);
    GameLogger.info(
        "[ONLINE] WHITE=" + info.getWhitePlayerName() + " BLACK=" + info.getBlackPlayerName());

    if (gameEngine != null) {
      gameEngine.previewMatch(localMatch);
    }

    if (myOnlineTurn) {
      GameLogger.info("[ONLINE] Your turn");
    } else {
      GameLogger.info("[ONLINE] Opponent turn");
    }
  }

  /**
   * Indicates whether an online game is currently active.
   *
   * @return true if an online match is active, false otherwise
   */
  public boolean isOnlineGameActive() {
    return onlineGameActive;
  }

  /**
   * Returns the identifier of the current online game.
   *
   * @return the current online game ID, or -1 if none is active
   */
  public int getCurrentOnlineGameId() {
    return onlineGameId;
  }

  /**
   * Returns the color assigned to the local player in the online match.
   *
   * @return the local player's color
   */
  public Color getLocalOnlineColor() {
    return localOnlineColor;
  }

  /**
   * Indicates whether it is currently the local player's turn.
   *
   * @return true if it is the local player's turn, false otherwise
   */
  public boolean isMyOnlineTurn() {
    return myOnlineTurn;
  }

  /**
   * Returns the current local visual representation of the online match.
   *
   * @return the current online match, or null if none is active
   */
  public Match getCurrentOnlineMatch() {
    return onlineMatch;
  }

  /**
   * Updates the local online turn flag.
   *
   * @param myOnlineTurn true if it is now the local player's turn
   */
  public void setMyOnlineTurn(final boolean myOnlineTurn) {
    this.myOnlineTurn = myOnlineTurn;
  }

  /**
   * Called when the server confirms a move played by the local player.
   *
   * <p>The confirmed move is applied to the local visual online match, the board is refreshed, and
   * the turn is passed to the opponent.
   *
   * @param rawMove compact move text (e.g. "e2e4")
   */
  @Override
  public void onLocalMoveConfirmed(final String rawMove) {
    boolean applied = false;

    if (onlineMatch != null) {
      final MoveParsed parsedMove = MoveProtocolParser.parse(rawMove);

      if (parsedMove == null) {
        GameLogger.error("[ONLINE] Failed to parse confirmed move: " + rawMove);
      } else {
        final Move move = buildMove(parsedMove, localOnlineColor);
        final boolean moveApplied = onlineMatch.move(move);

        if (!moveApplied) {
          GameLogger.error("[ONLINE] Failed to apply confirmed local move: " + rawMove);
        } else {
          myOnlineTurn = false;
          onlineMatch.startTurn();
          applied = true;
        }
      }
    }

    if (applied && gameEngine != null) {
      gameEngine.previewMatch(onlineMatch);
    }
  }

  /**
   * Called when the client receives a move played by the opponent.
   *
   * <p>The move is applied to the local visual online match, the board is refreshed, and the turn
   * is passed to the local player.
   *
   * @param rawMove compact move text (e.g. "e7e5")
   */
  @Override
  public void onOpponentMoveReceived(final String rawMove) {
    boolean applied = false;

    if (onlineMatch != null) {
      final MoveParsed parsedMove = MoveProtocolParser.parse(rawMove);

      if (parsedMove == null) {
        GameLogger.error("[ONLINE] Failed to parse opponent move: " + rawMove);
      } else {
        final Color opponentColor = getOpponentColor();
        final Move move = buildMove(parsedMove, opponentColor);
        final boolean moveApplied = onlineMatch.move(move);

        if (!moveApplied) {
          GameLogger.error("[ONLINE] Failed to apply opponent move: " + rawMove);
        } else {
          myOnlineTurn = true;
          onlineMatch.startTurn();
          applied = true;
        }
      }
    }

    if (applied && gameEngine != null) {
      gameEngine.previewMatch(onlineMatch);
    }
  }

  /**
   * Leaves the current online game and clears the associated local state.
   *
   * <p>This method resets online game flags, stops the local visual match, and clears the board
   * preview from the game engine.
   */
  public void leaveOnlineGame() {
    this.onlineGameActive = false;
    this.onlineGameId = -1;
    this.myOnlineTurn = false;

    if (this.onlineMatch != null) {
      this.onlineMatch.quit();
      this.onlineMatch = null;
    }

    if (this.gameEngine != null) {
      this.gameEngine.clearBoardPreview();
    }
  }

  /**
   * Called when the server notifies that the online game is over.
   *
   * <p>This method clears the current online game state.
   *
   * @param line the raw protocol message describing the game result
   */
  @Override
  public void onGameOver(final String line) {
    leaveOnlineGame();
  }

  /**
   * Called when the online board display needs to be refreshed.
   *
   * <p>If an online match is currently available, its board is previewed again through the game
   * engine.
   */
  @Override
  public void onOnlineBoardRefreshRequested() {
    if (gameEngine != null && onlineMatch != null) {
      gameEngine.previewMatch(onlineMatch);
    }
  }

  /**
   * Builds a move from parsed protocol data.
   *
   * @param parsedMove the parsed move
   * @param color the color associated with the move
   * @return the constructed move
   */
  private Move buildMove(final MoveParsed parsedMove, final Color color) {
    if (parsedMove.hasSource()) {
      return new Move(parsedMove.getFromIndex(), parsedMove.getToIndex(), color);
    }
    return new Move(-1, parsedMove.getToIndex(), color);
  }

  /**
   * Returns the local color from the online game information.
   *
   * @param info the online game information
   * @return the local player's color
   */
  private Color extractLocalColor(final OnlineGameInfo info) {
    return info.getLocalColor();
  }

  /**
   * Returns the opponent color.
   *
   * @return the opponent color
   */
  private Color getOpponentColor() {
    if (localOnlineColor == Color.WHITE) {
      return Color.BLACK;
    }
    return Color.WHITE;
  }

  /**
   * Set the player's name.
   *
   * @param name the player's name.
   */
  public void setPlayerName(String name) {
    this.profile.setName(name);
  }
}
