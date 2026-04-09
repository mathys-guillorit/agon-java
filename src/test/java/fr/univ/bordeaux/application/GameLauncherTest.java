package fr.univ.bordeaux.application;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.GameEngine;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameLauncherTest {

    private Object invokePrivate(
            Object target, String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = GameLauncher.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    // --- LE MOCK QUI ÉVITE DE LANCER JAVAFX ET DE PLANTER LES TESTS ---
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
        protected String askPlayerName() {
            return "TestPlayer";
        }

        @Override
        protected AppMode askApplicationMode() {
            return AppMode.LOCAL;
        }

        // Intercepte le lancement de l'interface graphique
        @Override
        protected void launchGUI(GameConfig config, AgonRegister<CmdAction> cmds, AppContext context) {
            this.guiLaunched = true;
        }

        // Intercepte le lancement de la console
        @Override
        protected void launchCLI(GameConfig config, CommandLine cmd, AgonRegister<CmdAction> cmds, String filePathToLoad, AppContext context) {
            this.cliLaunched = true;
            this.parsedCmd = cmd;
        }
    }

    @Test
    @DisplayName("Constructeur")
    void constructor_test() {
        GameLauncher launcher = new GameLauncher();
        assertNotNull(launcher);
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
        assertTrue(cmds.get("join").isPresent());
        assertTrue(cmds.get("quit").isPresent());
    }

    @Test
    @DisplayName("createDefaultConfigFile ne plante pas")
    void createDefaultConfigFileTest() throws Exception {
        GameLauncher launcher = new GameLauncher();
        assertDoesNotThrow(() -> invokePrivate(launcher, "createDefaultConfigFile", new Class<?>[] {}));
    }

    // ==========================================
    // TESTS DES ARGUMENTS CLI (ROBUSTES)
    // ==========================================

    @Test
    @DisplayName("Test option -h (Help)")
    public void testHelpOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        assertDoesNotThrow(() -> launcher.launch("-h"));
        assertFalse(launcher.cliLaunched, "Le jeu ne doit pas démarrer avec l'option -h");
    }

    @Test
    @DisplayName("Test option -V (Version)")
    public void testVersionOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        assertDoesNotThrow(() -> launcher.launch("-V"));
        assertFalse(launcher.cliLaunched, "Le jeu ne doit pas démarrer avec l'option -V");
    }

    @Test
    @DisplayName("Test option -v (Verbose)")
    public void testVerboseOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        launcher.launch("-v");
        assertNotNull(launcher.parsedCmd, "La ligne de commande a dû être analysée");
        assertTrue(launcher.parsedCmd.hasOption("v"), "L'option -v doit être détectée");
    }

    @Test
    @DisplayName("Test option -d (Debug)")
    public void testDebugOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        launcher.launch("-d");
        assertNotNull(launcher.parsedCmd, "La ligne de commande a dû être analysée");
        assertTrue(launcher.parsedCmd.hasOption("d"), "L'option -d doit être détectée");
    }

    @Test
    @DisplayName("Test option -g (GUI) intercepté")
    public void testGuiOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        assertDoesNotThrow(() -> launcher.launch("-g"));
        assertTrue(launcher.guiLaunched, "Le lanceur a dû appeler l'interface graphique");
        assertFalse(launcher.cliLaunched, "Le lanceur n'a pas dû appeler le terminal");
    }

    @Test
    @DisplayName("Test option -c (Contest) avec fichier valide")
    public void testContestOption() throws IOException {
        File dummyFile = new File("dummy_contest_valid.txt");
        Files.writeString(dummyFile.toPath(), "test");
        try {
            TestableGameLauncher launcher = new TestableGameLauncher();
            launcher.launch("-c", dummyFile.getName());
            assertFalse(launcher.cliLaunched, "Le mode Contest remplace le lancement normal");
        } finally {
            dummyFile.delete();
        }
    }

    @Test
    @DisplayName("Test option -c sans fichier (Erreur)")
    public void testContestModeWithoutFile() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        assertDoesNotThrow(() -> launcher.launch("-c"));
        assertFalse(launcher.cliLaunched, "Une erreur de fichier doit annuler le lancement normal");
    }

    @Test
    @DisplayName("Test option invalide")
    public void testInvalidOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        launcher.launch("-z");
        assertNull(launcher.parsedCmd, "Une option invalide déclenche une exception et stoppe le parsing");
        assertFalse(launcher.cliLaunched, "Le jeu ne doit pas démarrer avec une option invalide");
    }

    // ==========================================
    // TESTS DU ROUTAGE STARTGAME (À 5 PARAMÈTRES)
    // ==========================================

    @Test
    @DisplayName("Test startGame route vers GUI si l'option -g est présente")
    void testStartGame_RoutesToGUI() throws Exception {
        TestableGameLauncher launcher = new TestableGameLauncher();

        org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        options.addOption("g", "gui", false, "");
        CommandLine cmd = new org.apache.commons.cli.DefaultParser().parse(options, new String[] {"-g"});

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

        assertTrue(launcher.guiLaunched, "Le jeu aurait dû lancer l'interface graphique (GUI)");
    }

    @Test
    @DisplayName("Test startGame route vers CLI si l'option -g est absente")
    void testStartGame_RoutesToCLI() throws Exception {
        TestableGameLauncher launcher = new TestableGameLauncher();

        org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        CommandLine cmd = new org.apache.commons.cli.DefaultParser().parse(options, new String[] {});

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

        assertTrue(launcher.cliLaunched, "Le jeu aurait dû lancer le terminal (CLI)");
    }
}