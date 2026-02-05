package fr.univ.bordeaux.agonCore.bitboard;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.PieceType;
import fr.univ.bordeaux.agonCore.agonElements.Move;
public class BitBoard implements AgonBoard {

  private long[] whiteQueen = new long[2];
  private long[] blackQueen = new long[2];
  private long[] whitePawns = new long[2];
  private long[] blackPawns = new long[2];
  private long[] validZoneMask = new long[2];
  private long[][] circles = new long[6][2];
  long[][] allowedDestinations = new long[6][2];
  private int whitePawnsToRelocate = 0;
  private int blackPawnsToRelocate = 0;
  private boolean whiteQueenToRelocate = false;
  private boolean blackQueenToRelocate = false;
  private int THRONE = 60;

  /**
   * Default constructor. Initializes an empty board and pre-calculates valid zones and concentric
   * circles.
   */
  public BitBoard() {
    initCirclesAndValidZones();
    initAllowedDestinations();
  }

  /**
   * Parameterized constructor to initialize the board with a specific state. * @param whiteQueen
   * Bitboard for the white queen.
   *
   * @param blackQueen Bitboard for the black queen.
   * @param whitePawns Bitboard for the white pawns.
   * @param blackPawns Bitboard for the black pawns.
   */
  public BitBoard(long[] whiteQueen, long[] blackQueen, long[] whitePawns, long[] blackPawns) {
    this.whiteQueen = whiteQueen;
    this.blackQueen = blackQueen;
    this.whitePawns = whitePawns;
    this.blackPawns = blackPawns;
    initCirclesAndValidZones();
    initAllowedDestinations();
  }
  /**
   * Applies a move or a piece relocation on the board using a Move object.
   * * Logic flow:
   * 1. Checks if the player has a Queen to relocate.
   * 2. Checks if the player has Pawns to relocate.
   * 3. If no relocations are pending, performs a standard move from one tile to another.
   *
   * @param move The Move object containing the source (from), destination (to), and player color.
   * @return 1 if the move was valid and successfully applied, -1 otherwise.
   */
  public int applyMove(Move move) {
    int from = move.getFrom();
    int to = move.getTo();
    Color color = move.getColor();

    // 1. Queen Relocation Case (Priority 1)
    if (isQueenRelocating(color)) {
      setBit(getQueenTable(color), to, 1L);
      if (color == Color.WHITE) {
        whiteQueenToRelocate = false;
      } else {
        blackQueenToRelocate = false;
      }
      performCaptures(color);
      return 1;
    }

    // 2. Pawn Relocation Case (Priority 2)
    if (isPawnRelocating(color)) {
      setBit(getPawnsTable(color), to, 1L);
      if (color == Color.WHITE) {
        whitePawnsToRelocate--;
      } else {
        blackPawnsToRelocate--;
      }
      performCaptures(color);
      return 1;
    }

    // 3. Normal Move Case
    // Performs the required move if it is valid and checks for captures
    if (isValid(from, to, color)) {
      long[] pieceTable = getTabWhereIndexIsOn(from);

      if (pieceTable != null) {
        // Remove piece from origin and place it at destination
        setBit(pieceTable, from, 0L);
        setBit(pieceTable, to, 1L);

        // Check if this move triggers any captures
        performCaptures(color);
        return 1;
      }
    }

    return -1;
  }
  /**
   * Combines queen and pawns bitboards for a specific color to find occupied tiles.
   *
   * @param color The color to check.
   * @return A bitboard representing all pieces of that color.
   */
  public long[] getOccupiedBy(Color color) {
    long[] queen = (color == Color.WHITE) ? whiteQueen : blackQueen;
    long[] pawns = (color == Color.WHITE) ? whitePawns : blackPawns;
    long[] occupied = new long[2];
    occupied[0] |= queen[0] | pawns[0];
    occupied[1] |= queen[1] | pawns[1];
    return occupied;
  }

