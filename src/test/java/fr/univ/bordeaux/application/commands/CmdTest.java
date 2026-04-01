package fr.univ.bordeaux.application.commands;

import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeParsedLine;
import fr.univ.bordeaux.ui.cli.tools.FakeShell;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import jdk.jfr.Description;
import org.apache.commons.cli.Option;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * test remaining sections/lines that are not tested in specialized commands
 */
public class CmdTest {

  // requires 6 "Mock" classes for all those tests

  private FakeCmd fakeCmd;
  private Terminal term;
  private FakeParsedLine fpl;

  @BeforeEach
  void setup() throws Exception {
    this.term = new FakeTerminal(new ByteArrayOutputStream()); // user input is "-"
    this.fpl = new FakeParsedLine();
    var registrer = new AgonRegister<CmdAction>();
    var shell = new FakeShell(this.term, new LineReaderImpl(this.term), registrer);
    this.fakeCmd = new FakeCmd(shell);
    this.fakeCmd.addOption(new Option("b", "hey", false, null));
    this.fakeCmd.addOption(new Option("h", "help", false, null));
    this.fakeCmd.addOption(new Option("m", null));
    this.fakeCmd.addOption(new Option("r", "reset", false, null));
  }

  @Test
  @Description("check prediction for completeness single character or word")
  void checkWordCompleter() throws Exception {
    String userInputLine;
    String msg;
    Completer completer = this.fakeCmd.getAutoCompleter();
    List<Candidate> candidates = new ArrayList<>();
    completer.complete(new LineReaderImpl(this.term), this.fpl, candidates);
    assertFalse(
            candidates.isEmpty(),
            "completer must give -b, -h, -m, --hey, --help"
    );
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("-b")));
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("-h")));
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("-m")));
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("-r")));
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("--hey")));
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("--help")));
    assertTrue(candidates.stream().anyMatch(c -> c.value().equals("--reset")));
    assertEquals(7, candidates.size());
    // test including differentiation (--hel should predict only --help)
    // (--he should predict both --hey and --help)
    candidates.clear();
    FakeLineReader lineReader = new FakeLineReader(this.term);
    lineReader.setCompleter(completer);
    String opt = "--he";
    userInputLine = "cmd " +opt;
    candidates = lineReader.complete(userInputLine);
    StringBuilder finalSb = new StringBuilder();
    StringBuilder finalSb2 = finalSb;
    candidates.forEach(c -> finalSb2.append(c.value()).append(" "));
    assertFalse(
            candidates.stream().anyMatch(c -> c.value().equals("-h")),
            "candidate for \"-h\" must not contain -h but it is: (" + finalSb + ")"
    );
    assertFalse(
            candidates.stream().anyMatch(c -> c.value().equals("--reset")),
            "candidate for \"--reset\" must not contain -h but it is: (" + finalSb + ")"
    );
    assertTrue(
            candidates.stream().anyMatch(c -> c.value().equals("--hey")),
            "options: (" + finalSb + ") must contain : \"--hey\""
    );
    assertTrue(
            candidates.stream().anyMatch(c -> c.value().equals("--help")),
            "options: (" + finalSb + ") must contain : \"--help\""
    );
    assertEquals(2, candidates.size());
    opt = "-h";
    userInputLine = "cmd " +opt;
    candidates = lineReader.complete(userInputLine);
    finalSb = new StringBuilder();
    StringBuilder finalSb1 = finalSb; // ide don't like internal variables
    candidates.forEach(c -> finalSb1.append(c.value()).append(" "));
    msg = "\"candidate for %s must contain %s but is not: (%s)";
    assertTrue(
            candidates.stream().anyMatch(c -> c.value().equals("-h")),
            String.format(msg, opt, "-h", finalSb)
    );
    msg = "\"candidate for %s must not contain %s but it is: (%s)";
    assertFalse(
            candidates.stream().anyMatch(c -> c.value().equals("-r")),
            String.format(msg, opt, "-r", finalSb)
    );
    assertFalse(
            candidates.stream().anyMatch(c -> c.value().equals("-m")),
            String.format(msg, opt, "-m", finalSb)
    );
    assertFalse(
            candidates.stream().anyMatch(c -> c.value().equals("--reset")),
            String.format(msg, opt, "--reset", finalSb)
    );
    msg = "options: (%s) must not contain : \"%s\"";
    assertFalse(
            candidates.stream().anyMatch(c -> c.value().equals("--hey")),
            String.format(msg, finalSb, "--hey")
    );
    assertFalse(
            candidates.stream().anyMatch(c -> c.value().equals("--help")),
            String.format(msg, finalSb, "--help")
    );
    assertEquals(1, candidates.size(), "only option allowed is -h");
  }

  @Test
  @Description("get only long options as result with correct number")
  void testLongOptionsOnly(){
    String opt = "--";
    String userInputLine = "cmd " + opt;
    Completer completer = this.fakeCmd.getAutoCompleter();
    FakeLineReader lineReader = new FakeLineReader(this.term);
    lineReader.setCompleter(completer);
    List<Candidate> candidates = lineReader.complete(userInputLine);
    StringBuilder finalSb = new StringBuilder();
    candidates.forEach(c -> finalSb.append(c.value()).append(" "));
    assertFalse(
            candidates.stream().anyMatch(c -> c.value().equals("-h")),
            "candidate for \"" + opt + "\" must not contain -h but it is: (" + finalSb + ")"
    );
    assertFalse(
            candidates.stream().anyMatch(c -> c.value().equals("-m")),
            "candidate for \"" + opt + "\" must not contain -m but it is: (" + finalSb + ")"
    );
    assertFalse(
            candidates.stream().anyMatch(c -> c.value().equals("-m")),
            "candidate for \"" + opt + "\" must not contain -b but it is: (" + finalSb + ")"
    );
    assertTrue(
            candidates.stream().anyMatch(c -> c.value().equals("--hey")),
            "options: (" + finalSb + ")"
    );
    assertTrue(
            candidates.stream().anyMatch(c -> c.value().equals("--help")),
            "options: (" + finalSb + ")"
    );
    assertTrue(
            candidates.stream().anyMatch(c -> c.value().equals("--reset")),
            "options: (" + finalSb + ")"
    );
    assertEquals(3, candidates.size());
  }

  @Test
  @Description("when input look like : --something -a_thing --something_else ...")
  void testMultipleOptions(){



  }

  @Test
  @Description("")
  void checkWordsCompleter(){
    var flr = new FakeLineReader("-");
  }

  @Test
  @Description("when user word cannot be predicted (need F19)")
  void checkErrorSound(){
    // tested in terminal it's okay


  }



}
