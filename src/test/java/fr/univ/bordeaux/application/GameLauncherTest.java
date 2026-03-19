package fr.univ.bordeaux.application;

import static org.junit.jupiter.api.Assertions.*;

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
 *
 * <p>This class verifies that the CLI argument parsing correctly triggers the expected logic by
 * capturing and analyzing the standard output (System.out) and error output (System.err).
 */
public class GameLauncherTest {

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  /**
   * Sets up the test environment by redirecting System.out and System.err to internal buffers. This
   * allows us to read what the program prints.
   */
  @BeforeEach
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  /**
   * Restores the original System.out and System.err streams after each test to avoid interfering
   * with other tests or the IDE console.
   */
  @AfterEach
  public void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  /** Helper method to get the console output as a clean String. */
  private String getOutput() {
    return outContent.toString();
  }

  /** Tests that the help (-h) option displays the help message and does not crash. */
  @Test
  public void testHelpOption() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-h"};
    assertDoesNotThrow(() -> launcher.launch(args));
    assertTrue(
        getOutput().contains("Available Commands") || getOutput().contains("usage"),
        "Output should contain help information.");
  }

  /** Tests that the version (-V) option displays version info. */
  @Test
  public void testVersionOption() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-V"};
    assertDoesNotThrow(() -> launcher.launch(args));
    assertTrue(
        getOutput().contains("version") || getOutput().contains("1.0.0"),
        "Output should contain version information.");
  }

  /**
   * Tests that the Verbose mode (-v) is correctly detected. This covers the 'if
   * (cmd.hasOption("v"))' branch.
   */
  @Test
  public void testVerboseOption() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-v"};
    launcher.launch(args);
    assertTrue(getOutput().contains("Verbose mode enabled"));
  }

  /**
   * Tests that the Debug mode (-d) is correctly detected. This covers the 'if (cmd.hasOption("d"))'
   * branch.
   */
  @Test
  public void testDebugOption() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-d"};
    launcher.launch(args);
    assertTrue(getOutput().contains("Debug mode enabled"));
  }

  /**
   * Tests that the Contest mode (-c) is correctly detected. This covers the 'if
   * (cmd.hasOption("c"))' branch.
   */
  @Test
  public void testContestOption() throws IOException {
    File dummyFile = new File("dummy_contest_valid.txt");
    Files.writeString(dummyFile.toPath(), "[game]\nX\nq" + ".".repeat(120));

    try {
      GameLauncher launcher = new GameLauncher();
      String[] args = {"-c", dummyFile.getName()};
      assertDoesNotThrow(
          () -> launcher.launch(args),
          "Launcher should not throw exceptions for a valid contest file.");
      assertFalse(
          errContent.toString().contains("[ERROR]"),
          "There should be no execution error printed to System.err.");
    } finally {
      dummyFile.delete();
    }
  }

  /**
   * Tests that the Contest mode handles the absence of a file argument gracefully. Covers the
   * 'else' branch of 'if (fileArg.length > 0)' where it prints an error and help.
   */
  @Test
  public void testContestModeWithoutFile() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-c"};
    assertDoesNotThrow(
        () -> launcher.launch(args),
        "Launcher should not crash when the file argument is missing.");
    String errorOutput = errContent.toString();
    assertTrue(
        errorOutput.contains("Contest mode requires a file argument."),
        "Should print an error message indicating the missing save file.");
    String allOutput = getOutput().toLowerCase() + errorOutput.toLowerCase();
    assertTrue(
        allOutput.contains("usage") || allOutput.contains("available"),
        "Should print the help menu when the file argument is missing.");
  }

  /**
   * Tests that the GUI mode (-g) is correctly detected. This covers the 'if (cmd.hasOption("g"))'
   * branch in startGame.
   */
  @Test
  public void testGuiOption() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-g"};
    assertDoesNotThrow(
        () -> {
          launcher.launch(args);
        });
  }

  /** Tests that the Blitz mode (-b) is correctly detected and configured. */
  @Test
  public void testBlitzModeActivation() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-b"}; // Blitz simple
    launcher.launch(args);
    assertTrue(
        getOutput().contains("Blitz mode activated"),
        "Output should confirm blitz mode activation.");
    assertTrue(getOutput().contains("30 minutes"), "Default blitz time should be 30 minutes.");
  }

  /** Tests that the Blitz mode with custom time (-b -t 15) works correctly. */
  @Test
  public void testBlitzModeWithCustomTime() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-b", "-t", "15"};
    launcher.launch(args);
    assertTrue(getOutput().contains("Blitz mode activated"), "Blitz mode should be active.");
    assertTrue(getOutput().contains("15 minutes"), "Blitz time should be updated to 15 minutes.");
  }

  /** Tests the warning logic when Time (-t) is provided without Blitz (-b). */
  @Test
  public void testTimeWithoutBlitzWarning() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-t", "10"}; // Pas de -b !
    launcher.launch(args);
    assertTrue(getOutput().contains("WARNING"), "Should print a warning.");
    assertTrue(getOutput().contains("ignored"), "Should say the option is ignored.");
  }

  /**
   * Tests the error handling when an invalid string is passed to the time option. This specifically
   * triggers the 'catch (NumberFormatException e)' block to ensure the application doesn't crash
   * and falls back to the default 30 minutes.
   */
  @Test
  public void testBadTimeFormat() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-b", "-t", "abc"};
    launcher.launch(args);
    assertTrue(errContent.toString().contains("Invalid time format"));
    assertTrue(getOutput().contains("30 minutes"));
  }

  /** Tests AI configuration for White player (-a W). */
  @Test
  public void testAiWhiteConfiguration() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-a", "W"};
    launcher.launch(args);
    assertTrue(
        getOutput().contains("AI configured to play White"),
        "Output should confirm AI is set for White.");
  }

  /** Tests AI configuration for Default/Black player (-a). */
  @Test
  public void testAiDefaultConfiguration() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-a", "B"};
    launcher.launch(args);
    assertTrue(
        getOutput().contains("AI configured to play Black"),
        "Output should confirm AI is set for Black.");
  }

  /**
   * Tests AI configuration when set to play Both sides (-a A). This covers the final 'else if
   * ("A".equalsIgnoreCase(color))' branch.
   */
  @Test
  public void testAiBoth() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-a", "A"};
    launcher.launch(args);
    assertTrue(getOutput().contains("AI configured to play Both sides"));
  }

  /**
   * Tests AI configuration with an invalid color. Covers the fall-through of the if-else if block.
   */
  @Test
  public void testAiInvalidColor() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-a", "Z"};
    launcher.launch(args);
    assertFalse(
        getOutput().contains("AI configured to play"),
        "Should not configure AI for invalid color.");
  }

  /**
   * Tests AI configuration without specifying a color (uses DEFAULT). Covers the optional argument
   * handling.
   */
  @Test
  public void testAiNoColor() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-a"};
    launcher.launch(args);
    assertFalse(
        getOutput().contains("AI configured to play"),
        "Should not configure AI if no valid color is provided.");
  }

  /** Tests the conflict management: Manual Placement (-p) vs Save File. */
  @Test
  public void testManualPlacementConflictWithFile() throws IOException {
    File test_File = new File("save_test.txt");
    test_File.createNewFile();
    try {
      GameLauncher launcher = new GameLauncher();
      String[] args = {"-p", "save_test.txt"};
      launcher.launch(args);
      String output = getOutput();
      assertTrue(
          output.contains("WARNING"),
          "Should print a warning about conflict. Output was:\n" + output);
      assertTrue(output.contains("File argument detected"), "Should detect the file.");

    } finally {
      test_File.delete();
    }
  }

  /** Tests that providing an invalid command-line option handles the exception gracefully. */
  @Test
  public void testInvalidOption() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"-z"};
    launcher.launch(args);
    String allOutput = errContent.toString() + outContent;
    assertTrue(
        allOutput.contains("Argument Error") || allOutput.contains("Unrecognized option"),
        "Should report an argument error.");
  }

  /** Tests that providing a non-existent file causes an error and stops execution. */
  @Test
  public void testMissingFile() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"not_exist.txt"};
    launcher.launch(args);
    assertTrue(errContent.toString().contains("[ERROR]"), "Should print an error message.");
    assertTrue(
        errContent.toString().contains("does not exist"), "Should specify the file is missing.");
    assertFalse(
        getOutput().contains("Starting Agon Shell"),
        "The game should not start if the file is missing.");
  }

  /** Tests passing a directory instead of a file. Covers the 'file.isDirectory()' condition. */
  @Test
  public void testFileIsDirectory() {
    GameLauncher launcher = new GameLauncher();
    String[] args = {"."};
    launcher.launch(args);
    assertTrue(errContent.toString().contains("or is a directory"), "Should reject directories.");
  }

  /** Tests the successful creation of the default config file. */
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
      GameLauncher launcher = new GameLauncher();
      launcher.launch(new String[] {});
      assertTrue(
          getOutput().contains("Minimal configuration file created"),
          "Should successfully create the default config file.");
    } finally {
      configFile.delete();
      if (backup.exists()) {
        backup.renameTo(new File(path));
      }
    }
  }

  /**
   * Tests the IOException in createDefaultConfigFile by creating a directory named ".agonrc",
   * preventing the file from being written.
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
      GameLauncher launcher = new GameLauncher();
      launcher.launch(new String[] {});
      assertTrue(
          errContent.toString().contains("[ERROR] Failed to save default config"),
          "Should trigger and catch IOException.");
    } finally {
      fakeConfigDir.delete();
      if (backup.exists()) {
        backup.renameTo(new File(path));
      }
    }
  }

  /** Tests passing a valid file without the '-p' option. */
  @Test
  public void testValidFile() throws IOException {
    File testFile = new File("valid_save_test.txt");
    testFile.createNewFile();
    try {
      GameLauncher launcher = new GameLauncher();
      String[] args = {"valid_save_test.txt"};
      launcher.launch(args);
      assertTrue(getOutput().contains("File argument detected"));
      assertFalse(getOutput().contains("Option '-p' (Manual Placement) is ignored"));
    } finally {
      testFile.delete();
    }
  }

  /**
   * Tests that the application correctly handles and logs a failure when Contest mode is initiated
   * with an invalid or missing save file.
   */
  @Test
  public void testContestModeExecutionFailure() throws IOException {
    File invalidFile = new File("invalid_save_for_failure.txt");
    invalidFile.createNewFile();
    try {
      String[] args = {"-c", invalidFile.getName()};
      GameLauncher launcher = new GameLauncher();
      launcher.launch(args);

      assertTrue(
          errContent.toString().contains("[ERROR] Contest mode failed"),
          "The contest executor error should be displayed in System.err");
    } finally {
      invalidFile.delete();
    }
  }

  /**
   * Tests the help display behavior when an IOException occurs during file reading. Uses an
   * anonymous subclass to override getHelpContent and force a simulated error, ensuring the catch
   * block is executed for full coverage.
   */
  @Test
  public void testPrintHelp() {
    GameLauncher launcher =
        new GameLauncher() {
          @Override
          protected String getHelpContent() throws IOException {
            throw new IOException("Simulated Error");
          }
        };

    launcher.launch(new String[] {"-h"});
    assertTrue(
        errContent.toString().contains("agonShellMenu.txt not found"),
        "The version catch block should be covered.");
  }

  /**
   * Tests the version display behavior when an IOException occurs during file reading. Uses an
   * anonymous subclass to override getVersionContent and force a simulated error, ensuring the
   * catch block is executed for full coverage.
   */
  @Test
  public void testPrintVersion() {
    GameLauncher launcher =
        new GameLauncher() {
          @Override
          protected String getVersionContent() throws IOException {
            throw new IOException("Simulated Error");
          }
        };

    launcher.launch(new String[] {"-V"});
    assertTrue(
        errContent.toString().contains("version.txt not found"),
        "The version catch block should be covered.");
  }
}