  /**
   * Detects and processes pieces captured by the current player. Captured pieces are removed from
   * the board and added to the enemy's relocation queue.
   *
   * @param playerColor The color of the player who just moved.
   */
  public void performCaptures(Color playerColor) {
    Color enemyColor = (playerColor == Color.WHITE) ? Color.BLACK : Color.WHITE;

    long[] pawns = getPawnsTable(enemyColor);
    long[] queen = getQueenTable(enemyColor);

    //get every tiles where enemyColor can be captured
    long[] capturedMask = getSuicideMask(enemyColor);

    //verify for if queen is there
    if (((capturedMask[0] & queen[0]) != 0) || ((capturedMask[1] & queen[1]) != 0)) {
      if (enemyColor == Color.WHITE) {
        whiteQueenToRelocate = true;
      } else {
        blackQueenToRelocate = true;
      }

      queen[0] &= ~capturedMask[0];
      queen[1] &= ~capturedMask[1];
    }

    //now we count how many pawns are captured
    long pLow = capturedMask[0] & pawns[0];
    long pHigh = capturedMask[1] & pawns[1];
    int count = Long.bitCount(pLow) + Long.bitCount(pHigh);

    if (count > 0) {
      if (enemyColor == Color.WHITE) {
        whitePawnsToRelocate += count;
      } else {
        blackPawnsToRelocate += count;
      }

      pawns[0] &= ~capturedMask[0];
      pawns[1] &= ~capturedMask[1];
    }
  }

  /**
   * Determines if a player has won the game. Victory occurs when the Queen is on the throne and
   * surrounded by 6 pawns.
   *
   * @param color The color to check for victory.
   * @return true if the player has won.
   */
  public boolean isGameWon(Color color) {
    long[] queen = (color == Color.WHITE) ? whiteQueen : blackQueen;
    long[] pawns = (color == Color.WHITE) ? whitePawns : blackPawns;
    boolean queenOnThrone = (circles[0][0] & queen[0]) != 0 || (circles[0][1] & queen[1]) != 0;
    boolean arePawnsSurroundingThrone = (pawns[0] & circles[1][0]) == circles[1][0]
        && (pawns[1] & circles[1][1]) == circles[1][1];
    return arePawnsSurroundingThrone && queenOnThrone;
  }

  /**
   * Calculates the neighboring tiles for a single index.
   *
   * @param index Tile index.
   * @return A bitboard containing the 6 adjacent tiles.
   */
  public long[] getNeighbors(int index) {
    long[] indexMask = new long[2];
    long[] neighborMask = new long[2];
    if (index < 64) {
      indexMask[0] = 1L << index;
      indexMask[1] = 0L;
    } else {
      indexMask[0] = 0L;
      indexMask[1] = 1L << (index - 64);
    }
    for (Direction d : Direction.values()) {
      long[] res = shiftBitboard(indexMask[0], indexMask[1], d.getValue());
      neighborMask[0] |= res[0];
      neighborMask[1] |= res[1];
    }
    return neighborMask;
  }

  /**
   * Prints a bitboard mask in an hexagonal shape for console debugging.
   *
   * @param bitboard The bitboard to visualize.
   */
  public void printMask(long[] bitboard) {
    for (int r = 10; r >= 0; r--) {
      int numSpaces = Math.abs(5 - r);
      for (int s = 0; s < numSpaces; s++) {
        System.out.print(" ");
      }

      for (int c = 0; c < 11; c++) {
        int idx = r * 11 + c;

        if (!isSet(validZoneMask, idx)) {
          continue;
        }

        if (isSet(bitboard, idx)) {
          System.out.print("1 ");
        } else {
          System.out.print(". ");
        }
      }
      System.out.println();
    }
  }

  /**
   * Prints the current state of the board in an hexagonal layout for debugging.
   */
  public void printBoard() {
    for (int r = 10; r >= 0; r--) {
      int numSpaces = Math.abs(5 - r);
      for (int s = 0; s < numSpaces; s++) {
        System.out.print(" ");
      }
      for (int c = 0; c < 11; c++) {
        int idx = r * 11 + c;

        if (!isSet(validZoneMask, idx)) {
          continue;
        }

        if (isSet(whiteQueen, idx)) {
          System.out.print("Q ");
        } else if (isSet(blackQueen, idx)) {
          System.out.print("q ");
        } else if (isSet(whitePawns, idx)) {
          System.out.print("O ");
        } else if (isSet(blackPawns, idx)) {
          System.out.print("X ");
        } else if (idx == THRONE) {
          System.out.print("+ ");
        } else {
          System.out.print(". ");
        }
      }
      System.out.println();
    }
  }

