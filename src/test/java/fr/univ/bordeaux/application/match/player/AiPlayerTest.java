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

  // Implémentation factice (Stub) de AgonAi pour le test
  private class StubAi implements AgonAi {
    @Override
    public Move getBestMove(AgonBoard board) {
      return fakeBestMove;
    }

    @Override
    public void setTimeLimit(long millis) {

    }
  }

  @BeforeEach
  void setUp() {
    board = new AgonBoardImpl();
    fakeBestMove = new Move(1, 10, Color.WHITE);
    AgonAi stubAi = new StubAi();

    aiPlayer = new AiPlayer("AlphaAgon", Color.WHITE, board, stubAi);
  }

  @Test
  @DisplayName("Vérification des attributs de base (Nom, Couleur, Type)")
  void testBasicAttributes() {
    assertEquals("AlphaAgon", aiPlayer.getName());
    assertEquals(Color.WHITE, aiPlayer.getColor());
  }

  @Test
  @DisplayName("getAction doit retourner une CmdMove contenant le meilleur coup de l'IA")
  void testGetAction() {
    // On crée un registre vide pour l'appel
    AgonRegister<CmdAction> cmds = new AgonRegister<>();

    // Appel de la méthode à tester
    CmdAction action = aiPlayer.getAction(cmds);

    // Vérifications
    assertNotNull(action, "L'action ne doit pas être nulle");
    assertTrue(action instanceof CmdMove, "L'action retournée doit être une instance de CmdMove");

  }
}