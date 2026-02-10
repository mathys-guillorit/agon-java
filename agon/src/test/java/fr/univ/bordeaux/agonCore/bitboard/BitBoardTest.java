package fr.univ.bordeaux.agonCore.bitboard;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour la classe BitBoard. Vérifie la manipulation des 128 bits sur deux segments
 * de 64 bits.
 */
class BitBoardTest {

  @Test
  @DisplayName("Test de l'initialisation par index")
  void testConstructorWithIndex() {
    BitBoard bbLow = new BitBoard(10);
    BitBoard bbHigh = new BitBoard(70);

    assertTrue(bbLow.isSet(10));
    assertFalse(bbLow.isSet(70));
    assertTrue(bbHigh.isSet(70));
    assertFalse(bbHigh.isSet(10));
  }

  @Test
  @DisplayName("Test de setBit et isSet (limites low/high)")
  void testSetBitBoundaries() {
    BitBoard bb = new BitBoard();

    bb.setBit(0, 1L); // Premier bit de low
    bb.setBit(63, 1L); // Dernier bit de low
    bb.setBit(64, 1L); // Premier bit de high
    bb.setBit(120, 1L); // Limite du plateau Agon

    assertTrue(bb.isSet(0));
    assertTrue(bb.isSet(63));
    assertTrue(bb.isSet(64));
    assertTrue(bb.isSet(120));
    assertFalse(bb.isSet(32));
  }

  @Test
  @DisplayName("Test des opérations logiques (OR, AND, NOT)")
  void testLogicalOperations() {
    BitBoard b1 = new BitBoard(10);
    BitBoard b2 = new BitBoard(20);

    // OR
    BitBoard orRes = b1.orOperation(b2);
    assertTrue(orRes.isSet(10) && orRes.isSet(20));

    // AND
    BitBoard andRes = orRes.andOperation(b1);
    assertTrue(andRes.isSet(10));
    assertFalse(andRes.isSet(20));

    // NOT (Complement)
    BitBoard empty = new BitBoard();
    BitBoard full = empty.complementOperation();
    assertTrue(full.isSet(0));
    assertTrue(full.isSet(120));
  }

  @Test
  @DisplayName("Test du décalage (shift) avec passage de low vers high")
  void testShiftUpCarryOver() {
    BitBoard bb = new BitBoard(63); // Dernier bit de low

    // Décalage de +1 vers le haut
    BitBoard shifted = bb.shiftBitboard(1);

    assertTrue(shifted.isSet(64), "Le bit aurait dû passer de l'index 63 à 64 (low -> high)");
    assertFalse(shifted.isSet(63), "L'ancien bit à 63 devrait être vide");
  }

  @Test
  @DisplayName("Test du décalage (shift) avec passage de high vers low")
  void testShiftDownCarryOver() {
    BitBoard bb = new BitBoard(64); // Premier bit de high

    // Décalage de -1 vers le bas
    BitBoard shifted = bb.shiftBitboard(-1);

    assertTrue(shifted.isSet(63), "Le bit aurait dû passer de l'index 64 à 63 (high -> low)");
    assertFalse(shifted.isSet(64), "L'ancien bit à 64 devrait être vide");
  }

  @Test
  @DisplayName("Test de countBits")
  void testCountBits() {
    BitBoard bb = new BitBoard();
    bb.setBit(5, 1L);
    bb.setBit(65, 1L);
    bb.setBit(120, 1L);

    assertEquals(3, bb.countBits());
  }

  @Test
  @DisplayName("Test de nextSetBit pour itération")
  void testNextSetBit() {
    BitBoard bb = new BitBoard();
    bb.setBit(10, 1L);
    bb.setBit(60, 1L);
    bb.setBit(100, 1L);

    int first = bb.nextSetBit(-1);
    int second = bb.nextSetBit(first);
    int third = bb.nextSetBit(second);
    int fourth = bb.nextSetBit(third);

    assertEquals(10, first);
    assertEquals(60, second);
    assertEquals(100, third);
    assertEquals(-1, fourth);
  }

  @Test
  @DisplayName("Test de dilation (voisins)")
  void testDilation() {
    int index = 60; // center
    BitBoard bb = new BitBoard(index);
    BitBoard dilated = bb.dilation();

    // Vérifie que les voisins (selon les offsets de Direction) sont activés
    // East(+1), West(-1), NE(+12), NW(+11), SE(-11), SW(-12)
    assertTrue(dilated.isSet(index + Direction.East.getValue()));
    assertTrue(dilated.isSet(index + Direction.West.getValue()));
    assertTrue(dilated.isSet(index + Direction.NorthEast.getValue()));
    assertTrue(dilated.isSet(index + Direction.SouthWest.getValue()));
    assertTrue(dilated.isSet(index + Direction.SouthEast.getValue()));
    assertTrue(dilated.isSet(index + Direction.SouthWest.getValue()));

    // Le centre lui-même ne doit pas être activé par dilation (sauf si shifté sur lui-même)
    assertFalse(dilated.isSet(index));
  }
}
