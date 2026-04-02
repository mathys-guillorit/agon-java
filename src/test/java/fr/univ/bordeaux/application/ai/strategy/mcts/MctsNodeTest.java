package fr.univ.bordeaux.application.ai.strategy.mcts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Test suite for the {@link MctsNode} data structure. */
class MctsNodeTest {

  private Move move1;
  private Move move2;
  private Move move3;
  private List<Move> legalMoves;

  @BeforeEach
  void setUp() {
    move1 = new Move(0, 1, Color.WHITE, null);
    move2 = new Move(2, 3, Color.WHITE, null);
    move3 = new Move(4, 5, Color.WHITE, null);
    legalMoves = Arrays.asList(move1, move2, move3);
  }

  @Test
  @DisplayName("Should initialize correctly with correct state")
  void testInitialization() {
    MctsNode root = new MctsNode(null, null, Color.WHITE, legalMoves);

    assertNull(root.getParent(), "Root parent should be null");
    assertNull(root.getMove(), "Root move should be null");
    assertEquals(Color.WHITE, root.getPlayerToMove(), "Player to move should match");
    assertTrue(root.isLeaf(), "A new node should be a leaf (no children)");
    assertFalse(root.isFullyExpanded(), "A new node with legal moves should not be fully expanded");
    assertEquals(0, root.getVisitCount(), "Initial visit count should be 0");
    assertEquals(0.0, root.getWinScore(), "Initial win score should be 0.0");
  }

  @Test
  @DisplayName("Should update statistics correctly after rollouts")
  void testUpdateStats() {
    MctsNode node = new MctsNode(null, null, Color.WHITE, legalMoves);

    node.updateStats(1.0);
    assertEquals(1, node.getVisitCount());
    assertEquals(1.0, node.getWinScore());

    node.updateStats(0.0);
    assertEquals(2, node.getVisitCount());
    assertEquals(1.0, node.getWinScore());

    node.updateStats(0.5);
    assertEquals(3, node.getVisitCount());
    assertEquals(1.5, node.getWinScore());
  }

  @Test
  @DisplayName("Should correctly pop random untried moves until fully expanded")
  void testPopRandomUntriedMove() {
    MctsNode node = new MctsNode(null, null, Color.WHITE, legalMoves);
    Random fixedRandom = new Random(42);

    Move popped1 = node.popRandomUntriedMove(fixedRandom);
    assertNotNull(popped1);
    assertTrue(legalMoves.contains(popped1));
    assertFalse(node.isFullyExpanded());

    Move popped2 = node.popRandomUntriedMove(fixedRandom);
    assertNotNull(popped2);
    assertNotEquals(popped1, popped2);

    Move popped3 = node.popRandomUntriedMove(fixedRandom);
    assertNotNull(popped3);
    assertTrue(node.isFullyExpanded(), "Node should be fully expanded after all moves are popped");

    Move popped4 = node.popRandomUntriedMove(fixedRandom);
    assertNull(popped4, "Popping from fully expanded node should return null");
  }

  @Test
  @DisplayName("Should manage children correctly")
  void testChildManagement() {
    MctsNode root = new MctsNode(null, null, Color.WHITE, legalMoves);
    MctsNode child = new MctsNode(root, move1, Color.BLACK, Arrays.asList(move2, move3));

    root.addChild(child);

    assertFalse(root.isLeaf(), "Root should no longer be a leaf after adding a child");
    assertEquals(1, root.getChildren().size(), "Root should have exactly 1 child");
    assertEquals(child, root.getChildren().get(0), "The child should match the one added");
    assertEquals(root, child.getParent(), "Child's parent reference should point back to root");
  }
}
