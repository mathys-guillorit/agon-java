package fr.univ.bordeaux.application.commands.specialized;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.GameUserInterface;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jdk.jfr.Description;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CmdSetTest {
  private AgonRegister<CmdAction> cmds = new AgonRegister<>();
  private GameUserInterface gameUserInterface;
  private GameConfig config;
  private ByteArrayOutputStream outContent;

  @BeforeEach
  void setUp() {
    config = new GameConfig();
    outContent = new ByteArrayOutputStream();
    LineReader reader = new FakeLineReader("");
    try {
      Terminal terminal = new FakeTerminal(outContent);
      gameUserInterface = new AgonShell(terminal, reader, cmds);

      // Register the prototype with the real config
      cmds.register("set", new CmdSet(gameUserInterface, config));
    } catch (Exception e) {
      // fail("Setup failed");
      e.printStackTrace();
    }
  }

  @Test
  @DisplayName("Verify system parameters modification (PARAM=VALUE format)")
  void testSetSystemParams() {
    config.setVerbose(false);
    config.setDebug(false);

    // Simulation: set verbose=true debug=true
    CmdAction cmd = cmds.get("set").get().createNew(new String[] {"verbose=true", "debug=true"});
    boolean result = cmd.execute(null);

    assertTrue(result);
    assertTrue(config.isVerbose());
    assertTrue(config.isDebug());
    assertTrue(outContent.toString().contains("Verbose: true"));
    cmd = cmds.get("set").get().createNew(new String[] {"verbose=false", "debug=false"});
    cmd.execute(null);
    assertFalse(config.isVerbose());
    assertFalse(config.isDebug());
  }

  @Test
  @DisplayName("Verify AI parameters modification")
  void testSetAIParams() {
    // Simulation: set aiDepth=8 aiMode=minimax ...
    CmdAction cmd =
        cmds.get("set")
            .get()
            .createNew(
                new String[] {
                  "aiDepth=8",
                  "aiMode=minimax",
                  "aiTimeLimit=1800",
                  "aiIterativeDeepening=true",
                  "aiHeuristic=mixed",
                  "blitzmode=true",
                  "aiActive=true",
                  "timeout=10"
                });
    cmd.execute(null);

    assertEquals(8, config.getAiDepth());
    assertEquals("minimax", config.getAiMode());
    assertEquals(1800, config.getAiTimeLimit());
    assertTrue(config.isBlitzMode());
    assertTrue(outContent.toString().contains("AI Depth: 8"));
  }

  @Test
  @DisplayName("Verify error handling for invalid numbers")
  void testSetInvalidNumber() {
    // Passing a string instead of a number for timeout
    CmdAction cmd = cmds.get("set").get().createNew(new String[] {"timeout=not_a_number"});
    boolean result = cmd.execute(null);

    // The command should return false due to NumberFormatException in execute()
    assertFalse(result, "Command should fail with bad number format");
    assertTrue(outContent.toString().contains("Error: Numeric value expected"));
  }

  @Test
  @DisplayName("Verify error handling for missing '=' sign")
  void testSetInvalidSyntax() {
    // Malformed argument without '='
    CmdAction cmd = cmds.get("set").get().createNew(new String[] {"badformat"});
    boolean result = cmd.execute(null);

    // result might be true if other params work, but let's check if the error was shown
    assertTrue(outContent.toString().contains("Invalid format"));
  }

  @Test
  @DisplayName("Verify AI player assignment")
  void testSetPlayerAI() {
    // set whiteIsAI=true blackIsAI=false
    CmdAction cmd =
        cmds.get("set").get().createNew(new String[] {"whiteIsAi=true", "blackIsAi=false"});
    cmd.execute(null);

    assertTrue(config.isWhiteAi());
    assertFalse(config.isBlackAi());
  }

  @Test
  @DisplayName("Verify command description")
  void testDescription() {
    CmdAction cmd = cmds.get("set").get().createNew(new String[] {});
    String desc = cmd.getDescription();

    // Check if the description reflects the new mandatory format
    assertTrue(
        desc.contains("set PARAM=VALUE"), "Description should show the correct format: " + desc);
    assertTrue(desc.contains("set aiDepth=5"), "Description should provide a valid example");
  }

  @Test
  @DisplayName("check correct behavior with different possibilities --something -a --longopt")
  void testOptionCombinations() {
    // no predictions for variables -> requires to show help from user
    Terminal term = new FakeTerminal(new ByteArrayOutputStream());
    CmdSet cmd = new CmdSet(this.gameUserInterface, this.config);
    Completer completer = cmd.getAutoCompleter();
    FakeLineReader lineReader = new FakeLineReader(term);
    lineReader.setCompleter(completer);
    ArrayList<String> opts =
        new ArrayList<>(
            Arrays.asList("whiteIsAi=true ", "", "debug", "ai", "time", "time=5 aiMode=MCTS ", ""));
    String uinput;
    List<Candidate> candidates;
    List<Candidate> candidatesResult;
    final String[][] comparator = {
      {
        "verbose",
        "debug",
        "blitzMode",
        "timeout",
        "aiActive",
        "aiMode",
        "aiDepth",
        "aiTimeLimit",
        "aiIterativeDeepening",
        "aiHeuristic",
        "whiteIsAi",
        "blackIsAi"
      },
      {
        "verbose",
        "debug",
        "blitzMode",
        "timeout",
        "aiActive",
        "aiMode",
        "aiDepth",
        "aiTimeLimit",
        "aiIterativeDeepening",
        "aiHeuristic",
        "whiteIsAi",
        "blackIsAi"
      },
      {"debug"},
      {"aiActive", "aiMode", "aiDepth", "aiTimeLimit", "aiIterativeDeepening", "aiHeuristic"},
      {"timeout"},
      {
        "verbose",
        "debug",
        "blitzMode",
        "timeout",
        "aiActive",
        "aiMode",
        "aiDepth",
        "aiTimeLimit",
        "aiIterativeDeepening",
        "aiHeuristic",
        "whiteIsAi",
        "blackIsAi"
      },
      {}
    };
    short j = 0;
    String curropt;
    for (String opt : opts) {
      uinput = "set " + opt;
      // nothing predicted if bad command name
      if (j == 6) {
        uinput = "something ";
      }
      candidates = lineReader.complete(uinput);
      for (short i = 0; i < comparator[j].length; i++) {
        candidatesResult = candidates.stream().toList();
        short finalI = i; // generated by ide
        short finalJ = j; // generated by ide
        curropt = comparator[finalJ][finalI] + "=";
        String finalCurropt = curropt; // generated by ide
        assertTrue(
            candidates.stream().anyMatch(c -> c.value().equals(finalCurropt)),
            "user input is : (\""
                + uinput
                + "\") out is :"
                + candidatesResult
                + " doesn't contains:"
                + finalCurropt
                + "\"");
      }
      assertEquals(comparator[j].length, candidates.size(), "step: opt n°" + j);
      j++;
    }
  }

  @Test
  @Description("check if the new instance CmdSet have 'set' as name")
  void createNewHasAName() {
    CmdSet cmd = new CmdSet(this.gameUserInterface, this.config);
    assertEquals("set", cmd.createNew(new String[] {"name"}).getName());
  }
}