  /**
   * Generates all legal moves for a player at the start of their turn. Accounts for relocation
   * priority, circle-based restrictions, and suicide prevention. * @param color The player color.
   *
   * @return A bitboard of all legal destination tiles.
   */
  public long[] generateLegalMoves(Color color) {
    if (hasPiecesToRelocate(color)) {
      return getRelocationMoves(color);
    }
    long[] legalMoves = new long[2];
    long[] freeZones = getFreeZones();
    long queenLow = 0L;
    long queenHigh = 0L;
    long[] pawns = new long[2];
    long pOnCircleI_Low;
    long pOnCircleI_High;
    if (color == Color.WHITE) {
      pawns[0] = whitePawns[0];
      pawns[1] = whitePawns[1];
      queenLow = whiteQueen[0];
      queenHigh = whiteQueen[1];
    } else {
      pawns[0] = blackPawns[0];
      pawns[1] = blackPawns[1];
      queenLow = blackQueen[0];
      queenHigh = blackQueen[1];
    }
    for (int i = 1; i <= 5; i++) {
      pOnCircleI_Low = pawns[0] & circles[i][0];
      pOnCircleI_High = pawns[1] & circles[i][1];

      if (pOnCircleI_Low != 0 || pOnCircleI_High != 0) {
        // 1. On génère tous les voisins physiques (les 6 cases autour)
        long[] neighbors = getAllNeighbors(pOnCircleI_Low, pOnCircleI_High);

        // 2. On applique le masque de non-recul spécifique au cercle i
        long legalLow = neighbors[0] & allowedDestinations[i][0];
        long legalHigh = neighbors[1] & allowedDestinations[i][1];

        // 3. On retire le trône pour les pions (seule la Reine y va)
        legalLow &= ~circles[0][0];
        legalHigh &= ~circles[0][1];

        // 4. On retire les cases déjà occupées
        legalLow &= freeZones[0];
        legalHigh &= freeZones[1];

        // On ajoute ces coups à la liste globale
        legalMoves[0] |= legalLow;
        legalMoves[1] |= legalHigh;
      }
    }
    if (queenLow != 0 || queenHigh != 0) {
      for (int i = 0; i < 6; i++) {
        if ((circles[i][0] & queenLow) != 0L || (circles[i][1] & queenHigh) != 0L) {
          long[] queenNeighbors = getAllNeighbors(queenLow, queenHigh);
          queenNeighbors[0] &= validZoneMask[0];
          queenNeighbors[1] &= validZoneMask[1];
          legalMoves[0] |= (queenNeighbors[0] & allowedDestinations[i][0] & freeZones[0]);
          legalMoves[1] |= (queenNeighbors[1] & allowedDestinations[i][1] & freeZones[1]);
        }
      }
    }
    long[] suicideMask = getSuicideMask(color);
    legalMoves[0] &= ~suicideMask[0];
    legalMoves[1] &= ~suicideMask[1];
    return legalMoves;
  }
  public PieceType getPieceAt(int index) {
    if (index < 0 || index > 120) {
      return null;
    }
    if (isSet(whiteQueen, index)) {
      return PieceType.WHITE_QUEEN;
    }
    if (isSet(blackQueen, index)) {
      return PieceType.BLACK_QUEEN;
    }
    if (isSet(whitePawns, index)) {
      return PieceType.WHITE_PAWN;
    }
    if (isSet(blackPawns, index)) {
      return PieceType.BLACK_PAWN;
    }
    return null;
  }
  /**
   * Retrieves the bitboard associated with the queen of the specified color.
   *
   * @param color The target color.
   * @return A long array of size 2.
   */
  private long[] getQueenTable(Color color) {
    return (color == Color.WHITE) ? whiteQueen : blackQueen;
  }

  /**
   * Retrieves the bitboard associated with the pawns of the specified color.
   *
   * @param color The target color.
   * @return A long array of size 2.
   */
  private long[] getPawnsTable(Color color) {
    return (color == Color.WHITE) ? whitePawns : blackPawns;
  }

