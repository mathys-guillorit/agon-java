package fr.univ.bordeaux.agoncore.bitboard;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the BitBoard class. Verifies the manipulation of 128 bits across two 64-bit
 * segments (low and high).
 */
class BitBoardTest {

  @Test
  @DisplayName("Test initialization by index")
  void testConstructorWithIndex() {
    BitBoard bbLow = new BitBoard(10);
    BitBoard bbHigh = new BitBoard(70);

    assertTrue(bbLow.isSet(10));
    assertFalse(bbLow.isSet(70));
    assertTrue(bbHigh.isSet(70));
    assertFalse(bbHigh.isSet(10));
  }

  @Test
  @DisplayName("Test setBit and isSet (low/high boundaries)")
  void testSetBitBoundaries() {
    BitBoard bb = new BitBoard();

    bb.setBit(0, 1L); // First bit of 'low'
    bb.setBit(63, 1L); // Last bit of 'low'
    bb.setBit(64, 1L); // First bit of 'high'
    bb.setBit(120, 1L); // Agon board limit

    assertTrue(bb.isSet(0));
    assertTrue(bb.isSet(63));
    assertTrue(bb.isSet(64));
    assertTrue(bb.isSet(120));
    assertFalse(bb.isSet(32));
  }

  @Test
  @DisplayName("Test logical operations (OR, AND, NOT)")
  void testLogicalOperations() {
    BitBoard b1 = new BitBoard(10);
    BitBoard b2 = new BitBoard(20);

    // OR operation
    BitBoard orRes = b1.orOperation(b2);
    assertTrue(orRes.isSet(10) && orRes.isSet(20));

    // AND operation
    BitBoard andRes = orRes.andOperation(b1);
    assertTrue(andRes.isSet(10));
    assertFalse(andRes.isSet(20));

    // NOT operation (Complement)
    BitBoard empty = new BitBoard();
    BitBoard full = empty.complementOperation();
    assertTrue(full.isSet(0));
    assertTrue(full.isSet(120));
  }

  @Test
  @DisplayName("Test bit shift with carry-over from low to high")
  void testShiftUpCarryOver() {
    BitBoard bb = new BitBoard(63); // Last bit of 'low' segment

    // Shift by +1 (Upwards)
    BitBoard shifted = bb.shiftBitboard(1);

    assertTrue(shifted.isSet(64), "The bit should have moved from index 63 to 64 (low -> high)");
    assertFalse(shifted.isSet(63), "The original bit at index 63 should now be empty");
  }

  @Test
  @DisplayName("Test bit shift with carry-over from high to low")
  void testShiftDownCarryOver() {
    BitBoard bb = new BitBoard(64); // First bit of 'high' segment

    // Shift by -1 (Downwards)
    BitBoard shifted = bb.shiftBitboard(-1);

    assertTrue(shifted.isSet(63), "The bit should have moved from index 64 to 63 (high -> low)");
    assertFalse(shifted.isSet(64), "The original bit at index 64 should now be empty");
  }

  @Test
  @DisplayName("Test bit counting")
  void testCountBits() {
    BitBoard bb = new BitBoard();
    bb.setBit(5, 1L);
    bb.setBit(65, 1L);
    bb.setBit(120, 1L);

    assertEquals(3, bb.countBits());
  }

  @Test
  @DisplayName("Test nextSetBit for iteration")
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
  @DisplayName("Test dilation (neighbor generation)")
  void testDilation() {
    int index = 60; // Center position
    BitBoard bb = new BitBoard(index);
    BitBoard dilated = bb.dilation();

    // Check that neighbors (based on Direction offsets) are activated:
    // East(+1), West(-1), NE(+12), NW(+11), SE(-11), SW(-12)
    assertTrue(dilated.isSet(index + Direction.EAST.getValue()));
    assertTrue(dilated.isSet(index + Direction.WEST.getValue()));
    assertTrue(dilated.isSet(index + Direction.NORTH_EAST.getValue()));
    assertTrue(dilated.isSet(index + Direction.NORTH_WEST.getValue()));
    assertTrue(dilated.isSet(index + Direction.SOUTH_EAST.getValue()));
    assertTrue(dilated.isSet(index + Direction.SOUTH_WEST.getValue()));

    // The center bit itself should not be activated by dilation alone
    assertFalse(dilated.isSet(index));
  }
}
