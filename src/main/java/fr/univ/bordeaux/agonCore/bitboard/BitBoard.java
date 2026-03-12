package fr.univ.bordeaux.agonCore.bitboard;

/**
 * A high-performance 128-bit bitset implementation optimized for Agon's hexagonal grid.
 *
 * <p>Using two {@code long} primitives ({@code low} and {@code high}), this class represents the
 * 121 tiles of the Agon board as a linear sequence of bits. It provides near-instantaneous
 * bitwise operations for move generation, adjacency calculations, and pattern matching.</p>
 *
 *
 */
public class BitBoard {

  /** Bits 0 to 63 (Lower half of the board). */
  private long low;

  /** Bits 64 to 127 (Upper half of the board; Agon uses up to index 120). */
  private long high;

  /** * Constructs an empty BitBoard with all bits initialized to zero.
   */
  public BitBoard() {
    this.low = 0L;
    this.high = 0L;
  }

  public BitBoard(long low, long high) {this.low = low; this.high = high;}  public BitBoard copy2() {
    return new BitBoard(this.low, this.high);
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
  public BitBoard(BitBoard board) {
    this.low = board.low;
    this.high = board.high;
  }

  /**
   * Performs a bitwise OR (Union) operation.
   *
   * @param bitBoard2 The second operand.
   * @return A new {@link BitBoard} containing bits set in either board.
   */
  public BitBoard orOperation(BitBoard bitBoard2) {
    BitBoard bitBoard3 = new BitBoard();
    bitBoard3.low = this.low | bitBoard2.low;
    bitBoard3.high = this.high | bitBoard2.high;
    return bitBoard3;
  }

  /**
   * Performs a bitwise AND (Intersection) operation.
   *
   * @param bitBoard2 The second operand.
   * @return A new {@link BitBoard} containing only bits set in both boards.
   */
  public BitBoard andOperation(BitBoard bitBoard2) {
    BitBoard bitBoard3 = new BitBoard();
    bitBoard3.low = this.low & bitBoard2.low;
    bitBoard3.high = this.high & bitBoard2.high;
    return bitBoard3;
  }

  /**
   * Performs a bitwise NOT (Inversion) operation.
   *
   * @return A new {@link BitBoard} with all bits flipped.
   */
  public BitBoard complementOperation() {
    BitBoard bitBoard = new BitBoard();
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
   * Checks the status of the bit at the given index.
   *
   * @param index The bit position to query.
   * @return {@code true} if the bit is 1, {@code false} if 0.
   */
  public boolean isSet(int index) {
    if (index < 64) {
      return (this.low & (1L << index)) != 0;
    } else {
      return (this.high & (1L << (index - 64))) != 0;
    }
  }

  /**
   * Shifts the entire bitboard content in a specific direction.
   *
   * <p>This method maintains bit continuity across the 64-bit boundary by
   * calculating the carry-over between the {@code low} and {@code high} segments.</p>
   *
   * @param n The shift offset (corresponds to {@link Direction#getValue()}).
   * Positive moves bits toward higher indices, negative toward lower.
   * @return A new shifted {@link BitBoard}.
   */
  public BitBoard shiftBitboard(int n) {
    BitBoard shiftedBitBoard = new BitBoard();
    if (n > 0) {
      shiftedBitBoard.high = (this.high << n) | (this.low >>> (64 - n));
      shiftedBitBoard.low = (this.low << n);
      return shiftedBitBoard;
    } else if (n < 0) {
      int s = -n;
      shiftedBitBoard.low = (this.low >>> s) | (this.high << (64 - s));
      shiftedBitBoard.high = (this.high >>> s);
      return shiftedBitBoard;
    }
    return this;
  }

  /**
   * Checks if no pieces are present on this bitboard.
   *
   * @return {@code true} if all bits are 0.
   */
  public boolean isEmpty() {
    return (this.low == 0 && this.high == 0);
  }

  /**
   * Compares this BitBoard with another object for equality.
   *
   * @param bitBoard The board to compare against.
   * @return {@code true} if both boards have the same bits set.
   */
  public boolean equals(BitBoard bitBoard) {
    return this.low == bitBoard.low && this.high == bitBoard.high;
  }

  /**
   * Returns the total count of set bits (Hamming weight).
   *
   * @return The number of pieces or occupied tiles on this board.
   */
  public int countBits() {
    return Long.bitCount(low) + Long.bitCount(high);
  }

  /**
   * Expands the current bitboard state to include all adjacent hexagonal neighbors.
   *
   * <p>Technically, this performs a morphological dilation using a hexagonal
   * structuring element. It is used to find all reachable or surrounding tiles.</p>
   * @return A new {@link BitBoard} representing the dilated area.
   */
  public BitBoard dilation() {
    BitBoard dilatedBoard = new BitBoard();
    for (Direction d : Direction.values()) {
      dilatedBoard = dilatedBoard.orOperation(this.shiftBitboard(d.getValue()));
    }
    return dilatedBoard;
  }

  /**
   * Scans the bitboard for the next set bit after a given index.
   *
   * <p>This uses the CPU-optimized {@code Long.numberOfTrailingZeros} to find
   * pieces rapidly, which is critical for efficient move generation loops.</p>
   *
   * @param currentBit The index to start scanning from (exclusive). Use {@code -1} for the start.
   * @return The index of the next set bit, or {@code -1} if none remain.
   */
  public int nextSetBit(int currentBit) {
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
   * @param bitBoard The source {@link BitBoard}.
   * @return This {@link BitBoard} after the update.
   */
  public BitBoard copy(BitBoard bitBoard) {
    this.low = bitBoard.low;
    this.high = bitBoard.high;
    return this;
  }
}