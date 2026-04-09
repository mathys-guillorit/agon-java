package fr.univ.bordeaux.ui;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.agoncore.bitboard.BitBoard;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;
import fr.univ.bordeaux.application.commands.specialized.CmdShow;
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.cli.AgonShell;
import fr.univ.bordeaux.ui.cli.tools.FakeLineReader;
import fr.univ.bordeaux.ui.cli.tools.FakeTerminal;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UiPromptParserTest {
  private AgonRegister<CmdAction> registry;
  private AgonShell shell;
  private ByteArrayOutputStream outContent;
  private GameConfig config;

  @BeforeEach
  void setUp() throws Exception {
    registry = new AgonRegister<>();
    outContent = new ByteArrayOutputStream();
    config = new GameConfig();
    Terminal terminal = new FakeTerminal(outContent);
    LineReader reader = new FakeLineReader("");

    shell = new AgonShell(terminal, reader, registry);
    registry.register("show", new CmdShow(shell, config));
  }

  /**
   * Verifies that the parser correctly identifies a registered command when running through the
   * real AgonShell context.
   */
  @Test
  @DisplayName("Integration: Parse 'show' command through AgonShell")
  void testParseCommandInShell() {
    CmdAction action = UiPromptParser.parse("show", registry, shell);

    assertNotNull(action, "The action should not be null for a registered command");
    assertTrue(action instanceof CmdShow, "The action should be an instance of CmdShow");
  }

  /**
   * Verifies that the parser correctly handles a standard move string. Checks if the coordinate
   * mapping works within the shell context.
   */
  @Test
  @DisplayName("Integration: Parse move 'a1b2' through AgonShell")
  void testParseMoveInShell() {
    CmdAction action = UiPromptParser.parse("a1b2", registry, shell);

    assertNotNull(action, "The action should not be null for a valid move string");
    assertInstanceOf(CmdMove.class, action, "The action should be a CmdMove");
  }

  /**
   * Verifies that an invalid coordinate correctly triggers the styled [ERROR] output of the real
   * AgonShell.
   */
  @Test
  @DisplayName("Integration: Out of bounds move triggers AgonShell error styling")
  void testOutOfBoundsErrorInShell() {
    CmdAction action = UiPromptParser.parse("k15", registry, shell);

    assertNull(action, "The action should be null for an invalid coordinate");

    String output = outContent.toString();
    assertTrue(output.contains("ERROR"), "Output should contain 'ERROR'");
    assertTrue(
        output.contains("out of bounds"), "Output should explain the error is 'out of bounds'");
  }

  /** Verifies that the parser is case-insensitive when using real shell inputs. */
  @Test
  @DisplayName("Integration: Case insensitivity in AgonShell")
  void testCaseInsensitivityInShell() {
    assertNotNull(UiPromptParser.parse("SHOW", registry, shell), "Should handle uppercase 'SHOW'");
    assertNotNull(
        UiPromptParser.parse("F1G1", registry, shell), "Should handle uppercase moves like 'F1G1'");
  }

  /** Verifies parsing of a relocation move (single coordinate) after a capture. */
  @Test
  @DisplayName("Integration: Parse relocation move 'a1' through AgonShell")
  void testRelocationMove() {
    BitBoard whitePawns = new BitBoard();
    whitePawns.setBit(61, 1L);
    whitePawns.setBit(64, 1L);
    BitBoard blackPawns = new BitBoard(62);

    AgonBoardImpl captureBoard =
        new AgonBoardImpl(new BitBoard(), new BitBoard(), whitePawns, blackPawns);
    captureBoard.applyMove(new Move(64, 63, Color.WHITE));
    captureBoard.performCaptures(Color.WHITE, new ArrayList<>());

    CmdAction action = UiPromptParser.parse("a1", registry, shell);

    assertNotNull(action, "The action should not be null for a relocation move");
    assertInstanceOf(CmdMove.class, action, "Relocation should be parsed as a CmdMove");
  }
}
