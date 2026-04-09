package fr.univ.bordeaux.application;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.match.Match;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.OnlineGameInfo;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.application.network.server.AgonServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AppContextTest {

  private AppContext context;
  private LocalProfile profile;

  private static class FakeGameEngine extends GameEngine {
    boolean previewCalled = false;
    boolean clearCalled = false;
    MatchManager lastPreviewed = null;

    FakeGameEngine() {
      super(null, new AgonRegister<>());
    }

    @Override
    public void previewMatch(MatchManager matchManager) {
      previewCalled = true;
      lastPreviewed = matchManager;
    }

    @Override
    public void clearBoardPreview() {
      clearCalled = true;
    }
  }

  @BeforeEach
  void setUp() {
    profile = new LocalProfile("Alice");
    context = new AppContext(profile);
  }

  @Test
  @DisplayName("Constructeur et getters par défaut")
  void constructorDefaults() {
    assertSame(profile, context.getProfile());
    assertNotNull(context.getClient());
    assertNull(context.getServer());
    assertNull(context.getDiscovery());

    assertEquals(AppMode.LOCAL, context.getMode());
    assertFalse(context.isOnlineGameActive());
    assertEquals(-1, context.getCurrentOnlineGameId());
    assertNull(context.getLocalOnlineColor());
    assertFalse(context.isMyOnlineTurn());
    assertNull(context.getCurrentOnlineMatch());
  }

  @Test
  @DisplayName("setServer / getServer")
  void serverGetterSetter() {
    AgonServer server = new AgonServer("TestServer");
    context.setServer(server);
    assertSame(server, context.getServer());
  }

  @Test
  @DisplayName("setMode / getMode")
  void modeGetterSetter() {
    context.setMode(AppMode.ONLINE);
    assertEquals(AppMode.ONLINE, context.getMode());
  }

  @Test
  @DisplayName("isConnected ne plante pas")
  void isConnected() {
    assertDoesNotThrow(() -> context.isConnected());
  }

  @Test
  @DisplayName("setGameEngine ne plante pas")
  void setGameEngine() {
    FakeGameEngine engine = new FakeGameEngine();
    assertDoesNotThrow(() -> context.setGameEngine(engine));
  }

  @Test
  @DisplayName("setMyOnlineTurn met à jour la valeur")
  void setMyOnlineTurn() {
    context.setMyOnlineTurn(true);
    assertTrue(context.isMyOnlineTurn());

    context.setMyOnlineTurn(false);
    assertFalse(context.isMyOnlineTurn());
  }

  @Test
  @DisplayName("onOnlineGameStarted initialise correctement une partie online")
  void onOnlineGameStarted() {
    FakeGameEngine engine = new FakeGameEngine();
    context.setGameEngine(engine);

    OnlineGameInfo info = new OnlineGameInfo(42, Color.WHITE, "Alice", "Bob", true, false);

    context.onOnlineGameStarted(info);

    assertTrue(context.isOnlineGameActive());
    assertEquals(42, context.getCurrentOnlineGameId());
    assertEquals(Color.WHITE, context.getLocalOnlineColor());
    assertTrue(context.isMyOnlineTurn());
    assertNotNull(context.getCurrentOnlineMatch());

    assertTrue(engine.previewCalled);
    assertSame(context.getCurrentOnlineMatch(), engine.lastPreviewed);
  }

  @Test
  @DisplayName("onLocalMoveConfirmed ne fait rien sans match online")
  void onLocalMoveConfirmedWithoutMatch() {
    assertDoesNotThrow(() -> context.onLocalMoveConfirmed("a1a2"));
    assertNull(context.getCurrentOnlineMatch());
    assertFalse(context.isOnlineGameActive());
  }

  @Test
  @DisplayName("onOpponentMoveReceived ne fait rien sans match online")
  void onOpponentMoveReceivedWithoutMatch() {
    assertDoesNotThrow(() -> context.onOpponentMoveReceived("a1a2"));
    assertNull(context.getCurrentOnlineMatch());
    assertFalse(context.isOnlineGameActive());
  }

  @Test
  @DisplayName("onLocalMoveConfirmed ignore un coup invalide")
  void onLocalMoveConfirmedInvalidMove() {
    FakeGameEngine engine = new FakeGameEngine();
    context.setGameEngine(engine);

    OnlineGameInfo info = new OnlineGameInfo(1, Color.WHITE, "Alice", "Bob", true, false);
    context.onOnlineGameStarted(info);

    Match beforeMatch = context.getCurrentOnlineMatch();
    boolean beforeTurn = context.isMyOnlineTurn();
    engine.previewCalled = false;

    context.onLocalMoveConfirmed("move_invalide");

    assertSame(beforeMatch, context.getCurrentOnlineMatch());
    assertEquals(beforeTurn, context.isMyOnlineTurn());
    assertFalse(engine.previewCalled);
  }

  @Test
  @DisplayName("onOpponentMoveReceived ignore un coup invalide")
  void onOpponentMoveReceivedInvalidMove() {
    FakeGameEngine engine = new FakeGameEngine();
    context.setGameEngine(engine);

    OnlineGameInfo info = new OnlineGameInfo(1, Color.WHITE, "Alice", "Bob", false, false);
    context.onOnlineGameStarted(info);

    Match beforeMatch = context.getCurrentOnlineMatch();
    boolean beforeTurn = context.isMyOnlineTurn();
    engine.previewCalled = false;

    context.onOpponentMoveReceived("move_invalide");

    assertSame(beforeMatch, context.getCurrentOnlineMatch());
    assertEquals(beforeTurn, context.isMyOnlineTurn());
    assertFalse(engine.previewCalled);
  }

  @Test
  @DisplayName("leaveOnlineGame réinitialise l'état online")
  void leaveOnlineGame() {
    FakeGameEngine engine = new FakeGameEngine();
    context.setGameEngine(engine);

    OnlineGameInfo info = new OnlineGameInfo(7, Color.BLACK, "Alice", "Bob", false, false);
    context.onOnlineGameStarted(info);

    Match current = context.getCurrentOnlineMatch();
    assertNotNull(current);

    context.leaveOnlineGame();

    assertFalse(context.isOnlineGameActive());
    assertEquals(-1, context.getCurrentOnlineGameId());
    assertFalse(context.isMyOnlineTurn());
    assertNull(context.getCurrentOnlineMatch());
    assertTrue(current.isMatchOver());
    assertTrue(engine.clearCalled);
  }

  @Test
  @DisplayName("onGameOver appelle leaveOnlineGame")
  void onGameOver() {
    FakeGameEngine engine = new FakeGameEngine();
    context.setGameEngine(engine);

    OnlineGameInfo info = new OnlineGameInfo(9, Color.WHITE, "Alice", "Bob", true, false);
    context.onOnlineGameStarted(info);

    context.onGameOver("GAME_OVER");

    assertFalse(context.isOnlineGameActive());
    assertEquals(-1, context.getCurrentOnlineGameId());
    assertNull(context.getCurrentOnlineMatch());
    assertTrue(engine.clearCalled);
  }

  @Test
  @DisplayName("onOnlineBoardRefreshRequested refresh si possible")
  void onOnlineBoardRefreshRequested() {
    FakeGameEngine engine = new FakeGameEngine();
    context.setGameEngine(engine);

    OnlineGameInfo info = new OnlineGameInfo(11, Color.WHITE, "Alice", "Bob", true, false);
    context.onOnlineGameStarted(info);

    engine.previewCalled = false;
    context.onOnlineBoardRefreshRequested();

    assertTrue(engine.previewCalled);
    assertSame(context.getCurrentOnlineMatch(), engine.lastPreviewed);
  }

  @Test
  @DisplayName("onOnlineBoardRefreshRequested ne fait rien sans engine ou sans match")
  void onOnlineBoardRefreshRequestedNoop() {
    assertDoesNotThrow(() -> context.onOnlineBoardRefreshRequested());

    FakeGameEngine engine = new FakeGameEngine();
    context.setGameEngine(engine);

    assertDoesNotThrow(() -> context.onOnlineBoardRefreshRequested());
    assertFalse(engine.previewCalled);
  }

  @Test
  @DisplayName("ensureDiscoveryStarted initialise discovery")
  void ensureDiscoveryStartedTest() throws Exception {
    assertNull(context.getDiscovery());

    context.ensureDiscoveryStarted();

    assertNotNull(context.getDiscovery());
  }

  @Test
  @DisplayName("onLocalMoveConfirmed applique un coup valide")
  void onLocalMoveConfirmedValid() {
    FakeGameEngine engine = new FakeGameEngine();
    context.setGameEngine(engine);

    OnlineGameInfo info = new OnlineGameInfo(1, Color.WHITE, "Alice", "Bob", true, false);
    context.onOnlineGameStarted(info);

    engine.previewCalled = false;

    context.onLocalMoveConfirmed("k10j10");

    assertFalse(context.isMyOnlineTurn());
    assertTrue(engine.previewCalled);
  }

  @Test
  @DisplayName("onOpponentMoveReceived applique un coup valide")
  void onOpponentMoveReceivedValid() {
    FakeGameEngine engine = new FakeGameEngine();
    context.setGameEngine(engine);

    // Si le local est BLACK, alors l'adversaire est WHITE.
    OnlineGameInfo info = new OnlineGameInfo(1, Color.BLACK, "Alice", "Bob", false, false);
    context.onOnlineGameStarted(info);

    engine.previewCalled = false;

    // Coup blanc valide vu dans tes autres tests
    context.onOpponentMoveReceived("k10j10");

    assertTrue(context.isMyOnlineTurn());
    assertTrue(engine.previewCalled);
  }

  @Test
  @DisplayName("onLocalMoveConfirmed parsing null")
  void onLocalMoveConfirmedParseFail() {
    OnlineGameInfo info = new OnlineGameInfo(1, Color.WHITE, "Alice", "Bob", true, false);
    context.onOnlineGameStarted(info);

    context.onLocalMoveConfirmed(""); // parse => null

    assertTrue(context.isOnlineGameActive());
  }

  @Test
  @DisplayName("ensureDiscoveryStarted does not recreate discovery when already initialized")
  void ensureDiscoveryStartedAlreadyInitialized() throws Exception {
    assertNull(context.getDiscovery());

    context.ensureDiscoveryStarted();
    Object firstDiscovery = context.getDiscovery();

    assertNotNull(firstDiscovery);

    context.ensureDiscoveryStarted();
    Object secondDiscovery = context.getDiscovery();

    assertSame(firstDiscovery, secondDiscovery);
  }

  @Test
  @DisplayName("onLocalMoveConfirmed valid move works without game engine")
  void onLocalMoveConfirmedValidWithoutEngine() {
    OnlineGameInfo info = new OnlineGameInfo(1, Color.WHITE, "Alice", "Bob", true, false);
    context.onOnlineGameStarted(info);

    context.setGameEngine(null);

    context.onLocalMoveConfirmed("k10j10");

    assertTrue(context.isOnlineGameActive());
    assertFalse(context.isMyOnlineTurn());
    assertNotNull(context.getCurrentOnlineMatch());
  }

  @Test
  @DisplayName("onLocalMoveConfirmed relocation move reaches no-source branch and fails cleanly")
  void onLocalMoveConfirmedRelocationBranchFailure() {
    FakeGameEngine engine = new FakeGameEngine();
    context.setGameEngine(engine);

    OnlineGameInfo info = new OnlineGameInfo(1, Color.WHITE, "Alice", "Bob", true, false);
    context.onOnlineGameStarted(info);

    Match beforeMatch = context.getCurrentOnlineMatch();
    boolean beforeTurn = context.isMyOnlineTurn();
    engine.previewCalled = false;

    context.onLocalMoveConfirmed("a1");

    assertSame(beforeMatch, context.getCurrentOnlineMatch());
    assertEquals(beforeTurn, context.isMyOnlineTurn());
    assertFalse(engine.previewCalled);
  }

  @Test
  @DisplayName("onOpponentMoveReceived valid move works without game engine")
  void onOpponentMoveReceivedValidWithoutEngine() {
    OnlineGameInfo info = new OnlineGameInfo(1, Color.BLACK, "Alice", "Bob", false, false);
    context.onOnlineGameStarted(info);

    context.setGameEngine(null);

    context.onOpponentMoveReceived("k10j10");

    assertTrue(context.isOnlineGameActive());
    assertTrue(context.isMyOnlineTurn());
    assertNotNull(context.getCurrentOnlineMatch());
  }

  @Test
  @DisplayName("onOpponentMoveReceived black branch fails cleanly when move cannot be applied")
  void onOpponentMoveReceivedBlackBranchFailure() {
    FakeGameEngine engine = new FakeGameEngine();
    context.setGameEngine(engine);

    OnlineGameInfo info = new OnlineGameInfo(1, Color.WHITE, "Alice", "Bob", false, false);
    context.onOnlineGameStarted(info);

    Match beforeMatch = context.getCurrentOnlineMatch();
    engine.previewCalled = false;

    context.onOpponentMoveReceived("k10j10");

    assertSame(beforeMatch, context.getCurrentOnlineMatch());
  }

  @Test
  @DisplayName("onOpponentMoveReceived relocation move reaches no-source branch and fails cleanly")
  void onOpponentMoveReceivedRelocationBranchFailure() {
    FakeGameEngine engine = new FakeGameEngine();
    context.setGameEngine(engine);

    OnlineGameInfo info = new OnlineGameInfo(1, Color.BLACK, "Alice", "Bob", false, false);
    context.onOnlineGameStarted(info);

    Match beforeMatch = context.getCurrentOnlineMatch();
    boolean beforeTurn = context.isMyOnlineTurn();
    engine.previewCalled = false;

    context.onOpponentMoveReceived("a1");

    assertSame(beforeMatch, context.getCurrentOnlineMatch());
    assertEquals(beforeTurn, context.isMyOnlineTurn());
    assertFalse(engine.previewCalled);
  }

  @Test
  @DisplayName("leaveOnlineGame works safely without match and without engine")
  void leaveOnlineGameWithoutMatchAndWithoutEngine() {
    context.setMyOnlineTurn(true);
    context.setGameEngine(null);

    assertDoesNotThrow(() -> context.leaveOnlineGame());

    assertFalse(context.isOnlineGameActive());
    assertEquals(-1, context.getCurrentOnlineGameId());
    assertFalse(context.isMyOnlineTurn());
    assertNull(context.getCurrentOnlineMatch());
  }

  @Test
  @DisplayName("OnlineGameInfo exposes blitz mode flag")
  void onlineGameInfoBlitzFlag() {
    OnlineGameInfo normal = new OnlineGameInfo(1, Color.WHITE, "Alice", "Bob", true, false);
    OnlineGameInfo blitz = new OnlineGameInfo(2, Color.BLACK, "Alice", "Bob", false, true);

    assertFalse(normal.isBlitzMode());
    assertTrue(blitz.isBlitzMode());
  }
}