  /**
   * Helper to find which piece type is at a given index.
   *
   * @param index The index to search.
   * @return The bitboard array containing the piece, or null if empty.
   */
  private long[] getTabWhereIndexIsOn(int index) {
    if (index < 0 || index > 120) {
      return null;
    }
    if (isSet(whiteQueen, index)) {
      return whiteQueen;
    }
    if (isSet(blackQueen, index)) {
      return blackQueen;
    }
    if (isSet(whitePawns, index)) {
      return whitePawns;
    }
    if (isSet(blackPawns, index)) {
      return blackPawns;
    }
    return null;
  }



  /**
   * Generates a bitboard mask of tiles where a piece of the specified color would be sandwiched. A
   * sandwich is formed when a tile is flanked by two enemy threats on opposite sides. * @param
   * color The color for which to check potential captures.
   *
   * @return A bitboard mask of capture zones.
   */
  private long[] getSuicideMask(Color color) {
    Color enemyColor = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    //get every enemy position to projet them in all directions
    long[] Occupied = getOccupiedBy(enemyColor);
    long[] totalSuicideMask = new long[2];
    //Project the pawns in all directions and it's opposite if a tile is seen in both's shifted board it's a suicideTile
    for (Direction d1 : Direction.values()) {
      Direction d2 = Direction.getOpposite(d1);

      long[] shift1 = shiftBitboard(Occupied[0], Occupied[1], d1.getValue());
      long[] shift2 = shiftBitboard(Occupied[0], Occupied[1], d2.getValue());

      totalSuicideMask[0] |= (shift1[0] & shift2[0]);
      totalSuicideMask[1] |= (shift1[1] & shift2[1]);
    }
    //We clean the result with the validZone
    totalSuicideMask[0] &= validZoneMask[0];
    totalSuicideMask[1] &= validZoneMask[1];

    return totalSuicideMask;
  }

  /**
   * Generates a bitboard representing all empty tiles within the board boundaries.
   *
   * @return A bitboard of free zones.
   */
  private long[] getFreeZones() {
    long[] freeZones = new long[2];
    long occupiedLow = whiteQueen[0] | blackQueen[0] | whitePawns[0] | blackPawns[0];
    long occupiedHigh = whiteQueen[1] | blackQueen[1] | whitePawns[1] | blackPawns[1];
    freeZones[0] = ~occupiedLow & validZoneMask[0];
    freeZones[1] = ~occupiedHigh & validZoneMask[1];
    return freeZones;
  }

  /**
   * Combines all piece bitboards to show every occupied tile on the board.
   *
   * @return A bitboard of total occupancy.
   */
  private long[] getOccupiedTotal() {
    long[] total = new long[2];

    // On combine les Reines et les Pions des deux couleurs
    total[0] = whiteQueen[0] | blackQueen[0] | whitePawns[0] | blackPawns[0];
    total[1] = whiteQueen[1] | blackQueen[1] | whitePawns[1] | blackPawns[1];

    return total;
  }

  /**
   * Internal logic for calculating legal relocation moves.
   *
   * @param color       The player color.
   * @param allowedZone The geometric zone allowed for relocation (e.g., Circle 5).
   * @param isQueen     True if calculating for a queen relocation.
   * @return A bitboard of legal relocation destinations.
   */
  private long[] getRelocationMovesInternal(Color color, long[] allowedZone, boolean isQueen) {
    long[] legalMoves = new long[2];
    long[] occupied = getOccupiedTotal();
    long[] suicide = getSuicideMask(color);

    // 1. On part de la zone autorisée (Cercle 5 pour pions, ValidZone pour Reine)
    legalMoves[0] = allowedZone[0];
    legalMoves[1] = allowedZone[1];

    // 2. On retire les cases déjà occupées
    legalMoves[0] &= ~occupied[0];
    legalMoves[1] &= ~occupied[1];

    // 3. On retire les cases suicidaires
    legalMoves[0] &= ~suicide[0];
    legalMoves[1] &= ~suicide[1];

    // 4. Cas spécifique : La reine ne peut jamais retourner sur le trône (60)
    if (isQueen) {
      legalMoves[0] &= ~(1L << THRONE);
    }

    return legalMoves;
  }

