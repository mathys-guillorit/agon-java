package fr.univ.bordeaux.application;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
}
