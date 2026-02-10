package fr.univ.bordeaux.agonCore.bitboard;

import fr.univ.bordeaux.agonCore.bitboard.Direction;

/**
 * A custom 128-bit bitset implementation using two {@code long} values (low and high).
 * <p>
 * This class provides high-performance bitwise operations tailored for an 11x11
 * hexagonal grid (121 tiles total). It supports logical operations, shifts with
 * carry-over, and morphological operations like dilation.
 * </p>
 */
public class BitBoard {
  /** Bits 0 to 63. */
  private long low;
  /** Bits 64 to 127 (Agon uses up to 120). */
  private long high;

  /**
   * Constructs an empty BitBoard with all bits set to 0.
   */
  public BitBoard() {
    this.low = 0L;
    this.high = 0L;
  }

  /**
   * Constructs a BitBoard with a single bit set at the given index.
   *
   * @param index The bit index to set (0 to 120).
   */
  public BitBoard(int index) {
    if (index > 63) {
      this.high |= 1L << (index - 64);
    } else {
      this.low |= 1L << index;
    }
  }

  /**
   * Performs a bitwise OR operation with another BitBoard.
   *
   * @param bitBoard2 The second operand.
   * @return A new BitBoard representing the union of both boards.
   */
  public BitBoard orOperation(BitBoard bitBoard2) {
    BitBoard bitBoard3 = new BitBoard();
    bitBoard3.low = this.low | bitBoard2.low;
    bitBoard3.high = this.high | bitBoard2.high;
    return bitBoard3;
  }

  /**
   * Performs a bitwise AND operation with another BitBoard.
   *
   * @param bitBoard2 The second operand.
   * @return A new BitBoard representing the intersection of both boards.
   */
  public BitBoard andOperation(BitBoard bitBoard2) {
    BitBoard bitBoard3 = new BitBoard();
    bitBoard3.low = this.low & bitBoard2.low;
    bitBoard3.high = this.high & bitBoard2.high;
    return bitBoard3;
  }

  /**
   * Performs a bitwise NOT operation.
   *
   * @return A new BitBoard with all bits inverted.
   */
  public BitBoard complementOperation() {
    BitBoard bitBoard = new BitBoard();
    bitBoard.low = ~this.low;
    bitBoard.high = ~this.high;
    return bitBoard;
  }

  /**
   * Sets or clears the bit at a specific index.
   *
   * @param index The bit index (0 to 120).
   * @param value Use 1L to set the bit to 1, or 0L to clear it to 0.
   */
  public void setBit(int index, long value) {
    if (index > 63) {
      int shift = index - 64;
      if (value == 1L) {
        this.high |= (1L << shift);
      } else {
        this.high &= ~(1L << shift);
      }
    } else {
      if (value == 1L) {
        this.low |= (1L << index);
      } else {
        this.low &= ~(1L << index);
      }
    }
  }

  /**
   * Checks if the bit at the given index is set to 1.
   *
   * @param index The bit index.
   * @return {@code true} if the bit is 1; {@code false} otherwise.
   */
  public boolean isSet(int index) {
    if (index < 64) {
      return (this.low & (1L << index)) != 0;
    } else {
      return (this.high & (1L << (index - 64))) != 0;
    }
  }

  /**
   * Shifts the entire 128-bit structure in a given direction.
   * <p>
   * This method handles the carry-over between the {@code low} and {@code high}
   * segments to ensure bit continuity during shifts.
   * </p>
   *
   * @param n The shift magnitude (negative for right shift, positive for left shift).
   * @return A new shifted BitBoard.
   */
  public BitBoard shiftBitboard(int n) {
    BitBoard shiftedBitBoard = new BitBoard();
    if (n > 0) {
      // SHIFT UP: Bits move toward higher indices
      shiftedBitBoard.high = (this.high << n) | (this.low >>> (64 - n));
      shiftedBitBoard.low = (this.low << n);
      return shiftedBitBoard;
    } else if (n < 0) {
      // SHIFT DOWN: Bits move toward lower indices
      int s = -n;
      shiftedBitBoard.low = (this.low >>> s) | (this.high << (64 - s));
      shiftedBitBoard.high = (this.high >>> s);
      return shiftedBitBoard;
    }
    return this;
  }

  /**
   * Checks if all bits on the board are 0.
   *
   * @return {@code true} if the board is empty.
   */
  public boolean isEmpty() {
    return (this.low == 0 && this.high == 0);
  }

  /**
   * Compares this BitBoard with another for equality.
   *
   * @param bitBoard The board to compare against.
   * @return {@code true} if both boards have identical bits set.
   */
  public boolean equals(BitBoard bitBoard) {
    return this.low == bitBoard.low && this.high == bitBoard.high;
  }

  /**
   * Returns the total number of bits set to 1.
   *
   * @return The population count (Hamming weight) of the board.
   */
  public int countBits() {
    return Long.bitCount(low) + Long.bitCount(high);
  }

  /**
   * Performs a morphological dilation on the current bitboard.
   * <p>
   * This creates a new board where every tile adjacent to an existing piece
   * is set to 1. Useful for calculating neighbors or influence zones.
   * </p>
   *
   * @return A BitBoard representing the union of shifts in all 6 directions.
   */
  public BitBoard dilation() {
    BitBoard dilatedBoard = new BitBoard();
    for (Direction d : Direction.values()) {
      dilatedBoard = dilatedBoard.orOperation(this.shiftBitboard(d.getValue()));
    }
    return dilatedBoard;
  }

  /**
   * Efficiently finds the index of the next bit set to 1 after a given position.
   * <p>
   * This method uses {@code Long.numberOfTrailingZeros} for high-performance
   * bit scanning, which is essential for move generation loops.
   * </p>
   *
   * @param currentBit The index to start scanning from (exclusive). Use -1 to find the first bit.
   * @return The index of the next set bit, or -1 if no more bits are found.
   */
  public int nextSetBit(int currentBit) {
    int start = currentBit + 1;
    if (start >= 121) return -1;

    // Check the low part (0-63)
    if (start < 64) {
      long maskLow = low & (-1L << start);
      if (maskLow != 0) {
        return Long.numberOfTrailingZeros(maskLow);
      }
      start = 64;
    }

    // Check the high part (64-120)
    long maskHigh = high & (-1L << (start - 64));
    if (maskHigh != 0) {
      return 64 + Long.numberOfTrailingZeros(maskHigh);
    }

    return -1;
  }
}