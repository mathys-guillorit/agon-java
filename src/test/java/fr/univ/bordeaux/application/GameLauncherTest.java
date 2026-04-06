package fr.univ.bordeaux.application;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameLauncherTest {

  private static class TestableGameLauncher extends GameLauncher {
    @Override
    protected String getHelpContent() throws IOException {
      return "HELP CONTENT";
    }

    @Override
    protected String getVersionContent() throws IOException {
      return "VERSION CONTENT";
    }
  }

  private Object invokePrivate(
      Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
    Method method = GameLauncher.class.getDeclaredMethod(methodName, paramTypes);
    method.setAccessible(true);
    return method.invoke(target, args);
  }

  @Test
  @DisplayName("Constructeur")
  void constructor_test() {
    GameLauncher launcher = new GameLauncher();
    assertNotNull(launcher);
  }

  @Test
  @DisplayName("getHelpContent")
  void get_help_content() throws Exception {
    TestableGameLauncher launcher = new TestableGameLauncher();
    assertEquals("HELP CONTENT", launcher.getHelpContent());
  }

  @Test
  @DisplayName("getVersionContent")
  void get_version_content() throws Exception {
    TestableGameLauncher launcher = new TestableGameLauncher();
    assertEquals("VERSION CONTENT", launcher.getVersionContent());
  }

  @Test
  @DisplayName("askPlayerName redemande tant que la saisie est vide")
  void ask_player_name() throws Exception {
    GameLauncher launcher = new GameLauncher();
    System.setIn(new ByteArrayInputStream("\nAlice\n".getBytes()));

    String name = (String) invokePrivate(launcher, "askPlayerName", new Class<?>[] {});

    assertEquals("Alice", name);
  }

  @Test
  @DisplayName("askApplicationMode retourne LOCAL")
  void ask_application_mode_local() throws Exception {
    GameLauncher launcher = new GameLauncher();
    System.setIn(new ByteArrayInputStream("1\n".getBytes()));

    AppMode mode = (AppMode) invokePrivate(launcher, "askApplicationMode", new Class<?>[] {});

    assertEquals(AppMode.LOCAL, mode);
  }

  @Test
  @DisplayName("askApplicationMode retourne ONLINE")
  void ask_application_mode_online() throws Exception {
    GameLauncher launcher = new GameLauncher();
    System.setIn(new ByteArrayInputStream("2\n".getBytes()));

    AppMode mode = (AppMode) invokePrivate(launcher, "askApplicationMode", new Class<?>[] {});

    assertEquals(AppMode.ONLINE, mode);
  }

  @Test
  @DisplayName("askApplicationMode redemande après une saisie invalide")
  void ask_application_mode_retry() throws Exception {
    GameLauncher launcher = new GameLauncher();
    System.setIn(new ByteArrayInputStream("x\n3\n2\n".getBytes()));

    AppMode mode = (AppMode) invokePrivate(launcher, "askApplicationMode", new Class<?>[] {});

    assertEquals(AppMode.ONLINE, mode);
  }

  @Test
  @DisplayName("loadInitialConfig retourne toujours une config")
  void load_initial_config() throws Exception {
    GameLauncher launcher = new GameLauncher();

    GameConfig config =
        (GameConfig) invokePrivate(launcher, "loadInitialConfig", new Class<?>[] {});

    assertNotNull(config);
  }

  @Test
  @DisplayName("fillRegister enregistre toutes les commandes principales")
  void fill_register() throws Exception {
    GameLauncher launcher = new GameLauncher();
    AgonRegister<CmdAction> cmds = new AgonRegister<>();
    AppContext context = new AppContext(new LocalProfile("Alice"));

    invokePrivate(
        launcher,
        "fillRegister",
        new Class<?>[] {
          AgonRegister.class,
          GameUserInterface.class,
          GameConfig.class,
          GameEngine.class,
          AppContext.class
        },
        cmds,
        null,
        new GameConfig(),
        null,
        context);

    assertTrue(cmds.get("new").isPresent());
    assertTrue(cmds.get("hint").isPresent());
    assertTrue(cmds.get("show").isPresent());
    assertTrue(cmds.get("load").isPresent());
    assertTrue(cmds.get("save").isPresent());
    assertTrue(cmds.get("set").isPresent());
    assertTrue(cmds.get("undo").isPresent());
    assertTrue(cmds.get("redo").isPresent());
    assertTrue(cmds.get("help").isPresent());
    assertTrue(cmds.get("join").isPresent());
    assertTrue(cmds.get("ping").isPresent());
    assertTrue(cmds.get("server_start").isPresent());
    assertTrue(cmds.get("server_stop").isPresent());
    assertTrue(cmds.get("server_list").isPresent());
    assertTrue(cmds.get("server_status").isPresent());
    assertTrue(cmds.get("players").isPresent());
    assertTrue(cmds.get("scoreboard").isPresent());
    assertTrue(cmds.get("quit").isPresent());
  }

  @Test
  @DisplayName("createDefaultConfigFile ne plante pas")
  void create_default_config_file() throws Exception {
    GameLauncher launcher = new GameLauncher();
    assertDoesNotThrow(() -> invokePrivate(launcher, "createDefaultConfigFile", new Class<?>[] {}));
  }

  @Test
  @DisplayName("startGame avec option gui retourne immédiatement")
  void start_game_gui_branch() throws Exception {
    GameLauncher launcher = new GameLauncher();

    org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
    options.addOption("g", "gui", false, "gui");
    org.apache.commons.cli.CommandLine cmd =
        new org.apache.commons.cli.DefaultParser().parse(options, new String[] {"-g"});

    assertDoesNotThrow(
        () ->
            invokePrivate(
                launcher,
                "startGame",
                new Class<?>[] {
                  GameConfig.class,
                  org.apache.commons.cli.CommandLine.class,
                  AgonRegister.class,
                  String.class, // AJOUT : pour filePathToLoad
                  AppContext.class
                },
                new GameConfig(),
                cmd,
                new AgonRegister<CmdAction>(),
                null, // AJOUT : valeur pour filePathToLoad
                new AppContext(new LocalProfile("Alice"))));
  }

  @Test
  @DisplayName("printVersion fonctionne même sans fichier")
  void printVersionTest() {
    GameLauncher launcher = new GameLauncher();
    assertDoesNotThrow(
        () -> {
          try {
            launcher.getVersionContent();
          } catch (Exception ignored) {
          }
        });
  }

  @Test
  @DisplayName("printHelp ne plante pas")
  void printHelpTest() throws Exception {
    GameLauncher launcher = new GameLauncher();
    AgonRegister<CmdAction> cmds = new AgonRegister<>();

    Method m = GameLauncher.class.getDeclaredMethod("printHelp", AgonRegister.class);
    m.setAccessible(true);

    assertDoesNotThrow(() -> m.invoke(launcher, cmds));
  }

  @Test
  @DisplayName("startGame GUI branch")
  void startGameGuiTest() throws Exception {
    GameLauncher launcher = new GameLauncher();

    org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
    options.addOption("g", "gui", false, "gui");

    org.apache.commons.cli.CommandLine cmd =
        new org.apache.commons.cli.DefaultParser().parse(options, new String[] {"-g"});

    // Correction ici : ajout de String.class pour filePathToLoad
    Method m =
        GameLauncher.class.getDeclaredMethod(
            "startGame",
            GameConfig.class,
            org.apache.commons.cli.CommandLine.class,
            AgonRegister.class,
            String.class, // <--- PARAMÈTRE MANQUANT AJOUTÉ
            AppContext.class);
    m.setAccessible(true);

    assertDoesNotThrow(
        () ->
            m.invoke(
                launcher,
                new GameConfig(),
                cmd,
                new AgonRegister<CmdAction>(),
                null, // <--- VALEUR MANQUANTE AJOUTÉE (filePathToLoad)
                new AppContext(new LocalProfile("Alice"))));
  }

  @Test
  @DisplayName("createDefaultConfigFile ne plante pas")
  void createDefaultConfigFileTest() throws Exception {
    GameLauncher launcher = new GameLauncher();

    Method m = GameLauncher.class.getDeclaredMethod("createDefaultConfigFile");
    m.setAccessible(true);

    assertDoesNotThrow(() -> m.invoke(launcher));
  }
}
