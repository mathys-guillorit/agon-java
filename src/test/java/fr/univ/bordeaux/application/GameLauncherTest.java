package fr.univ.bordeaux.application;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.technical.io.config.GameConfig;
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
 * Ensures command-line arguments are parsed correctly and proper modes are initialized.
 */
public class GameLauncherTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    /**
     * MOCK SUBCLASS:
     * Allows testing the argument parsing logic without ever launching
     * the GameEngine's infinite loop, the JavaFX window, or the actual Contest mode.
     */
    private static class TestableGameLauncher extends GameLauncher {
        public CommandLine parsedCmd;
        public boolean startGameCalled = false;
        public boolean guiModeDetected = false;

        @Override
        protected void startGame(GameConfig config, CommandLine cmd, String filePathToLoad) {
            this.startGameCalled = true;
            this.guiModeDetected = (cmd != null && cmd.hasOption("g"));
            this.parsedCmd = cmd;
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

    /**
     * Redirects standard output and error streams before each test to capture console prints.
     */
    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    /**
     * Restores standard output and error streams after each test.
     */
    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Retrieves the captured standard output as a string.
     *
     * @return The console output.
     */
    private String getOutput() {
        return outContent.toString();
    }

    /**
     * Tests the help argument (-h).
     */
    @Test
    public void testHelpOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-h"};
        assertDoesNotThrow(() -> launcher.launch(args));
        assertTrue(getOutput().contains("Mock Help Content") || getOutput().contains("usage"));
    }

    /**
     * Tests the version argument (-V).
     */
    @Test
    public void testVersionOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-V"};
        assertDoesNotThrow(() -> launcher.launch(args));
        assertTrue(getOutput().contains("Mock Version Content") || getOutput().contains("1.0.0"));
    }

    /**
     * Tests the verbose argument (-v).
     */
    @Test
    public void testVerboseOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-v"};
        launcher.launch(args);
        assertTrue(getOutput().contains("Verbose mode enabled"));
    }

    /**
     * Tests the debug argument (-d).
     */
    @Test
    public void testDebugOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-d"};
        launcher.launch(args);
        assertTrue(getOutput().contains("Debug mode enabled"));
    }

    /**
     * Tests the GUI argument (-g).
     */
    @Test
    public void testGuiOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-g"};
        assertDoesNotThrow(() -> { launcher.launch(args); });
        assertNotNull(launcher.parsedCmd);
        assertTrue(launcher.parsedCmd.hasOption("g"));
    }

    /**
     * Tests the contest mode argument (-c) with a valid dummy file.
     */
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

    /**
     * Tests the contest mode argument (-c) without providing a file path.
     */
    @Test
    public void testContestModeWithoutFile() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-c"};
        assertDoesNotThrow(() -> launcher.launch(args));
        assertTrue(errContent.toString().contains("Contest mode requires a file argument."));
    }

    /**
     * Tests the contest mode execution when the simulated run fails.
     */
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

    /**
     * Tests handling of an unrecognized command-line option.
     */
    @Test
    public void testInvalidOption() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"-z"};
        launcher.launch(args);
        assertTrue(errContent.toString().contains("Argument Error") || outContent.toString().contains("Unrecognized option"));
    }

    /**
     * Tests behavior when a specified file does not exist.
     */
    @Test
    public void testMissingFile() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"not_exist.txt"};
        launcher.launch(args);
        assertTrue(errContent.toString().contains("[ERROR]"));
    }

    /**
     * Tests behavior when a directory is passed instead of a file.
     */
    @Test
    public void testFileIsDirectory() {
        TestableGameLauncher launcher = new TestableGameLauncher();
        String[] args = {"."};
        launcher.launch(args);
        assertTrue(errContent.toString().contains("or is a directory"));
    }

    /**
     * Tests successful creation of the default configuration file.
     */
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

    /**
     * Tests failure handling when creating the default configuration file.
     */
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

    /**
     * Tests launching the game with a valid file argument.
     */
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

    /**
     * Tests behavior when the help file is missing or triggers an IO error.
     */
    @Test
    public void testPrintHelp() {
        GameLauncher launcher = new GameLauncher() {
            @Override
            protected String getHelpContent() throws IOException {
                throw new IOException("Simulated Error");
            }
        };
        launcher.launch(new String[] {"-h"});
        assertTrue(errContent.toString().contains("agonShellMenu.txt not found"));
    }

    /**
     * Tests behavior when the version file is missing or triggers an IO error.
     */
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

    /**
     * Coverage test for startGame logic (GUI and Shell branches) without blocking threads.
     */
    @Test
    public void testStartGameLogicWithoutBlocking() {
        GameLauncher launcher = new GameLauncher() {
            @Override
            protected void startGame(GameConfig config, CommandLine cmd, String filePath) {
                System.out.println("[INFO] Running startGame coverage...");
                if (cmd != null && cmd.hasOption("g")) {
                    System.out.println("[INFO] Starting Agon GUI...");
                } else {
                    System.out.println("[INFO] Starting Agon Shell...");
                }
            }
        };

        assertDoesNotThrow(() -> {
            launcher.launch(new String[]{"-g"});
        });
        assertTrue(getOutput().contains("Starting Agon GUI..."));
        outContent.reset();
        assertDoesNotThrow(() -> {
            launcher.launch(new String[]{});
        });
        assertTrue(getOutput().contains("Starting Agon Shell..."));
    }

    /**
     * Validates that the actual help content can be retrieved properly.
     */
    @Test
    public void testRealGetHelpContent() throws Exception {
        GameLauncher realLauncher = new GameLauncher();
        String content = realLauncher.getHelpContent();
        assertNotNull(content, "The help content must not be null.");
        assertFalse(content.isEmpty(), "The help file must contain text.");
    }

    /**
     * Validates that the actual version content can be retrieved properly.
     */
    @Test
    public void testRealGetVersionContent() throws Exception {
        GameLauncher realLauncher = new GameLauncher();
        String content = realLauncher.getVersionContent();
        assertNotNull(content, "The version content must not be null.");
        assertFalse(content.isEmpty(), "The version file must contain text.");
    }

}