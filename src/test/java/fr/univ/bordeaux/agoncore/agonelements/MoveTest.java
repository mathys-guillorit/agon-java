package fr.univ.bordeaux.agoncore.agonelements;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MoveTest {

  @Test
  @DisplayName("Test du constructeur simple et des getters")
  void testSimpleConstructor() {
    Move move = new Move(10, 20, Color.WHITE);

    assertEquals(10, move.getFrom());
    assertEquals(20, move.getDestination());
    assertEquals(Color.WHITE, move.getColor());
    assertNull(move.getPieceType(), "Le pieceType doit être null avec ce constructeur");
    assertFalse(move.isRelocationMove(), "from n'est pas -1, ce n'est pas une relocation");
  }

  @Test
  @DisplayName("Test du constructeur complet (avec PieceType)")
  void testFullConstructor() {
    // On suppose que PieceType est une Enum avec PAWN, QUEEN, etc.
    Move move = new Move(5, 15, Color.BLACK, PieceType.BLACK_QUEEN);

    assertEquals(5, move.getFrom());
    assertEquals(15, move.getDestination());
    assertEquals(Color.BLACK, move.getColor());
    assertEquals(PieceType.BLACK_QUEEN, move.getPieceType());
  }

  @Test
  @DisplayName("Vérifier la détection d'un mouvement de relocation")
  void testIsRelocationMove() {
    Move normalMove = new Move(10, 20, Color.WHITE);
    Move relocationMove = new Move(-1, 50, Color.WHITE);

    assertFalse(normalMove.isRelocationMove());
    assertTrue(relocationMove.isRelocationMove(), "Un from à -1 doit être une relocation");
  }

  @Test
  @DisplayName("Test de la méthode equals - branches avec et sans PieceType")
  void testEquals() {
    Move move1 = new Move(10, 20, Color.WHITE);
    Move move2 = new Move(10, 20, Color.WHITE);
    Move move3 = new Move(10, 25, Color.WHITE); // Destination différente
    Move move4 = new Move(10, 20, Color.BLACK); // Couleur différente

    // Cas sans PieceType (null)
    assertEquals(move1, move2, "Deux moves identiques sans PieceType doivent être égaux");
    assertNotEquals(move1, move3, "Destinations différentes");
    assertNotEquals(move1, move4, "Couleurs différentes");

    // Cas avec PieceType
    Move moveWithPiece1 = new Move(10, 20, Color.WHITE, PieceType.WHITE_PAWN);
    Move moveWithPiece2 = new Move(10, 20, Color.WHITE, PieceType.WHITE_PAWN);
    Move moveWithPiece3 = new Move(10, 20, Color.WHITE, PieceType.WHITE_QUEEN);

    assertEquals(moveWithPiece1, moveWithPiece2);
    assertNotEquals(moveWithPiece1, moveWithPiece3, "Même positions mais types différents");
  }

  @Test
  @DisplayName("Test de la méthode toString")
  void testToString() {
    Move move = new Move(1, 2, Color.WHITE, PieceType.WHITE_PAWN);
    String result = move.toString();

    // On vérifie que les informations clés sont présentes dans la String
    assertTrue(result.contains("from: 1"));
    assertTrue(result.contains("to: 2"));
    assertTrue(result.contains("color: WHITE"));
    assertTrue(result.contains("type: WhitePawn"));
  }

  @Test
  @DisplayName("Cas aux limites de equals (Type cast et null)")
  void testEqualsEdgeCases() {
    Move move = new Move(10, 20, Color.WHITE);

    // Vérifier le comportement face à null ou un autre objet (provoque un ClassCastException selon
    // ton code actuel)
    // Note : Ton implémentation actuelle de equals fait (Move) obj directement sans instanceOf.
    // Si tu veux un coverage propre, il faut tester ce qui se passe.

    assertThrows(ClassCastException.class, () -> move.equals("une string"));
    assertThrows(NullPointerException.class, () -> move.equals(null));
  }
}