  /**
   * Determines legal relocation moves based on game priority (Queen first).
   *
   * @param color The player color.
   * @return A bitboard of valid relocation spots.
   */
  private long[] getRelocationMoves(Color color) {
    if (color == Color.WHITE ? whiteQueenToRelocate : blackQueenToRelocate) {
      // Reine : Zone valide complète
      return getRelocationMovesInternal(color, validZoneMask, true);
    } else {
      // Pions : Uniquement le cercle 5
      return getRelocationMovesInternal(color, circles[5], false);
    }
  }

  /**
   * Internal neighbor generation for multiple pieces without boundary filtering.
   */
  private long[] getAllNeighborsInternal(long low, long high) {
    long[] neighbors = new long[2];
    for (Direction d : Direction.values()) {
      long[] res = shiftBitboard(low, high, d.getValue());
      neighbors[0] |= res[0];
      neighbors[1] |= res[1];
    }
    // Ici, on ne fait PAS de "& validZoneMask" !
    return neighbors;
  }

  /**
   * Internal neighbor generation with valid board zone filtering.
   */
  private long[] getAllNeighbors(long low, long high) {
    long[] neighbors = new long[2];

    for (Direction d : Direction.values()) {
      long[] res = shiftBitboard(low, high, d.getValue());
      neighbors[0] |= res[0];
      neighbors[1] |= res[1];
    }
    neighbors[0] &= validZoneMask[0];

    neighbors[1] &= validZoneMask[1];
    return neighbors;
  }

  /**
   * Sets or clears a specific bit in a bitboard.
   *
   * @param mask  The bitboard to modify.
   * @param index The bit index (0-120).
   * @param value 1L to set, 0L to clear.
   */
  private void setBit(long[] mask, int index, long value) {
    if (index > 63) {
      int shift = index - 64;
      if (value == 1L) {
        mask[1] |= (1L << shift);
      } else {
        mask[1] &= ~(1L << shift);
      }
    } else {
      if (value == 1L) {
        mask[0] |= (1L << index);
      } else {
        mask[0] &= ~(1L << index);
      }
    }

  }

  /**
   * Validates if a move is physically possible (adjacent and not moving backwards/outwards).
   *
   * @param from  Start index.
   * @param to    Destination index.
   * @param color Player color.
   * @return true if the move follows game rules.
   */
  private boolean isValid(int from, int to, Color color) {
    if (isFree(to) && isAdjacent(from, to)) {
      long[] maskTo = createMaskWithOneElement(to);
      long[] legalMoves = generateLegalMoves(color);
      return (maskTo[0] & legalMoves[0]) != 0 || (maskTo[1] & legalMoves[1]) != 0;
    }
    return false;
  }

  /**
   * Checks if the queen of the given color is currently waiting to be relocated.
   *
   * @param color The color to check.
   * @return true if the queen is captured and off-board.
   */
  private boolean isQueenRelocating(Color color) {
    if (color == Color.WHITE) {
      return whiteQueenToRelocate;
    } else {
      return blackQueenToRelocate;
    }
  }

  /**
   * Checks if any pawns of the given color are currently waiting to be relocated.
   *
   * @param color The color to check.
   * @return true if there is at least one pawn in the relocation queue.
   */
  private boolean isPawnRelocating(Color color) {
    if (color == Color.WHITE) {
      return whitePawnsToRelocate > 0;
    } else {
      return blackPawnsToRelocate > 0;
    }
  }

  /**
   * Checks if two tiles are adjacent on the hexagonal grid.
   *
   * @param from Start index.
   * @param to   Target index.
   * @return true if they are neighbors.
   */
  private boolean isAdjacent(int from, int to) {
    long[] fromNeighbors = getNeighbors(from);
    if (to < 64) {
      return (fromNeighbors[0] & (1L << to)) != 0;
    } else {
      return (fromNeighbors[1] & (1L << (to - 64))) != 0;
    }
  }

