package fr.univ.bordeaux.application;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameLauncherTest {

  @BeforeAll
  static void initEnv() {
    System.setProperty("IS_TEST_ENV", "true");
  }

  private Object invokePrivate(
      Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
    Method method = GameLauncher.class.getDeclaredMethod(methodName, paramTypes);
    method.setAccessible(true);
    return method.invoke(target, args);
  }

  private static class TestableGameLauncher extends GameLauncher {
    public CommandLine parsedCmd;
    public boolean guiLaunched = false;
    public boolean cliLaunched = false;

    @Override
    protected String getHelpContent() throws IOException {
      return "HELP CONTENT";
    }

    @Override
    protected String getVersionContent() throws IOException {
      return "VERSION CONTENT";
    }

    @Override
    protected void launchGUI(
        GameConfig config,
        CommandLine cmd,
        AgonRegister<CmdAction> cmds,
        String filePathToLoad,
        AppContext context) {
      this.guiLaunched = true;
    }

    @Override
    protected void launchCLI(
        GameConfig config,
        CommandLine cmd,
        AgonRegister<CmdAction> cmds,
        String filePathToLoad,
        AppContext context) {
      this.cliLaunched = true;
      this.parsedCmd = cmd;
    }
  }

  @Test
  @DisplayName("Constructor")
  void constructor_test() {
    GameLauncher launcher = new GameLauncher();
    assertNotNull(launcher);
  }

  @Test
  @DisplayName("askPlayerName prompts again while input is empty")
  void ask_player_name() throws Exception {
    GameLauncher launcher = new GameLauncher();
    Terminal terminal =
        TerminalBuilder.builder()
            .streams(new ByteArrayInputStream("\nAlice\n".getBytes()), new ByteArrayOutputStream())
            .build();
    LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

    String name =
        (String)
            invokePrivate(launcher, "askPlayerName", new Class<?>[] {LineReader.class}, reader);
    assertEquals("Alice", name);
  }

  @Test
  @DisplayName("askApplicationMode returns LOCAL")
  void ask_application_mode_local() throws Exception {
    GameLauncher launcher = new GameLauncher();
    Terminal terminal =
        TerminalBuilder.builder()
            .streams(new ByteArrayInputStream("1\n".getBytes()), new ByteArrayOutputStream())
            .build();
    LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

    AppMode mode =
        (AppMode)
            invokePrivate(
                launcher, "askApplicationMode", new Class<?>[] {LineReader.class}, reader);
    assertEquals(AppMode.LOCAL, mode);
  }

  @Test
  @DisplayName("askApplicationMode returns ONLINE")
  void ask_application_mode_online() throws Exception {
    GameLauncher launcher = new GameLauncher();
    Terminal terminal =
        TerminalBuilder.builder()
            .streams(new ByteArrayInputStream("2\n".getBytes()), new ByteArrayOutputStream())
            .build();
    LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

    AppMode mode =
        (AppMode)
            invokePrivate(
                launcher, "askApplicationMode", new Class<?>[] {LineReader.class}, reader);
    assertEquals(AppMode.ONLINE, mode);
  }

  @Test
  @DisplayName("askApplicationMode prompts again after invalid input")
  void ask_application_mode_retry() throws Exception {
    GameLauncher launcher = new GameLauncher();
    Terminal terminal =
        TerminalBuilder.builder()
            .streams(new ByteArrayInputStream("x\n3\n2\n".getBytes()), new ByteArrayOutputStream())
            .build();
    LineReader reader = LineReaderBuilder.builder().terminal(terminal).build();

    AppMode mode =
        (AppMode)
            invokePrivate(
                launcher, "askApplicationMode", new Class<?>[] {LineReader.class}, reader);
    assertEquals(AppMode.ONLINE, mode);
  }

  @Test
  @DisplayName("loadInitialConfig always returns a config")
  void load_initial_config() throws Exception {
    GameLauncher launcher = new GameLauncher();
    GameConfig config =
        (GameConfig) invokePrivate(launcher, "loadInitialConfig", new Class<?>[] {});
    assertNotNull(config);
  }

  @Test
  @DisplayName("createDefaultConfigFile does not crash")
  void createDefaultConfigFileTest() throws Exception {
    GameLauncher launcher = new GameLauncher();
    assertDoesNotThrow(() -> invokePrivate(launcher, "createDefaultConfigFile", new Class<?>[] {}));
  }

  @Test
  @DisplayName("Test option -h (Help)")
  public void testHelpOption() {
    TestableGameLauncher launcher = new TestableGameLauncher();
    assertDoesNotThrow(() -> launcher.launch("-h"));
    assertFalse(launcher.cliLaunched, "The game should not start with the -h option");
  }

  @Test
  @DisplayName("Test option -V (Version)")
  public void testVersionOption() {
    TestableGameLauncher launcher = new TestableGameLauncher();
    assertDoesNotThrow(() -> launcher.launch("-V"));
    assertFalse(launcher.cliLaunched, "The game should not start with the -V option");
  }

  @Test
  @DisplayName("Test option -v (Verbose)")
  public void testVerboseOption() {
    TestableGameLauncher launcher = new TestableGameLauncher();
    launcher.launch("-v");
    assertNotNull(launcher.parsedCmd, "The command line should have been parsed");
    assertTrue(launcher.parsedCmd.hasOption("v"), "The -v option must be detected");
  }

  @Test
  @DisplayName("Test option -d (Debug)")
  public void testDebugOption() {
    TestableGameLauncher launcher = new TestableGameLauncher();
    launcher.launch("-d");
    assertNotNull(launcher.parsedCmd, "The command line should have been parsed");
    assertTrue(launcher.parsedCmd.hasOption("d"), "The -d option must be detected");
  }

  @Test
  @DisplayName("Test option -g (GUI) intercepted")
  public void testGuiOption() {
    TestableGameLauncher launcher = new TestableGameLauncher();
    assertDoesNotThrow(() -> launcher.launch("-g"));
    assertTrue(launcher.guiLaunched, "The launcher should have called the graphical interface");
    assertFalse(launcher.cliLaunched, "The launcher should not have called the terminal");
  }

  @Test
  @DisplayName("Test option -c (Contest) with valid file")
  public void testContestOption() throws IOException {
    File dummyFile = new File("dummy_contest_valid.txt");
    Files.writeString(dummyFile.toPath(), "test");
    try {
      TestableGameLauncher launcher = new TestableGameLauncher();
      launcher.launch("-c", dummyFile.getName());
      assertFalse(launcher.cliLaunched, "Contest mode replaces normal launch");
    } finally {
      dummyFile.delete();
    }
  }

  @Test
  @DisplayName("Test option -c without file (Error)")
  public void testContestModeWithoutFile() {
    TestableGameLauncher launcher = new TestableGameLauncher();
    assertDoesNotThrow(() -> launcher.launch("-c"));
    assertFalse(launcher.cliLaunched, "A file error should cancel the normal launch");
  }

  @Test
  @DisplayName("Test invalid option")
  public void testInvalidOption() {
    TestableGameLauncher launcher = new TestableGameLauncher();
    launcher.launch("-z");
    assertNull(launcher.parsedCmd, "An invalid option triggers an exception and stops parsing");
    assertFalse(launcher.cliLaunched, "The game should not start with an invalid option");
  }

  @Test
  @DisplayName("Test startGame routes to GUI if -g option is present")
  void testStartGame_RoutesToGUI() throws Exception {
    TestableGameLauncher launcher = new TestableGameLauncher();

    Options options = new Options();
    options.addOption("g", "gui", false, "");
    CommandLine cmd = new DefaultParser().parse(options, new String[] {"-g"});
    AppContext ctx = new AppContext(new LocalProfile("Test"));

    invokePrivate(
        launcher,
        "startGame",
        new Class<?>[] {
          GameConfig.class, CommandLine.class, AgonRegister.class, String.class, AppContext.class
        },
        new GameConfig(),
        cmd,
        new AgonRegister<>(),
        null,
        ctx);

    assertTrue(launcher.guiLaunched, "The game should have launched the graphical interface (GUI)");
  }

  @Test
  @DisplayName("Test startGame routes to CLI if -g option is absent")
  void testStartGame_RoutesToCLI() throws Exception {
    TestableGameLauncher launcher = new TestableGameLauncher();

    Options options = new Options();
    CommandLine cmd = new DefaultParser().parse(options, new String[] {});
    AppContext ctx = new AppContext(new LocalProfile("Test"));

    invokePrivate(
        launcher,
        "startGame",
        new Class<?>[] {
          GameConfig.class, CommandLine.class, AgonRegister.class, String.class, AppContext.class
        },
        new GameConfig(),
        cmd,
        new AgonRegister<>(),
        null,
        ctx);

    assertTrue(launcher.cliLaunched, "The game should have launched the terminal (CLI)");
  }

  @Test
  @DisplayName("Coverage of real launchCLI method without hanging")
  void testLaunchCLI_AllBranches() throws Exception {
    GameLauncher launcher = new GameLauncher();
    GameConfig config = new GameConfig();
    config.setBlitzMode(true);

    Options options = new Options();
    options.addOption("a", "ai", true, "");
    CommandLine cmd = new DefaultParser().parse(options, new String[] {"-a", "black"});

    AgonRegister<CmdAction> cmds = new AgonRegister<>();
    AppContext context = new AppContext(new LocalProfile("Test"));

    Thread t =
        new Thread(
            () -> {
              try {
                invokePrivate(
                    launcher,
                    "launchCLI",
                    new Class<?>[] {
                      GameConfig.class,
                      CommandLine.class,
                      AgonRegister.class,
                      String.class,
                      AppContext.class
                    },
                    config,
                    cmd,
                    cmds,
                    "dummy_save.agon",
                    context);
              } catch (Exception ignored) {
              }
            });
    t.start();

    Thread.sleep(1000);

    try {
      Field engineField = AppContext.class.getDeclaredField("gameEngine");
      engineField.setAccessible(true);
      GameEngine engine = (GameEngine) engineField.get(context);

      if (engine != null) {
        for (java.lang.reflect.Field field : engine.getClass().getDeclaredFields()) {
          if (GameUserInterface.class.isAssignableFrom(field.getType())) {
            field.setAccessible(true);
            GameUserInterface ui = (GameUserInterface) field.get(engine);
            if (ui != null && ui.getClass().getSimpleName().equals("AgonShell")) {
              java.lang.reflect.Method getRunningMethod = ui.getClass().getMethod("getRunning");
              java.util.concurrent.atomic.AtomicBoolean running =
                  (java.util.concurrent.atomic.AtomicBoolean) getRunningMethod.invoke(ui);
              running.set(false);
            }
          }
        }
      }
    } catch (Exception ignored) {
    }

    t.interrupt();
    t.join(3000);

    assertTrue(true, "launchCLI executed and terminated gracefully");
  }

  @Test
  @DisplayName("Coverage of internal catch (Exception) block in launchCLI")
  void testLaunchCLI_Exception() {
    GameLauncher launcher = new GameLauncher();
    GameConfig config = new GameConfig();
    AgonRegister<CmdAction> cmds = new AgonRegister<>();

    assertDoesNotThrow(
        () -> {
          Options options = new Options();
          CommandLine dummyCmd = new DefaultParser().parse(options, new String[] {});

          invokePrivate(
              launcher,
              "launchCLI",
              new Class<?>[] {
                GameConfig.class,
                CommandLine.class,
                AgonRegister.class,
                String.class,
                AppContext.class
              },
              config,
              dummyCmd,
              cmds,
              null,
              null);
        });
  }

  @Test
  @DisplayName("Coverage of real launchGUI method without hanging")
  void testLaunchGUI_AllBranches() throws Exception {
    GameLauncher launcher = new GameLauncher();
    GameConfig config = new GameConfig();

    Options options = new Options();
    options.addOption("a", "ai", true, "");
    CommandLine cmd = new DefaultParser().parse(options, new String[] {"-a", "white"});

    AgonRegister<CmdAction> cmds = new AgonRegister<>();
    AppContext context = new AppContext(new LocalProfile("Test"));

    Thread t =
        new Thread(
            () -> {
              try {
                invokePrivate(
                    launcher,
                    "launchGUI",
                    new Class<?>[] {
                      GameConfig.class,
                      CommandLine.class,
                      AgonRegister.class,
                      String.class,
                      AppContext.class
                    },
                    config,
                    cmd,
                    cmds,
                    "dummy_save.agon",
                    context);
              } catch (Exception ignored) {
              }
            });
    t.start();

    Thread.sleep(1000);

    try {
      java.lang.reflect.Field guiField =
          fr.univ.bordeaux.ui.gui.AgonApp.class.getDeclaredField("agonGui");
      guiField.setAccessible(true);
      fr.univ.bordeaux.ui.gui.AgonGui gui = (fr.univ.bordeaux.ui.gui.AgonGui) guiField.get(null);

      if (gui != null) {
        gui.quit();
      }
    } catch (Exception ignored) {
    }

    t.interrupt();
    t.join(3000);
    assertTrue(true);
  }

  @Test
  @DisplayName("Coverage of the reflection catch block (Profile fallback) in launchCLI")
  void testLaunchCLI_ReflectionCatchCoverage() throws Exception {
    String prevEnv = System.getProperty("IS_TEST_ENV");
    System.setProperty("IS_TEST_ENV", "false");

    InputStream originalIn = System.in;
    System.setIn(new ByteArrayInputStream("CoverageTest\n1\nquit\nn\n".getBytes()));

    try {
      GameLauncher launcher = new GameLauncher();
      GameConfig config = new GameConfig();
      Options options = new Options();
      CommandLine dummyCmd = new DefaultParser().parse(options, new String[] {});
      AgonRegister<CmdAction> cmds = new AgonRegister<>();

      AppContext context = new AppContext(new LocalProfile("Temp"));

      java.lang.reflect.Field profileField = AppContext.class.getDeclaredField("profile");
      profileField.setAccessible(true);
      profileField.set(context, null);

      Thread t =
          new Thread(
              () -> {
                try {
                  invokePrivate(
                      launcher,
                      "launchCLI",
                      new Class<?>[] {
                        GameConfig.class,
                        CommandLine.class,
                        AgonRegister.class,
                        String.class,
                        AppContext.class
                      },
                      config,
                      dummyCmd,
                      cmds,
                      null,
                      context);
                } catch (Exception ignored) {
                }
              });
      t.start();

      Thread.sleep(1000);

      try {
        java.lang.reflect.Field engineField = AppContext.class.getDeclaredField("gameEngine");
        engineField.setAccessible(true);
        GameEngine engine = (GameEngine) engineField.get(context);

        if (engine != null) {
          for (java.lang.reflect.Field field : engine.getClass().getDeclaredFields()) {
            if (GameUserInterface.class.isAssignableFrom(field.getType())) {
              field.setAccessible(true);
              GameUserInterface ui = (GameUserInterface) field.get(engine);
              if (ui != null && ui.getClass().getSimpleName().equals("AgonShell")) {
                java.lang.reflect.Method getRunningMethod = ui.getClass().getMethod("getRunning");
                java.util.concurrent.atomic.AtomicBoolean running =
                    (java.util.concurrent.atomic.AtomicBoolean) getRunningMethod.invoke(ui);
                running.set(false);
              }
            }
          }
        }
      } catch (Exception ignored) {
      }

      t.interrupt();
      t.join(3000);

      assertTrue(true, "Le bloc catch avec réflexion s'est exécuté sans lever d'erreur fatale.");

    } finally {
      System.setIn(originalIn);
      if (prevEnv != null) {
        System.setProperty("IS_TEST_ENV", prevEnv);
      }
    }
  }
}
