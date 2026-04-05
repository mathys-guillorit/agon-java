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
    Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
    method.setAccessible(true);
    return method.invoke(target, args);
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

  @Test
  @DisplayName("Vérifier que createNew génère une action non nulle")
  void createNewTest() {
    CmdAction cmdCreate = cmds.get("new").get().createNew(new String[] {});
    assertTrue(cmdCreate.execute(null));
    assertNotNull(gameEngine.getMatchManager());
  }

  @Test
  @DisplayName("Test de la boucle principale : Menu -> Create -> Match")
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

      assertNotNull(
          gameEngine.getMatchManager(),
          "La boucle aurait dû exécuter 'create' et initialiser le match");
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

    assertTrue(
        interruptedReceived.get(),
        "Le thread du joueur aurait dû être interrompu par futureAction.cancel(true)");

    gameUserInterface.quit();
    engineThread.join(1000);
  }

  @Test
  @DisplayName("previewMatch : ne doit rien faire si le matchManager est null")
  void previewMatchWithNullDoesNothing() {
    assertDoesNotThrow(() -> gameEngine.previewMatch(null));
  }

  @Test
  @DisplayName("previewMatch : fonctionne avec un match valide")
  void previewMatchWithValidMatch() {
    StandardMatch match = createMatch();
    assertDoesNotThrow(() -> gameEngine.previewMatch(match));
  }

  @Test
  @DisplayName("setAppContext : enregistre correctement le contexte")
  void setAppContextStoresContext() throws Exception {
    AppContext context = new AppContext(new LocalProfile("test"));
    gameEngine.setAppContext(context);

    Object stored = getPrivateField(gameEngine, "appContext");
    assertSame(context, stored);
  }

  @Test
  @DisplayName("clearBoardPreview : ne lève pas d'exception")
  void clearBoardPreviewTest() {
    assertDoesNotThrow(() -> gameEngine.clearBoardPreview());
  }

  @Test
  @DisplayName("isForbiddenOnlineCommand : retourne false si action est null")
  void isForbiddenOnlineCommandNull() throws Exception {
    Boolean result =
        (Boolean)
            invokePrivateMethod(
                gameEngine,
                "isForbiddenOnlineCommand",
                new Class<?>[] {CmdAction.class},
                new Object[] {null});

    assertFalse(result);
  }

  @Test
  @DisplayName("isForbiddenOnlineCommand : retourne false si le nom est null")
  void isForbiddenOnlineCommandNameNull() throws Exception {
    CmdAction action = new FakeCmdAction(null);

    Boolean result =
        (Boolean)
            invokePrivateMethod(
                gameEngine, "isForbiddenOnlineCommand", new Class<?>[] {CmdAction.class}, action);

    assertFalse(result);
  }

  @Test
  @DisplayName("isForbiddenOnlineCommand : undo doit être interdit")
  void isForbiddenOnlineCommandUndo() throws Exception {
    CmdAction action = new FakeCmdAction("undo");

    Boolean result =
        (Boolean)
            invokePrivateMethod(
                gameEngine, "isForbiddenOnlineCommand", new Class<?>[] {CmdAction.class}, action);

    assertTrue(result);
  }

  @Test
  @DisplayName("isForbiddenOnlineCommand : redo doit être interdit")
  void isForbiddenOnlineCommandRedo() throws Exception {
    CmdAction action = new FakeCmdAction("redo");

    Boolean result =
        (Boolean)
            invokePrivateMethod(
                gameEngine, "isForbiddenOnlineCommand", new Class<?>[] {CmdAction.class}, action);

    assertTrue(result);
  }

  @Test
  @DisplayName("isForbiddenOnlineCommand : pause doit être interdit")
  void isForbiddenOnlineCommandPause() throws Exception {
    CmdAction action = new FakeCmdAction("pause");

    Boolean result =
        (Boolean)
            invokePrivateMethod(
                gameEngine, "isForbiddenOnlineCommand", new Class<?>[] {CmdAction.class}, action);

    assertTrue(result);
  }

  @Test
  @DisplayName("isForbiddenOnlineCommand : save doit être interdit")
  void isForbiddenOnlineCommandSave() throws Exception {
    CmdAction action = new FakeCmdAction("save");

    Boolean result =
        (Boolean)
            invokePrivateMethod(
                gameEngine, "isForbiddenOnlineCommand", new Class<?>[] {CmdAction.class}, action);

    assertTrue(result);
  }

  @Test
  @DisplayName("isForbiddenOnlineCommand : load doit être interdit")
  void isForbiddenOnlineCommandLoad() throws Exception {
    CmdAction action = new FakeCmdAction("load");

    Boolean result =
        (Boolean)
            invokePrivateMethod(
                gameEngine, "isForbiddenOnlineCommand", new Class<?>[] {CmdAction.class}, action);

    assertTrue(result);
  }

  @Test
  @DisplayName("isForbiddenOnlineCommand : une commande normale ne doit pas être interdite")
  void isForbiddenOnlineCommandAllowed() throws Exception {
    CmdAction action = new FakeCmdAction("help");

    Boolean result =
        (Boolean)
            invokePrivateMethod(
                gameEngine, "isForbiddenOnlineCommand", new Class<?>[] {CmdAction.class}, action);

    assertFalse(result);
  }

  @Test
  @DisplayName("refreshOnlineBoard : ne fait rien si appContext est null")
  void refreshOnlineBoardWithoutContext() {
    assertDoesNotThrow(
        () -> invokePrivateMethod(gameEngine, "refreshOnlineBoard", new Class<?>[] {}));
  }

  @Test
  @DisplayName("setMatchManager / getMatchManager : conservent la référence")
  void setAndGetMatchManager() {
    StandardMatch match = createMatch();
    gameEngine.setMatchManager(match);

    assertSame(match, gameEngine.getMatchManager());
  }

  @Test
  @DisplayName("start : ne plante pas avec une UI qui s'arrête rapidement")
  void startWithImmediateQuit() {
    LineReader reader = new FakeLineReader("quit");

    try {
      Terminal terminal = new FakeTerminal(new ByteArrayOutputStream());
      gameUserInterface = new AgonShell(terminal, reader, cmds);
      gameEngine = new GameEngine(gameUserInterface, cmds);

      AppContext context = new AppContext(new LocalProfile("test"));
      cmds.register("quit", new CmdQuit(gameUserInterface, context));

      assertDoesNotThrow(() -> gameEngine.start());
    } catch (Exception e) {
      fail("Le test a échoué : " + e.getMessage());
    }
  }

  @Test
  @DisplayName("start : accepte une commande inconnue sans planter")
  void startWithUnknownCommand() {
    LineReader reader = new FakeLineReader("commande_inconnue", "quit");

    try {
      Terminal terminal = new FakeTerminal(new ByteArrayOutputStream());
      gameUserInterface = new AgonShell(terminal, reader, cmds);
      gameEngine = new GameEngine(gameUserInterface, cmds);

      AppContext context = new AppContext(new LocalProfile("test"));
      cmds.register("quit", new CmdQuit(gameUserInterface, context));

      assertDoesNotThrow(() -> gameEngine.start());
    } catch (Exception e) {
      fail("Le test a échoué : " + e.getMessage());
    }
  }
}