  /**
   * Checks if a tile is within the valid board area and unoccupied.
   *
   * @param index Tile index.
   * @return true if the tile is available for a piece.
   */
  private boolean isFree(int index) {
    long indexMask = 0L;
    long occupiedZone = 0L;
    if (index > 63) {
      indexMask |= 1L << (index - 64);
      occupiedZone = whiteQueen[1] | blackQueen[1] | whitePawns[1] | blackPawns[1];
      return (validZoneMask[1] & indexMask) != 0 && (occupiedZone & indexMask) == 0;
    } else {
      indexMask |= 1L << index;
      occupiedZone = whiteQueen[0] | blackQueen[0] | whitePawns[0] | blackPawns[0];
      return (validZoneMask[0] & indexMask) != 0 && (occupiedZone & indexMask) == 0;
    }
  }

  /**
   * Checks if a specific bit is set to 1 in a bitboard.
   *
   * @param bitboard Target bitboard.
   * @param index    Bit index.
   * @return true if bit is 1.
   */
  private boolean isSet(long[] bitboard, int index) {
    if (index < 64) {
      return (bitboard[0] & (1L << index)) != 0;
    } else {
      return (bitboard[1] & (1L << (index - 64))) != 0;
    }
  }

  /**
   * Checks if the player has any captured pieces that must be returned to the board.
   *
   * @param color The player color.
   * @return true if a piece is waiting for relocation.
   */
  private boolean hasPiecesToRelocate(Color color) {
    if (color == Color.WHITE) {
      return whiteQueenToRelocate || whitePawnsToRelocate > 0;
    }
    return blackQueenToRelocate || blackPawnsToRelocate > 0;
  }

  /**
   * Creates a bitboard with a single bit set at the specified index.
   *
   * @param index The index (0-120).
   * @return A bitboard with one bit active.
   */
  private long[] createMaskWithOneElement(int index) {
    long[] mask = new long[2];
    if (index > 63) {
      mask[1] |= 1L << (index - 64);
    } else {
      mask[0] |= 1L << index;
    }
    return mask;
  }

  /**
   * Shifts a 128-bit bitboard in a given direction. Automatically handles carry-over between the
   * low and high 64-bit segments. * @param low Lower 64 bits.
   *
   * @param high Higher 64 bits.
   * @param n    Shift magnitude (Direction value).
   * @return Shifted bitboard array.
   */
  private long[] shiftBitboard(long low, long high, int n) {
    if (n > 0) {
      // ON MONTE : On décale tout vers les index supérieurs (vers long 1)
      long newHigh = (high << n) | (low >>> (64 - n));
      long newLow = (low << n);
      return new long[]{newLow, newHigh};
    } else if (n < 0) {
      // ON DESCEND : On décale tout vers les index inférieurs (vers long 0)
      int s = -n; // on rend le décalage positif pour les opérateurs
      long newLow = (low >>> s) | (high << (64 - s));
      long newHigh = (high >>> s);
      return new long[]{newLow, newHigh};
    }
    return new long[]{low, high}; // n = 0
  }

  /**
   * Pre-calculates allowed movement zones based on the current circle to prevent moving away from
   * center.
   */
  private void initAllowedDestinations() {
    //A piece can move only forward or on the sides
    for (int i = 5; i >= 1; i--) {
      for (int j = i; j >= i - 1; j--) {
        allowedDestinations[i][0] |= circles[j][0];
        allowedDestinations[i][1] |= circles[j][1];
      }
    }
  }

  /**
   * Pre-calculates concentric circles and valid board boundaries.
   */
  private void initCirclesAndValidZones() {
    //starting with THRONE (center)
    circles[0][0] = 1L << THRONE;
    circles[0][1] = 0L;
    validZoneMask[0] = circles[0][0];
    validZoneMask[1] = circles[0][1];
    //create others circles using expansion methode
    for (int i = 1; i < 6; i++) {
      long[] expansion = getAllNeighborsInternal(circles[i - 1][0], circles[i - 1][1]);

      //remove tiles that are parts of the last processed circle
      expansion[0] &= ~validZoneMask[0];
      expansion[1] &= ~validZoneMask[1];

      circles[i] = expansion;

      //add the circle to the validZone
      validZoneMask[0] |= circles[i][0];
      validZoneMask[1] |= circles[i][1];
    }
  }
}
