package fr.univ.bordeaux.application;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.technical.config.GameConfig;
import org.apache.commons.cli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link GameLauncher} class.
 */
public class GameLauncherTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    /**
     * SOUS-CLASSE DE TEST :
     * Permet d'exécuter toute la logique de parsing des arguments sans jamais
     * lancer la boucle infinie du GameEngine, la fenêtre JavaFX, ou le mode Contest.
     */
    private static class TestableGameLauncher extends GameLauncher {
        public CommandLine parsedCmd;

        @Override
        protected void startGame(GameConfig config, CommandLine cmd, String filePathToLoad) {
            // ON NE FAIT RIEN ! On bloque le lancement réel du jeu pour protéger les tests.
            this.parsedCmd = cmd;
        }

        @Override
        protected void runContest(String filePath) throws Exception {
            if (filePath.contains("invalid_save_for_failure")) {
                throw new Exception("Simulated contest error");
            }
        }

        @Override
        protected String getHelpContent() throws IOException {
            return "Mock Help Content";
        }

        @Override
        protected String getVersionContent() throws IOException {
            return "Mock Version Content";
        }
    }

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private String getOutput() {
        return outContent.toString();
    }

    @Test
    public void testHelpOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-h"};
        assertDoesNotThrow(() -> launcher.launch(args));
        assertTrue(getOutput().contains("Mock Help Content") || getOutput().contains("usage"));
    }

    @Test
    public void testVersionOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-V"};
        assertDoesNotThrow(() -> launcher.launch(args));
        assertTrue(getOutput().contains("Mock Version Content") || getOutput().contains("1.0.0"));
    }

    @Test
    public void testVerboseOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-v"};
        launcher.launch(args);
        assertTrue(getOutput().contains("Verbose mode enabled"));
    }

    @Test
    public void testDebugOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-d"};
        launcher.launch(args);
        assertTrue(getOutput().contains("Debug mode enabled"));
    }

    @Test
    public void testGuiOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-g"};
        assertDoesNotThrow(() -> { launcher.launch(args); });
        assertNotNull(launcher.parsedCmd);
        assertTrue(launcher.parsedCmd.hasOption("g"));
    }

    @Test
    public void testContestOption() throws IOException {
        File dummyFile = new File("dummy_contest_valid.txt");
        Files.writeString(dummyFile.toPath(), "[game]\nX\nq" + ".".repeat(120));

        try {
            TestableGameLauncher launcher = new TestableGameLauncher();
            String[] args = {"-c", dummyFile.getName()};
            assertDoesNotThrow(() -> launcher.launch(args));
            assertFalse(errContent.toString().contains("[ERROR]"));
        } finally {
            dummyFile.delete();
        }
    }

    @Test
    public void testContestModeWithoutFile() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-c"};
        assertDoesNotThrow(() -> launcher.launch(args));
        assertTrue(errContent.toString().contains("Contest mode requires a file argument."));
    }

    @Test
    public void testContestModeExecutionFailure() throws IOException {
        File invalidFile = new File("invalid_save_for_failure.txt");
        invalidFile.createNewFile();
        try {
            String[] args = {"-c", invalidFile.getName()};
            TestableGameLauncher launcher = new TestableGameLauncher();
            launcher.launch(args);
            assertTrue(errContent.toString().contains("[ERROR] Contest mode failed"));
        } finally {
            invalidFile.delete();
        }
    }

    @Test
    public void testInvalidOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-z"};
        launcher.launch(args);
        assertTrue(errContent.toString().contains("Argument Error") || outContent.toString().contains("Unrecognized option"));
    }

    @Test
    public void testMissingFile() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"not_exist.txt"};
        launcher.launch(args);
        assertTrue(errContent.toString().contains("[ERROR]"));
    }

    @Test
    public void testFileIsDirectory() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"."};
        launcher.launch(args);
        assertTrue(errContent.toString().contains("or is a directory"));
    }

    @Test
    public void testCreateDefaultConfig_Success() {
        String path = System.getProperty("user.dir") + File.separator + ".agonrc";
        File configFile = new File(path);
        File backup = new File(path + "_backup_success_test");
        if (configFile.exists() && !configFile.isDirectory()) {
            configFile.renameTo(backup);
        } else {
            configFile.delete();
        }
        try {
            TestableGameLauncher launcher = new TestableGameLauncher();
            launcher.launch(new String[] {});
            assertTrue(getOutput().contains("Minimal configuration file created"));
        } finally {
            configFile.delete();
            if (backup.exists()) {
                backup.renameTo(new File(path));
            }
        }
    }

    @Test
    public void testCreateDefaultConfig_Failed() {
        String path = System.getProperty("user.dir") + File.separator + ".agonrc";
        File fakeConfigDir = new File(path);
        File backup = new File(path + "_backup");
        if (fakeConfigDir.exists() && !fakeConfigDir.isDirectory()) {
            fakeConfigDir.renameTo(backup);
        }
        fakeConfigDir.mkdir();

        try {
            TestableGameLauncher launcher = new TestableGameLauncher();
            launcher.launch(new String[] {});
            assertTrue(errContent.toString().contains("[ERROR] Failed to save default config"));
        } finally {
            fakeConfigDir.delete();
            if (backup.exists()) {
                backup.renameTo(new File(path));
            }
        }
    }

    @Test
    public void testValidFile() throws IOException {
        File testFile = new File("valid_save_test.txt");
        testFile.createNewFile();
        try {
            TestableGameLauncher launcher = new TestableGameLauncher();
            String[] args = {"valid_save_test.txt"};
            launcher.launch(args);
            assertTrue(getOutput().contains("File argument detected"));
        } finally {
            testFile.delete();
        }
    }

    @Test
    public void testPrintHelp() {
        TestableGameLauncher launcher = new TestableGameLauncher() {
            @Override
            protected String getHelpContent() throws IOException {
                throw new IOException("Simulated Error");
            }
        };
        launcher.launch(new String[] {"-h"});
        assertTrue(errContent.toString().contains("agonShellMenu.txt not found"));
    }

    @Test
    public void testPrintVersion() {
        TestableGameLauncher launcher = new TestableGameLauncher() {
            @Override
            protected String getVersionContent() throws IOException {
                throw new IOException("Simulated Error");
            }
        };
        launcher.launch(new String[] {"-V"});
        assertTrue(errContent.toString().contains("version.txt not found"));
    }
}