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

/** Application shared context. */
public class AppContext implements OnlineGameStartListener {

  private final LocalProfile profile;
  private final AgonClient client;
  private AgonServer server;
  private ClientDiscovery discovery;
  private AppMode mode = AppMode.LOCAL;

  // For the OnlineGame part
  private boolean onlineGameActive = false;
  private int currentOnlineGameId = -1;
  private Color localOnlineColor;
  private boolean myOnlineTurn = false;
  private Match currentOnlineMatch;
  private GameEngine gameEngine;

  /**
   * Creates an application context from an existing local profile.
   *
   * @param profile the local profile of the user
   */
  public AppContext(LocalProfile profile) {
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
  public void setServer(AgonServer server) {
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
   * @throws Exception if discovery cannot be started
   */
  public void ensureDiscoveryStarted() throws Exception {
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
  public void setMode(AppMode mode) {
    this.mode = mode;
  }

  /**
   * Registers the game engine associated with this application context.
   *
   * @param gameEngine the application game engine
   */
  public void setGameEngine(GameEngine gameEngine) {
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
  public void onOnlineGameStarted(OnlineGameInfo info) {
    final Match localMatch =
        MatchFactory.createOnlineMatch(info.getWhitePlayerName(), info.getBlackPlayerName());

    this.onlineGameActive = true;
    this.currentOnlineGameId = info.getGameId();
    this.localOnlineColor = info.getLocalColor();
    this.myOnlineTurn = info.isMyTurn();
    this.currentOnlineMatch = localMatch;

    System.out.println("[ONLINE] Game started. GAME_ID=" + info.getGameId());
    System.out.println("[ONLINE] You are " + info.getLocalColor());
    System.out.println(
        "[ONLINE] WHITE=" + info.getWhitePlayerName() + " BLACK=" + info.getBlackPlayerName());

    if (gameEngine != null) {
      gameEngine.previewMatch(localMatch);
    }

    if (myOnlineTurn) {
      System.out.println("[ONLINE] Your turn");
    } else {
      System.out.println("[ONLINE] Opponent turn");
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
    return currentOnlineGameId;
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
    return currentOnlineMatch;
  }

  /**
   * Updates the local online turn flag.
   *
   * @param myOnlineTurn true if it is now the local player's turn
   */
  public void setMyOnlineTurn(boolean myOnlineTurn) {
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
  public void onLocalMoveConfirmed(String rawMove) {
    if (currentOnlineMatch == null) {
      return;
    }

    MoveParsed parsedMove = MoveProtocolParser.parse(rawMove);
    if (parsedMove == null) {
      System.err.println("[ONLINE] Failed to parse confirmed move: " + rawMove);
      return;
    }

    Move move;

    if (parsedMove.hasSource()) {
      move = new Move(parsedMove.getFromIndex(), parsedMove.getToIndex(), localOnlineColor);
    } else {
      move = new Move(-1, parsedMove.getToIndex(), localOnlineColor);
    }

    boolean ok = currentOnlineMatch.move(move);
    if (!ok) {
      System.err.println("[ONLINE] Failed to apply confirmed local move: " + rawMove);
      return;
    }

    myOnlineTurn = false;

    if (gameEngine != null) {
      gameEngine.previewMatch(currentOnlineMatch);
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
  public void onOpponentMoveReceived(String rawMove) {
    if (currentOnlineMatch == null) {
      return;
    }

    MoveParsed parsedMove = MoveProtocolParser.parse(rawMove);
    if (parsedMove == null) {
      System.err.println("[ONLINE] Failed to parse opponent move: " + rawMove);
      return;
    }

    Color opponentColor = (localOnlineColor == Color.WHITE) ? Color.BLACK : Color.WHITE;

    Move move;
    if (parsedMove.hasSource()) {
      move = new Move(parsedMove.getFromIndex(), parsedMove.getToIndex(), opponentColor);
    } else {
      move = new Move(-1, parsedMove.getToIndex(), opponentColor);
    }

    boolean ok = currentOnlineMatch.move(move);
    if (!ok) {
      System.err.println("[ONLINE] Failed to apply opponent move: " + rawMove);
      return;
    }

    myOnlineTurn = true;

    if (gameEngine != null) {
      gameEngine.previewMatch(currentOnlineMatch);
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
    this.currentOnlineGameId = -1;
    this.myOnlineTurn = false;

    if (this.currentOnlineMatch != null) {
      this.currentOnlineMatch.quit();
    }

    this.currentOnlineMatch = null;

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
  public void onGameOver(String line) {
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
    if (gameEngine != null && currentOnlineMatch != null) {
      gameEngine.previewMatch(currentOnlineMatch);
    }
  }
}
