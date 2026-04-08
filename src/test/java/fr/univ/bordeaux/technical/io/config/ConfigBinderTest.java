package fr.univ.bordeaux.technical.io.config;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import java.io.ByteArrayOutputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConfigBinderTest {

  private GameConfig config;
  private GameUserInterface ui;
  private ByteArrayOutputStream out;
  private Options options;
  private CommandLineParser parser = new DefaultParser();

  @BeforeEach
  void setUp() {
    config = new GameConfig();
    out = new ByteArrayOutputStream();
    LineReader reader = new FakeLineReader("n");

    try {
      Terminal terminal = new FakeTerminal(out);
      ui = new AgonShell(terminal, reader, new AgonRegister<>());

      options = new Options();
      options.addOption("b", "blitz", false, "blitz");
      options.addOption("t", "time", true, "time");
      options.addOption("a", "ai", true, "ai");

      options.addOption(null, "ai-mode", true, "mode");
      options.addOption(null, "ai-minimax-depth", true, "depth");
      options.addOption(null, "ai-minimax-scoring", true, "scoring");
      options.addOption(null, "ai-time", true, "aitime");
      options.addOption("v", "verbose", false, "verbose");
      options.addOption(null, "ai-mcts-selection", true, "mcts selection");
      options.addOption("d", "debug", false, "debug");
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  @Test
  @DisplayName("Test MCTS Selection: ML and UCT")
  void testAiMctsSelection() throws Exception {

    CommandLine cmd = parser.parse(options, new String[] {"--ai-mode", "mcts", "--ai-mcts-selection", "ML"});
    ConfigBinder.bindOptionsToConfig(cmd, config, ui);
    assertEquals("ml", config.getAiHeuristic());

    config = new GameConfig();
    cmd = parser.parse(options, new String[] {"--ai-mode", "mcts", "--ai-mcts-selection", "UCT"});
    ConfigBinder.bindOptionsToConfig(cmd, config, ui);
    assertEquals("uct", config.getAiHeuristic());

    config = new GameConfig();
    cmd = parser.parse(options, new String[] {"--ai-mode", "minimax", "--ai-mcts-selection", "ML"});
    ConfigBinder.bindOptionsToConfig(cmd, config, ui);
    assertNotEquals("ml", config.getAiHeuristic());
    assertTrue(out.toString().contains("Mcts mode for Ai is not active"));
  }

  @Test
  @DisplayName("Test MCTS Selection: Invalid mode defaults to UCT")
  void testAiMctsSelectionInvalid() throws Exception {
    CommandLine cmd = parser.parse(options, new String[] {"--ai-mode", "mcts", "--ai-mcts-selection", "RANDOM"});
    ConfigBinder.bindOptionsToConfig(cmd, config, ui);
    assertTrue(out.toString().contains("Unreconised mode for mcts"));
  }

  @Test
  @DisplayName("Test Verbose and Debug modes")
  void testVerboseAndDebug() throws Exception {
    CommandLine cmdV = parser.parse(options, new String[] {"-v"});
    ConfigBinder.bindOptionsToConfig(cmdV, config, ui);
    assertTrue(config.isVerbose());

    config = new GameConfig();
    CommandLine cmdD = parser.parse(options, new String[] {"-d"});
    ConfigBinder.bindOptionsToConfig(cmdD, config, ui);
    assertTrue(config.isDebug());
  }
  @Test
  void testTimeoutWithoutBlitz() throws Exception {
    CommandLine cmd = parser.parse(options, new String[] {"-t", "5"});
    ConfigBinder.bindOptionsToConfig(cmd, config, ui);

    assertFalse(config.isBlitzMode());
    assertTrue(out.toString().contains("ignored"));
  }

  @Test
  void testAiActivationColors() throws Exception {
    ConfigBinder.bindOptionsToConfig(
        parser.parse(options, new String[] {"-a", "white"}), config, ui);
    assertTrue(config.isWhiteAi());

    config = new GameConfig();
    ConfigBinder.bindOptionsToConfig(parser.parse(options, new String[] {"-a", "all"}), config, ui);
    assertTrue(config.isWhiteAi() && config.isBlackAi());

    config = new GameConfig();
    options.getOption("a").setOptionalArg(true);
    CommandLine cmd = parser.parse(options, new String[] {"-a"});
    ConfigBinder.bindOptionsToConfig(cmd, config, ui);
    assertTrue(config.isBlackAi());
    assertTrue(out.toString().contains("No Color given"));
  }

  @Test
  void testAiActivationInvalidColor() throws Exception {
    CommandLine cmd = parser.parse(options, new String[] {"-a", "purple"});
    ConfigBinder.bindOptionsToConfig(cmd, config, ui);
    assertTrue(config.isBlackAi());
    assertTrue(out.toString().contains("Invalid Color"));
  }

  @Test
  void testAiModes() throws Exception {
    ConfigBinder.bindOptionsToConfig(
        parser.parse(options, new String[] {"--ai-mode", "minimax"}), config, ui);
    assertEquals("minimax", config.getAiMode());

    ConfigBinder.bindOptionsToConfig(
        parser.parse(options, new String[] {"--ai-mode", "iterative"}), config, ui);
    assertEquals("iterative", config.getAiMode());
    assertTrue(config.isAiIterativeDeepening());
  }

  @Test
  void testAiMinimaxDepth() throws Exception {
    ConfigBinder.bindOptionsToConfig(
        parser.parse(options, new String[] {"--ai-minimax-depth", "4"}), config, ui);
    assertEquals(4, config.getAiDepth());
  }

  @Test
  void testAiHeuristics() throws Exception {
    ConfigBinder.bindOptionsToConfig(
        parser.parse(options, new String[] {"--ai-minimax-scoring", "centrality"}), config, ui);
    assertEquals("centrality", config.getAiHeuristic());
  }

  @Test
  void testAiTimeLimit() throws Exception {
    ConfigBinder.bindOptionsToConfig(
        parser.parse(options, new String[] {"--ai-time", "1000"}), config, ui);
    assertEquals(1000, config.getAiTimeLimit());
  }
}