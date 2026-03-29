package fr.univ.bordeaux.agoncore.bitboard;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Test suite for the {@link CoordinateMapper} utility class. */
class CoordinateMapperTest {

  @Test
  @DisplayName("Test toCoordinate: Index to String conversion")
  void testToCoordinate() {
    assertEquals("a1", CoordinateMapper.toCoordinate(0), "Index 0 should be 'a1'");
    assertEquals("c3", CoordinateMapper.toCoordinate(24), "Index 24 should be 'c3'");
    assertEquals("f6", CoordinateMapper.toCoordinate(60), "Index 60 (Throne) should be 'f6'");
    assertEquals("k11", CoordinateMapper.toCoordinate(120), "Index 120 should be 'k11'");

    assertEquals("reloc", CoordinateMapper.toCoordinate(-1), "Index -1 should return 'reloc'");
  }

  @Test
  @DisplayName("Test fromCoordinateString: String to Index conversion")
  void testFromCoordinateString() {
    assertEquals(
        24, CoordinateMapper.fromCoordinateString("c3"), "Lowercase 'c3' should map to index 24");
    assertEquals(
        24, CoordinateMapper.fromCoordinateString("C3"), "Uppercase 'C3' should map to index 24");

    assertEquals(
        60, CoordinateMapper.fromCoordinateString("f6"), "'f6' should map to the Throne (60)");
    assertEquals(0, CoordinateMapper.fromCoordinateString("A1"), "'A1' should map to index 0");
  }

  @Test
  @DisplayName("Test Symmetry: Index -> String -> Index")
  void testSymmetry() {
    int[] criticalIndices = {0, 15, 60, 85, 120};

    for (int originalIndex : criticalIndices) {
      String coord = CoordinateMapper.toCoordinate(originalIndex);
      int restoredIndex = CoordinateMapper.fromCoordinateString(coord);

      assertEquals(
          originalIndex,
          restoredIndex,
          "Symmetry failed for index " + originalIndex + " (mapped to " + coord + ")");
    }
  }

  @Test
  @DisplayName("Test returned values")
  void testReturnValues() {
    assertEquals(67, CoordinateMapper.toIndex('G', 2), "'G2' should map to index 67");
    assertEquals(0, CoordinateMapper.toIndex('A', 1), "'A1' should map to index 0");
    assertEquals(
        66,
        CoordinateMapper.toIndex('G', 1),
        "'G1' is not in the game but is valid so should return 66");
    assertEquals(
        -1, CoordinateMapper.toIndex('G', 0), "'G0' is not in the game so should return -1");
  }
}
