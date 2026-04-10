package fr.univ.bordeaux.application.match.player;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoardImpl;
import fr.univ.bordeaux.application.ai.strategy.AgonAi;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.commands.specialized.CmdMove;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AiPlayerTest {

  private AgonBoard board;
  private AiPlayer aiPlayer;
  private Move fakeBestMove;

  /** Simple stub for AI strategy to avoid running complex algorithms during tests. */
  private class StubAi implements AgonAi {
    @Override
    public Move getBestMove(AgonBoard board) {
      return fakeBestMove;
    }

    @Override
    public void setTimeLimit(long millis) {}
  }

  @BeforeEach
  void setUp() {
    board = new AgonBoardImpl();
    fakeBestMove = new Move(1, 10, Color.WHITE);
    AgonAi stubAi = new StubAi();

    aiPlayer = new AiPlayer("AlphaAgon", Color.WHITE, board, stubAi);
  }

  @Test
  @DisplayName("Verify basic attributes (Name, Color, Type)")
  void testBasicAttributes() {
    assertEquals("AlphaAgon", aiPlayer.getName());
    assertEquals(Color.WHITE, aiPlayer.getColor());
  }

  @Test
  @DisplayName("getAction should return a CmdMove containing the AI's best move")
  void testGetAction() {
    AgonRegister<CmdAction> cmds = new AgonRegister<>();
    CmdAction action = aiPlayer.getAction(cmds);

    assertNotNull(action, "The action should not be null");
    assertTrue(action instanceof CmdMove, "The returned action should be an instance of CmdMove");
  }
}
