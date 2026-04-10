package fr.univ.bordeaux.agoncore.bitboard;

import fr.univ.bordeaux.technical.utils.GameLogger;

/**
 * A high-performance 128-bit bitset implementation optimized for Agon's hexagonal grid.
 *
 * <p>Using two {@code long} primitives ({@code low} and {@code high}), this class represents the
 * 121 tiles of the Agon board as a linear sequence of bits. It provides near-instantaneous bitwise
 * operations for move generation, adjacency calculations, and pattern matching.
 */
public class BitBoard {

  /** Bits 0 to 63 (Lower half of the board). */
  private long low;

  /** Bits 64 to 127 (Upper half of the board; Agon uses up to index 120). */
  private long high;

  /** Constructs an empty BitBoard with all bits initialized to zero (0L). */
  public BitBoard() {
    this.low = 0L;
    this.high = 0L;
  }

  /**
   * Constructs a BitBoard with a single bit set at the specified index.
   *
   * @param index The bit position to set (0 to 120).
   */
  public BitBoard(int index) {
    if (index > 63) {
      this.high |= 1L << (index - 64);
    } else {
      this.low |= 1L << index;
    }
  }

  /**
   * Copy constructor. Creates a new BitBoard with the same state as the provided one.
   *
   * @param board The {@link BitBoard} to replicate.
   */
  public BitBoard(final BitBoard board) {
    this.low = board.low;
    this.high = board.high;
  }

  /**
   * Performs a bitwise OR (Union) operation between two bitboards.
   *
   * @param bitBoard2 The second operand for the OR operation.
   * @return A new {@link BitBoard} containing bits set in either board.
   */
  public BitBoard orOperation(final BitBoard bitBoard2) {
    final BitBoard bitBoard3 = new BitBoard();
    bitBoard3.low = this.low | bitBoard2.low;
    bitBoard3.high = this.high | bitBoard2.high;
    return bitBoard3;
  }

  /**
   * Performs a bitwise AND (Intersection) operation between two bitboards.
   *
   * @param bitBoard2 The second operand for the AND operation.
   * @return A new {@link BitBoard} containing only bits set in both boards.
   */
  public BitBoard andOperation(final BitBoard bitBoard2) {
    final BitBoard bitBoard3 = new BitBoard();
    bitBoard3.low = this.low & bitBoard2.low;
    bitBoard3.high = this.high & bitBoard2.high;
    return bitBoard3;
  }

  /**
   * Performs a bitwise NOT (Inversion) operation on the current board.
   *
   * @return A new {@link BitBoard} with all bits flipped (1 becomes 0 and vice-versa).
   */
  public BitBoard complementOperation() {
    final BitBoard bitBoard = new BitBoard();
    bitBoard.low = ~this.low;
    bitBoard.high = ~this.high;
    return bitBoard;
  }

  /**
   * Sets or clears the bit at a specific tile index.
   *
   * @param index The bit index (0 to 120).
   * @param value {@code 1L} to set the bit, {@code 0L} to clear it.
   */
  public void setBit(final int index, final long value) {
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
    if (GameLogger.isDebugEnabled()) {
      GameLogger.debug("BitBoard: bit " + index + " set to " + value);
    }
  }

  /**
   * Checks the status of the bit at the given index.
   *
   * @param index The bit position to query.
   * @return {@code true} if the bit is 1, {@code false} if 0.
   */
  public boolean isSet(final int index) {
    if (index < 64) {
      return (this.low & (1L << index)) != 0;
    } else {
      return (this.high & (1L << (index - 64))) != 0;
    }
  }

  /**
   * Shifts the entire bitboard content in a specific direction.
   *
   * <p>This method maintains bit continuity across the 64-bit boundary by calculating the
   * carry-over between the {@code low} and {@code high} segments using unsigned shifts.
   *
   * @param n The shift offset. Positive moves bits toward higher indices, negative toward lower.
   * @return A new shifted {@link BitBoard}.
   */
  public BitBoard shiftBitboard(final int n) {
    BitBoard shiftedBitBoard = new BitBoard();
    if (n > 0) {
      shiftedBitBoard.high = (this.high << n) | (this.low >>> (64 - n));
      shiftedBitBoard.low = (this.low << n);
    } else if (n < 0) {
      int s = -n;
      shiftedBitBoard.low = (this.low >>> s) | (this.high << (64 - s));
      shiftedBitBoard.high = (this.high >>> s);
    } else {
      shiftedBitBoard = this;
    }
    if (GameLogger.isDebugEnabled()) {
      GameLogger.debug("BitBoard: performing shift of " + n);
    }
    return shiftedBitBoard;
  }

  /**
   * Checks if no bits are set on this bitboard.
   *
   * @return {@code true} if all bits are 0, meaning the board or layer is empty.
   */
  public boolean isEmpty() {
    return (this.low == 0 && this.high == 0);
  }

  /**
   * Compares this BitBoard with another object for equality.
   *
   * @param obj The object to compare against.
   * @return {@code true} if the other object is a BitBoard with identical bit states.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    BitBoard other = (BitBoard) obj;
    return this.low == other.low && this.high == other.high;
  }

  /**
   * Returns the total count of set bits, also known as Hamming weight or population count.
   *
   * @return The number of set bits (occupied tiles) on this board.
   */
  public int countBits() {
    return Long.bitCount(low) + Long.bitCount(high);
  }

  /**
   * Expands the current bitboard state to include all adjacent hexagonal neighbors.
   *
   * <p>Technically, this performs a morphological dilation using a hexagonal structuring element.
   * It is used to find all reachable or surrounding tiles in a single pass.
   *
   * @return A new {@link BitBoard} representing the dilated area (original bits + neighbors).
   */
  public BitBoard dilation() {
    GameLogger.debug("BitBoard: calculating dilation (hexagonal neighbors)...");
    BitBoard dilatedBoard = new BitBoard();
    for (Direction d : Direction.values()) {
      dilatedBoard = dilatedBoard.orOperation(this.shiftBitboard(d.getValue()));
    }
    return dilatedBoard;
  }

  /**
   * Scans the bitboard for the next set bit after a given index.
   *
   * <p>This uses the CPU-optimized {@code Long.numberOfTrailingZeros} to find bits rapidly, which
   * is critical for efficient move generation loops.
   *
   * @param currentBit The index to start scanning from (exclusive). Use {@code -1} for the start.
   * @return The index of the next set bit, or {@code -1} if none remain.
   */
  public int nextSetBit(final int currentBit) {
    int start = currentBit + 1;
    if (start >= 121) {
      return -1;
    }

    if (start < 64) {
      long maskLow = low & (-1L << start);
      if (maskLow != 0) {
        return Long.numberOfTrailingZeros(maskLow);
      }
      start = 64;
    }

    long maskHigh = high & (-1L << (start - 64));
    if (maskHigh != 0) {
      return 64 + Long.numberOfTrailingZeros(maskHigh);
    }

    return -1;
  }

  /**
   * Synchronizes this bitboard's state with another without creating a new object.
   *
   * @param bitBoard The source {@link BitBoard} to copy from.
   * @return This {@link BitBoard} instance after the update.
   */
  public BitBoard copy(BitBoard bitBoard) {
    this.low = bitBoard.low;
    this.high = bitBoard.high;
    return this;
  }

  /**
   * * Returns a string representation of the raw bits (low:high) for hashing or logging purposes.
   *
   * @return A {@link String} formatted as "lowPart:highPart".
   */
  public String getRawValueString() {
    return this.low + ":" + this.high;
  }
}
