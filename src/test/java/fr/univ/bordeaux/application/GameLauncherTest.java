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
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameLauncherTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        System.setIn(System.in);
    }

    private String getOutput() {
        return outContent.toString();
    }

    private Object invokePrivate(
            Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = GameLauncher.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static class TestableGameLauncher extends GameLauncher {
        @Override
        protected String getHelpContent() throws IOException {
            return "HELP CONTENT";
        }

        @Override
        protected String getVersionContent() throws IOException {
            return "VERSION CONTENT";
        }

        @Override
        protected String askPlayerName() {
            return "TestPlayer";
        }

        @Override
        protected AppMode askApplicationMode() {
            return AppMode.LOCAL;
        }
    }

    @Test
    @DisplayName("Constructeur")
    void constructorTest() {
        GameLauncher launcher = new GameLauncher();
        assertNotNull(launcher);
    }

    @Test
    @DisplayName("getHelpContent")
    void getHelpContent() throws Exception {
        TestableGameLauncher launcher = new TestableGameLauncher();
        assertEquals("HELP CONTENT", launcher.getHelpContent());
    }

    @Test
    @DisplayName("getVersionContent")
    void getVersionContent() throws Exception {
        TestableGameLauncher launcher = new TestableGameLauncher();
        assertEquals("VERSION CONTENT", launcher.getVersionContent());
    }

    @Test
    @DisplayName("askPlayerName redemande tant que la saisie est vide")
    void askPlayerName() throws Exception {
        GameLauncher launcher = new GameLauncher();
        System.setIn(new ByteArrayInputStream("\nAlice\n".getBytes()));
        String name = (String) invokePrivate(launcher, "askPlayerName", new Class<?>[] {});
        assertEquals("Alice", name);
    }

    @Test
    @DisplayName("askApplicationMode retourne LOCAL")
    void askApplicationModeLocal() throws Exception {
        GameLauncher launcher = new GameLauncher();
        System.setIn(new ByteArrayInputStream("1\n".getBytes()));
        AppMode mode = (AppMode) invokePrivate(launcher, "askApplicationMode", new Class<?>[] {});
        assertEquals(AppMode.LOCAL, mode);
    }

    @Test
    @DisplayName("askApplicationMode retourne ONLINE")
    void askApplicationModeOnline() throws Exception {
        GameLauncher launcher = new GameLauncher();
        System.setIn(new ByteArrayInputStream("2\n".getBytes()));
        AppMode mode = (AppMode) invokePrivate(launcher, "askApplicationMode", new Class<?>[] {});
        assertEquals(AppMode.ONLINE, mode);
    }

    @Test
    @DisplayName("askApplicationMode redemande après une saisie invalide")
    void askApplicationModeRetry() throws Exception {
        GameLauncher launcher = new GameLauncher();
        System.setIn(new ByteArrayInputStream("x\n3\n2\n".getBytes()));
        AppMode mode = (AppMode) invokePrivate(launcher, "askApplicationMode", new Class<?>[] {});
        assertEquals(AppMode.ONLINE, mode);
    }

    @Test
    @DisplayName("loadInitialConfig retourne toujours une config")
    void loadInitialConfig() throws Exception {
        GameLauncher launcher = new GameLauncher();
        GameConfig config =
                (GameConfig) invokePrivate(launcher, "loadInitialConfig", new Class<?>[] {});
        assertNotNull(config);
    }

    @Test
    @DisplayName("fillRegister enregistre toutes les commandes principales")
    void fillRegister() throws Exception {
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
    void createDefaultConfigFileTest() throws Exception {
        GameLauncher launcher = new GameLauncher();
        assertDoesNotThrow(() -> invokePrivate(launcher, "createDefaultConfigFile", new Class<?>[] {}));
    }


    @Test
    @DisplayName("Test option -h (Help)")
    public void testHelpOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        assertDoesNotThrow(() -> launcher.launch(new String[] {"-h"}));
        assertTrue(getOutput().contains("HELP CONTENT") || getOutput().contains("usage"));
    }

    @Test
    @DisplayName("Test option -V (Version)")
    public void testVersionOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        assertDoesNotThrow(() -> launcher.launch(new String[] {"-V"}));
        assertTrue(getOutput().contains("VERSION CONTENT") || getOutput().contains("1.0.0"));
    }

    @Test
    @DisplayName("Test option -v (Verbose)")
    public void testVerboseOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        launcher.launch(new String[] {"-v"});
        assertTrue(getOutput().contains("Verbose mode enabled"));
    }

    @Test
    @DisplayName("Test option -d (Debug)")
    public void testDebugOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        launcher.launch(new String[] {"-d"});
        assertTrue(getOutput().contains("Debug mode enabled"));
    }

    @Test
    @DisplayName("Test option -g (GUI) isolé pour ne pas bloquer les tests")
    public void testGuiOption() throws InterruptedException {
        TestableGameLauncher launcher = new TestableGameLauncher();
        Thread t = new Thread(() -> {
            try {
                launcher.launch(new String[] {"-g"});
            } catch (Exception e) {
                //
            }
        });
        t.start();
        Thread.sleep(300);
        t.interrupt();

        assertTrue(getOutput().contains("Starting Graphical User Interface"));
    }

    @Test
    @DisplayName("Test option -c (Contest) avec fichier valide")
    public void testContestOption() throws IOException {
        File dummyFile = new File("dummy_contest_valid.txt");
        Files.writeString(dummyFile.toPath(), "test");
        try {
            TestableGameLauncher launcher = new TestableGameLauncher();
            launcher.launch(new String[] {"-c", dummyFile.getName()});
            assertTrue(getOutput().contains("[INFO] Contest mode detected."));
        } finally {
            dummyFile.delete();
        }
    }

    @Test
    @DisplayName("Test option -c sans fichier (Erreur)")
    public void testContestModeWithoutFile() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        assertDoesNotThrow(() -> launcher.launch(new String[] {"-c"}));
        assertTrue(errContent.toString().contains("Contest mode requires a file argument."));
    }

    @Test
    @DisplayName("Test option invalide")
    public void testInvalidOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        launcher.launch(new String[] {"-z"});
        assertTrue(
                errContent.toString().contains("Argument Error")
                        || outContent.toString().contains("Unrecognized option"));
    }


    @Test
    @DisplayName("printVersion fonctionne même sans fichier")
    void printVersionTest() throws Exception {
        GameLauncher launcher = new GameLauncher();
        Method m = GameLauncher.class.getDeclaredMethod("printVersion");
        m.setAccessible(true);
        assertDoesNotThrow(() -> m.invoke(launcher));
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
    @DisplayName("Test startGame interne via réflexion")
    void testStartGame_Internal() throws Exception {
        GameLauncher launcher = new GameLauncher();

        Options options = new Options();
        CommandLine cmd = new DefaultParser().parse(options, new String[] {});
        AppContext ctx = new AppContext(new LocalProfile("Test"));

        Thread t = new Thread(() -> {
            try {
                invokePrivate(
                        launcher,
                        "startGame",
                        new Class<?>[] {
                                GameConfig.class, CommandLine.class, AgonRegister.class, String.class, AppContext.class
                        },
                        new GameConfig(),
                        cmd,
                        new AgonRegister<>(),
                        null, // filePathToLoad
                        ctx);
            } catch (Exception e) {
                // Ignorer pour ne pas planter le thread principal
            }
        });

        t.start();
        Thread.sleep(300);
        t.interrupt();

        assertTrue(getOutput().contains("Starting Agon Shell..."));
    }
}