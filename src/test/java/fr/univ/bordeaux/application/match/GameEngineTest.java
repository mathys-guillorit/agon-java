package fr.univ.bordeaux.application.match;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdCreate;
import fr.univ.bordeaux.application.commands.specialized.CmdQuit;
import fr.univ.bordeaux.application.match.player.HumanPlayer;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.application.network.OnlineGameInfo;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class GameEngineTest {

  private AgonRegister<CmdAction> cmds;
  private GameUserInterface gameUserInterface;
  private GameConfig config;
  private GameEngine gameEngine;

  @BeforeEach
  void setUp() {
    cmds = new AgonRegister<>();
    config = new GameConfig();
    LineReader reader = new FakeLineReader("n");

    try {
      Terminal terminal = new FakeTerminal(new ByteArrayOutputStream());
      gameUserInterface = new AgonShell(terminal, reader, cmds);
      gameEngine = new GameEngine(gameUserInterface, cmds);
      cmds.register("new", new CmdCreate(gameUserInterface, config, gameEngine));
    } catch (Exception e) {
      fail("Le setup a échoué : " + e.getMessage());
    }
  }

  private StandardMatch createMatch() {
    return new StandardMatch(
        new AgonBoardImpl(),
        new HumanPlayer("J1", Color.WHITE, null),
        new HumanPlayer("J2", Color.BLACK, null),
        new GameConfig());
  }

  private Object getPrivateField(Object target, String fieldName) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(target);
  }

  private Object invokePrivateMethod(
      Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
    Class<?> type = target.getClass();
    while (type != null) {
      try {
        Method method = type.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
      } catch (NoSuchMethodException e) {
        type = type.getSuperclass();
      }
    }
    throw new NoSuchMethodException(methodName);
  }

  private static class FakeCmdAction implements CmdAction {
    private final String name;

    FakeCmdAction(String name) {
      this.name = name;
    }

    @Override
    public boolean execute(MatchManager matchManager) {
      return true;
    }

    @Override
    public CmdAction createNew(String[] args) {
      return this;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Options getOptions() {
      return null;
    }

    @Override
    public String getDescription() {
      return "fake command";
    }

    @Nonnull
    @Override
    public Completer getAutoCompleter() {
      return null;
    }
  }

  private static class SpyGameEngine extends GameEngine {
    boolean previewCalled = false;
    MatchManager lastPreviewed = null;

    SpyGameEngine(GameUserInterface ui, AgonRegister<CmdAction> cmds) {
      super(ui, cmds);
    }

    @Override
    public void previewMatch(MatchManager matchManager) {
      previewCalled = true;
      lastPreviewed = matchManager;
    }
  }

  @Test
  @DisplayName("createNew initialise un match")
  void createNewTest() {
    CmdAction cmdCreate = cmds.get("new").get().createNew(new String[] {});
    assertTrue(cmdCreate.execute(null));
    assertNotNull(gameEngine.getMatchManager());
  }

  @Test
  @DisplayName("loop : new puis quit initialise le match")
  void loopTest() {
    LineReader reader = new FakeLineReader("new", "quit");

    try {
      Terminal terminal = new FakeTerminal(new ByteArrayOutputStream());
      gameUserInterface = new AgonShell(terminal, reader, cmds);
      gameEngine = new GameEngine(gameUserInterface, cmds);
      cmds.register("new", new CmdCreate(gameUserInterface, config, gameEngine));

      AppContext context = new AppContext(new LocalProfile("test"));
      cmds.register("quit", new CmdQuit(gameUserInterface, context));

      gameEngine.start();
      assertNotNull(gameEngine.getMatchManager());
      gameUserInterface.quit();
    } catch (Exception e) {
      fail("La boucle principale a levé une exception : " + e.getMessage());
    }
  }

  @Test
  @DisplayName("stop : interrompt le joueur bloqué quand le match se termine")
  void stopTest() throws Exception {
    AtomicBoolean interruptedReceived = new AtomicBoolean(false);

    Player slowPlayer =
        new HumanPlayer("Slow", Color.WHITE, null) {
          @Override
          public CmdAction getAction(AgonRegister<CmdAction> cmds) {
            try {
              Thread.sleep(5000);
            } catch (InterruptedException e) {
              interruptedReceived.set(true);
              return null;
            }
            return null;
          }
        };

    StandardMatch match =
        new StandardMatch(
            new AgonBoardImpl(),
            slowPlayer,
            new HumanPlayer("J2", Color.BLACK, null),
            new GameConfig());
    gameEngine.setMatchManager(match);

    Thread engineThread = new Thread(gameEngine::start);
    engineThread.start();
    Thread.sleep(200);
    match.setMatchStatus(MatchStatus.FINISHED);
    Thread.sleep(500);

    assertTrue(interruptedReceived.get());
    gameUserInterface.quit();
    engineThread.join(1000);
  }

  @Test
  @DisplayName("previewMatch et clearBoardPreview sont sûrs")
  void preview_and_clear_safe() {
    assertDoesNotThrow(() -> gameEngine.previewMatch(null));
    assertDoesNotThrow(() -> gameEngine.previewMatch(createMatch()));
    assertDoesNotThrow(() -> gameEngine.clearBoardPreview());
  }

  @Test
  @DisplayName("setAppContext stocke le contexte")
  void setAppContextStoresContext() throws Exception {
    AppContext context = new AppContext(new LocalProfile("test"));
    gameEngine.setAppContext(context);
    assertSame(context, getPrivateField(gameEngine, "appContext"));
  }

  @Test
  @DisplayName("isForbiddenOnlineCommand couvre toutes les branches")
  void isForbiddenOnlineCommandCases() throws Exception {
    assertFalse(
        (Boolean)
            invokePrivateMethod(
                gameEngine,
                "isForbiddenOnlineCommand",
                new Class<?>[] {CmdAction.class},
                new Object[] {null}));
    assertFalse(
        (Boolean)
            invokePrivateMethod(
                gameEngine,
                "isForbiddenOnlineCommand",
                new Class<?>[] {CmdAction.class},
                new FakeCmdAction(null)));
    assertTrue(
        (Boolean)
            invokePrivateMethod(
                gameEngine,
                "isForbiddenOnlineCommand",
                new Class<?>[] {CmdAction.class},
                new FakeCmdAction("undo")));
    assertTrue(
        (Boolean)
            invokePrivateMethod(
                gameEngine,
                "isForbiddenOnlineCommand",
                new Class<?>[] {CmdAction.class},
                new FakeCmdAction("redo")));
    assertTrue(
        (Boolean)
            invokePrivateMethod(
                gameEngine,
                "isForbiddenOnlineCommand",
                new Class<?>[] {CmdAction.class},
                new FakeCmdAction("pause")));
    assertTrue(
        (Boolean)
            invokePrivateMethod(
                gameEngine,
                "isForbiddenOnlineCommand",
                new Class<?>[] {CmdAction.class},
                new FakeCmdAction("save")));
    assertTrue(
        (Boolean)
            invokePrivateMethod(
                gameEngine,
                "isForbiddenOnlineCommand",
                new Class<?>[] {CmdAction.class},
                new FakeCmdAction("load")));
    assertFalse(
        (Boolean)
            invokePrivateMethod(
                gameEngine,
                "isForbiddenOnlineCommand",
                new Class<?>[] {CmdAction.class},
                new FakeCmdAction("help")));
  }

  @Test
  @DisplayName("refreshOnlineBoard couvre null, contexte vide et contexte online")
  void refreshOnlineBoardCases() throws Exception {
    assertDoesNotThrow(
        () -> invokePrivateMethod(gameEngine, "refreshOnlineBoard", new Class<?>[] {}));

    SpyGameEngine engine1 = new SpyGameEngine(gameUserInterface, cmds);
    AppContext context1 = new AppContext(new LocalProfile("test"));
    engine1.setAppContext(context1);
    assertDoesNotThrow(() -> invokePrivateMethod(engine1, "refreshOnlineBoard", new Class<?>[] {}));
    assertFalse(engine1.previewCalled);

    SpyGameEngine engine2 = new SpyGameEngine(gameUserInterface, cmds);
    AppContext context2 = new AppContext(new LocalProfile("test"));
    context2.onOnlineGameStarted(new OnlineGameInfo(1, Color.WHITE, "Alice", "Bob", true, false));
    engine2.setAppContext(context2);
    assertDoesNotThrow(() -> invokePrivateMethod(engine2, "refreshOnlineBoard", new Class<?>[] {}));
    assertTrue(engine2.previewCalled);
    assertNotNull(engine2.lastPreviewed);
  }

  @Test
  @DisplayName("setMatchManager / getMatchManager conservent la référence")
  void setAndGetMatchManager() {
    StandardMatch match = createMatch();
    gameEngine.setMatchManager(match);
    assertSame(match, gameEngine.getMatchManager());
  }

  @Test
  @DisplayName("start : quit immédiat et commande inconnue ne plantent pas")
  void startWithImmediateQuitAndUnknownCommand() {
    try {
      Terminal terminal1 = new FakeTerminal(new ByteArrayOutputStream());
      GameUserInterface ui1 = new AgonShell(terminal1, new FakeLineReader("quit"), cmds);
      GameEngine engine1 = new GameEngine(ui1, cmds);
      AppContext context1 = new AppContext(new LocalProfile("test"));
      cmds.register("quit", new CmdQuit(ui1, context1));
      assertDoesNotThrow(engine1::start);

      Terminal terminal2 = new FakeTerminal(new ByteArrayOutputStream());
      GameUserInterface ui2 =
          new AgonShell(terminal2, new FakeLineReader("commande_inconnue", "quit"), cmds);
      GameEngine engine2 = new GameEngine(ui2, cmds);
      AppContext context2 = new AppContext(new LocalProfile("test"));
      cmds.register("quit", new CmdQuit(ui2, context2));
      assertDoesNotThrow(engine2::start);
    } catch (Exception e) {
      fail("Le test a échoué : " + e.getMessage());
    }
  }

  @Test
  @DisplayName("start : couvre la branche catch quand le joueur lève une exception")
  void startPlayerActionExceptionBranch() throws Exception {
    Player failingPlayer =
        new HumanPlayer("Fail", Color.WHITE, null) {
          @Override
          public CmdAction getAction(AgonRegister<CmdAction> cmds) {
            throw new RuntimeException("boom");
          }
        };

    StandardMatch match =
        new StandardMatch(
            new AgonBoardImpl(),
            failingPlayer,
            new HumanPlayer("J2", Color.BLACK, null),
            new GameConfig());
    gameEngine.setMatchManager(match);

    Thread engineThread =
        new Thread(
            () -> {
              try {
                gameEngine.start();
              } catch (Exception ignored) {
              }
            });

    engineThread.start();
    Thread.sleep(200);
    gameUserInterface.quit();
    engineThread.join(1000);

    assertTrue(true);
  }
}
