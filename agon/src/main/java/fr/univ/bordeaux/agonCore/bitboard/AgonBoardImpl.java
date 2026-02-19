package fr.univ.bordeaux.agonCore.bitboard;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.history.History;
import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.agonElements.PieceType;
import fr.univ.bordeaux.agonCore.history.HistoryInformations;
import java.util.ArrayList;
import java.util.List;
/**
 * Implementation of the Agon board using bitboard.
 *
 * <p>This class manages the game state using several {@link BitBoard} instances for queens and pawns.
 * It handles the core logic of Agon, including the "no-retreating" rule, hexagonal movement,
 * capture processing (sandwiches), and mandatory relocation sequences.</p>
 *
 * <p>The board is structured as 121 tiles organized into 6 concentric circles (0 to 5),
 * where Circle 0 is the central "Throne".</p>
 *
 *
 */
public class AgonBoardImpl implements AgonBoard {

  /** Bitboard for the white queen. */
  private BitBoard whiteQueen;
  /** Bitboard for the black queen. */
  private BitBoard blackQueen;
  /** Bitboard for the white pawns. */
  private BitBoard whitePawns;
  /** Bitboard for the black pawns. */
  private BitBoard blackPawns;
  /** Mask representing all 121 playable tiles on the hexagonal grid. */
  private BitBoard validZoneMask = new BitBoard();
  /** Array of 6 bitboards, each representing one concentric ring (0=center, 5=outer). */
  private BitBoard[] circles = new BitBoard[6];
  /** Pre-calculated masks defining valid destination circles for a piece in circle [i]. */
  BitBoard[] allowedDestinations = new BitBoard[6];
  /** Number of white pawns captured and waiting to return to the board. */
  private int whitePawnsToRelocate = 0;
  /** Number of black pawns captured and waiting to return to the board. */
  private int blackPawnsToRelocate = 0;
  /** Flag indicating if the white queen is captured and must be relocated. */
  private boolean whiteQueenToRelocate = false;
  /** Flag indicating if the black queen is captured and must be relocated. */
  private boolean blackQueenToRelocate = false;
  /** Stack-based history manager for undo/redo operations. */
  private History history = new History();
  /** The constant index of the central tile (Circle 0). */
  private int THRONE = 60;

  /**
   * Initializes a new Agon board with empty bitboards and pre-calculated geometry masks.
   */
  public AgonBoardImpl() {
    this.whiteQueen = new BitBoard();
    this.blackQueen = new BitBoard();
    this.whitePawns = new BitBoard();
    this.blackPawns = new BitBoard();
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
   * @param whiteQueen Bitboard for the white queen
   */
  public AgonBoardImpl(BitBoard whiteQueen, BitBoard blackQueen, BitBoard whitePawns,
      BitBoard blackPawns) {
    this.whiteQueen = whiteQueen;
    this.blackQueen = blackQueen;
    this.whitePawns = whitePawns;
    this.blackPawns = blackPawns;
    initCirclesAndValidZones();
    initAllowedDestinations();
  }

  /**
   * Determines the distance of a specific tile from the center of the board.
   *
   * <p>The method checks which concentric circle contains the given index. In Agon, lower values
   * represent higher centrality (e.g., Circle 0 is the Throne).
   *
   * @param index The tile index (0 to 120).
   * @return The circle index (0 to 5) where 0 is the center; -1 if the index is invalid.
   */
  public int getCentrality(int index) {
    BitBoard centrality = new BitBoard(index);
    BitBoard res;
    for (int i = 0; i < circles.length; i++) {
      res = centrality.andOperation(circles[i]);
      if (!res.isEmpty()) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Calculates the number of immediate legal moves available for a piece at a given index.
   *
   * <p>This is a common heuristic for AI evaluation, representing how "trapped" or "free" a
   * specific piece is based on current board constraints.
   *
   * @param index The tile index of the piece to evaluate.
   * @return The number of neighboring tiles that are currently legal destinations.
   */
  public int getMobility(int index) {
    Color color = getColorWhereIndexIsOn(index);
    if (color == null) {
      return -1;
    }
    BitBoard neighbors = getNeighbors(index);
    int circleIndex = getCentrality(index);
    if (circleIndex == -1) {
      return -1;
    }
    BitBoard allowed = allowedDestinations[circleIndex];
    BitBoard legalMoves = neighbors.andOperation(allowed).andOperation(getFreeZones());
    PieceType type = getPieceAt(index);
    if (type != null && type.isPawn()) {
      legalMoves = legalMoves.andOperation(circles[0].complementOperation());
    }
    BitBoard suicideMask = getSuicideMask(color);
    legalMoves = legalMoves.andOperation(suicideMask.complementOperation());
    return legalMoves.countBits();
  }

  /**
   * Reverts a standard move by moving the piece from its destination back to its origin.
   *
   * <p><b>Note:</b> This method only handles the physical movement of the piece. Complex state
   * changes, such as restoring captured pieces to the board or updating relocation counters, must
   * be managed separately.
   *
   * @return {@code true} if the piece was successfully moved back; {@code false} if no friendly
   * piece was found at the destination.
   */
  public boolean undoMove() {
    if (history.isEmptyUndo()) {
      return false;
    }
    HistoryInformations historyInformations = history.undo();
    List<Move> moves = historyInformations.getMoves();

    for (int i = moves.size() - 1; i >= 0; i--) {
      Move move = moves.get(i);
      Color color = move.getColor();
      PieceType type = move.getPieceType();

      if (move.getFrom() == -1) {
        movePieceInBitboard(move.getTo(), -1, color, type);
        if (type.isPawn()) {
          updatePawnRelocationCount(color, 1);
        } else {
          setQueenRelocating(color, true);
        }
      }
      else if (move.getTo() == -1) {
        movePieceInBitboard(-1, move.getFrom(), color, type);
        if (type.isPawn()) {
          updatePawnRelocationCount(color, -1);
        } else {
          setQueenRelocating(color, false);
        }
      }
      else {
        movePieceInBitboard(move.getTo(), move.getFrom(), color, type);
      }
    }
    return true;
  }
  /**
   * Replays the next move(s) from the redo stack.
   * * <p>This method identifies the color of the next player in the redo history
   * and re-applies all consecutive moves belonging to that same player's turn
   * (e.g., a standard move followed by potential relocations). It updates both
   * the physical bitboards and the internal relocation counters.</p>
   *
   * @return {@code true} if at least one move was re-applied;
   * {@code false} if the redo stack is empty.
   */
  public boolean redoMove() {
    if (history.isEmptyRedo()) {
      return false;
    }
    HistoryInformations historyInformations = history.redo();
    List<Move> moves = historyInformations.getMoves();
    for (Move move : moves) {
      movePieceInBitboard(move.getFrom(),move.getTo(),move.getColor(),move.getPieceType());
    }
    return true;
  }

  /**
   * Calculates a numerical evaluation score for the current board state from the perspective of the
   * specified player.
   *
   * <p>A higher score typically indicates a better position for the player.
   *
   * @param color The {@link Color} of the player to evaluate.
   * @return The heuristic score (currently returns -1 as a placeholder).
   */
  public int getScore(Color color) {
    return -1;
  }

  /**
   * Updates the queen's relocation status for a specific player.
   * * @param color The color of the player.
   * @param state {@code true} if the queen is captured and pending relocation,
   * {@code false} otherwise.
   */
  private void setQueenRelocating(Color color, boolean state) {
    if (color == Color.WHITE) {
      whiteQueenToRelocate = state;
    } else {
      blackQueenToRelocate = state;
    }
  }

  /**
   * Adjusts the count of pawns waiting to be relocated.
   * * @param color The color of the player who owns the captured pawns.
   * @param delta The value to add to the counter (positive for a new capture,
   * negative when a pawn is placed back on the board).
   */
  private void updatePawnRelocationCount(Color color, int delta) {
    if (color == Color.WHITE) {
      whitePawnsToRelocate += delta;
    } else {
      blackPawnsToRelocate += delta;
    }
  }

  /**
   * Performs the physical update of piece positions on the bitboards.
   * * This is a unified method handling three scenarios:
   * <ul>
   * <li>Standard move: Updates both {@code from} and {@code to} indices.</li>
   * <li>Relocation: Only updates {@code to} (set {@code from} to -1).</li>
   * <li>Capture/Removal: Only updates {@code from} (set {@code to} to -1).</li>
   * </ul>
   * * @param from  The starting tile index (use -1 if the piece is coming from the reserve).
   * @param to    The destination tile index (use -1 if the piece is being removed from the board).
   * @param color The color of the piece owner.
   * @param type  The specific type of the piece (Queen or Pawn).
   */
  private void movePieceInBitboard(int from, int to, Color color, PieceType type) {
    BitBoard pieceBoard = (type == PieceType.WHITE_QUEEN || type == PieceType.BLACK_QUEEN)
        ? getQueenTable(color) : getPawnsTable(color);
    if (from != -1) {
      pieceBoard.setBit(from, 0L);
    }
    if (to != -1) {
      pieceBoard.setBit(to, 1L);
    }
  }

  /**
   * Applies a move or a piece relocation on the board using a Move object. * Logic flow: 1. Checks
   * if the player has a Queen to relocate. 2. Checks if the player has Pawns to relocate. 3. If no
   * relocations are pending, performs a standard move from one tile to another.
   *
   * @param move The Move object containing the source (from), destination (to), and player color.
   * @return 1 if the move was valid and successfully applied, -1 otherwise.
   */
  public boolean applyMove(Move move) {
    Color color = move.getColor();
    PieceType type = null;
    List<Move> moves=new ArrayList<>();
    if (isQueenRelocating(color)) {
      type = (color == Color.WHITE) ? PieceType.WHITE_QUEEN : PieceType.BLACK_QUEEN;
    } else if (isPawnRelocating(color)) {
      type = (color == Color.WHITE) ? PieceType.WHITE_PAWN : PieceType.BLACK_PAWN;
    }
    if (type != null) {
      moves.add(new Move(-1,move.getTo(),color,type));
      movePieceInBitboard(-1, move.getTo(), color, type);
      if (type.isQueen()) {
        setQueenRelocating(color, false);
      } else {
        updatePawnRelocationCount(color, -1);
      }
      performCaptures(color,moves);
      history.add(new HistoryInformations(moves,type,color));
      return true;
    }
    if (isValid(move.getFrom(), move.getTo(), color)) {
      type = getPieceAt(move.getFrom());
      if (type != null) {
        moves.add(new Move(move.getFrom(),move.getTo(),color,type));
        movePieceInBitboard(move.getFrom(), move.getTo(), color, type);
        performCaptures(color,moves);
        history.add(new HistoryInformations(moves,type,color));
        return true;
      }
    }
    return false;
  }

  /**
   * Combines queen and pawns bitboards for a specific color to find occupied tiles.
   *
   * @param color The color to check.
   * @return A bitboard representing all pieces of that color.
   */
  public BitBoard getOccupiedBy(Color color) {
    BitBoard queen = (color == Color.WHITE) ? whiteQueen : blackQueen;
    BitBoard pawns = (color == Color.WHITE) ? whitePawns : blackPawns;
    BitBoard occupied = queen.orOperation(pawns);
    return occupied;
  }

  /**
   * Detects and processes pieces captured by the current player. Captured pieces are removed from
   * the board and added to the enemy's relocation queue.
   *
   * @param playerColor The color of the player who just moved.
   * @param moves a list of Move where the function add the captured Move.
   */
  public void performCaptures(Color playerColor,List<Move> moves) {
    Color enemyColor = (playerColor == Color.WHITE) ? Color.BLACK : Color.WHITE;
    BitBoard capturedMask = getSuicideMask(enemyColor);
    BitBoard notCaptured = capturedMask.complementOperation();
    // capture the queen and erase it
    BitBoard queenTable = getQueenTable(enemyColor);
    if (!capturedMask.andOperation(queenTable).isEmpty()) {
      moves.add(new Move(queenTable.nextSetBit(-1),-1,enemyColor,PieceType.getQueen(enemyColor)));
      setQueenRelocating(enemyColor, true);
      queenTable.copy(queenTable.andOperation(notCaptured));
    }
    // capture pawns and erase them
    BitBoard pawnsTable = getPawnsTable(enemyColor);
    BitBoard capturedPawns = capturedMask.andOperation(pawnsTable);
    int count = capturedPawns.countBits();
    if (count > 0) {
      PieceType type=PieceType.getPawn(enemyColor);
      for (int i = capturedPawns.nextSetBit(-1); i != -1; i = capturedPawns.nextSetBit(i)) {
        moves.add(
            new Move(i, -1, enemyColor, type));
      }
      updatePawnRelocationCount(enemyColor, count);
      pawnsTable.copy(pawnsTable.andOperation(notCaptured));
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
    BitBoard queen = (color == Color.WHITE) ? whiteQueen : blackQueen;
    BitBoard pawns = (color == Color.WHITE) ? whitePawns : blackPawns;
    boolean queenOnThrone = (!circles[0].andOperation(queen).isEmpty());
    boolean arePawnsSurroundingThrone = (pawns.andOperation(circles[1]).equals(circles[1]));
    return arePawnsSurroundingThrone && queenOnThrone;
  }

  /**
   * Calculates the neighboring tiles for a single index.
   *
   * @param index Tile index.
   * @return A bitboard containing the 6 adjacent tiles.
   */
  public BitBoard getNeighbors(int index) {
    BitBoard indexMask = new BitBoard(index);
    return indexMask.dilation();
  }

  /**
   * Retrieves the bitboard associated with the queen of the specified color.
   *
   * @param color The target color.
   * @return A long array of size 2.
   */
  private BitBoard getQueenTable(Color color) {
    return (color == Color.WHITE) ? whiteQueen : blackQueen;
  }
  /**
   * Retrieves the bitboard associated with the pawns of the specified color.
   *
   * @param color The target color.
   * @return The Pawns bitboard of the specified color.
   */
  private BitBoard getPawnsTable(Color color) {
    return (color == Color.WHITE) ? whitePawns : blackPawns;
  }

  /**
   * Retrieves the {@link Color} of the player who owns the piece at the specified index.
   *
   * <p>This helper method checks all active bitboards (Queens and Pawns for both players) to
   * determine which player, if any, occupies the given tile. This is essential for calculating
   * mobility, legal moves, and identifying potential capture targets.
   *
   * @param index The tile index (0 to 120) to check.
   * @return The {@link Color} of the piece at the index; {@code null} if the tile is empty or if
   * the index is out of the valid board range.
   */
  private Color getColorWhereIndexIsOn(int index) {
    if (index < 0 || index > 120) {
      return null;
    }
    if (whiteQueen.isSet(index)) {
      return Color.WHITE;
    }
    if (blackQueen.isSet(index)) {
      return Color.BLACK;
    }
    if (whitePawns.isSet(index)) {
      return Color.WHITE;
    }
    if (blackPawns.isSet(index)) {
      return Color.BLACK;
    }
    return null;
  }

  /**
   * Prints a bitboard mask in an hexagonal shape for console debugging.
   *
   * @param bitboard The bitboard to visualize.
   */
  public void printMask(BitBoard bitboard) {
    for (int r = 10; r >= 0; r--) {
      int numSpaces = Math.abs(5 - r);
      for (int s = 0; s < numSpaces; s++) {
        System.out.print(" ");
      }

      for (int c = 0; c < 11; c++) {
        int idx = r * 11 + c;

        if (!validZoneMask.isSet(idx)) {
          continue;
        }

        if (bitboard.isSet(idx)) {
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

        if (!validZoneMask.isSet(idx)) {
          continue;
        }

        if (whiteQueen.isSet(idx)) {
          System.out.print("Q ");
        } else if (blackQueen.isSet(idx)) {
          System.out.print("q ");
        } else if (whitePawns.isSet(idx)) {
          System.out.print("O ");
        } else if (blackPawns.isSet(idx)) {
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
   * Generates a single {@link BitBoard} representing all valid destination tiles for the given
   * player.
   *
   * <p>This method follows a specific hierarchy of rules:
   *
   * <ol>
   *   <li><b>Relocation:</b> If pieces are captured, it returns only the valid relocation spots.
   *   <li><b>Movement:</b> If no pieces are captured, it calculates moves for pawns and the queen.
   *   <li><b>Constraints:</b> Pawns cannot enter the Throne, and no piece can "retreat" (move to a
   *       circle further from the center).
   *   <li><b>Suicide Prevention:</b> Destined tiles that would lead to immediate capture (sandwich)
   *       are filtered out.
   * </ol>
   *
   * @param color The {@link Color} of the player whose legal moves are being generated.
   * @return A {@link BitBoard} where each set bit corresponds to a legal destination tile.
   */
  public BitBoard generateLegalMovesBitboard(Color color) {
    if (hasPiecesToRelocate(color)) {
      return getRelocationMoves(color);
    }
    BitBoard legalMoves = new BitBoard();
    BitBoard freeZones = getFreeZones();
    BitBoard pawns;
    BitBoard queen;
    BitBoard pOnCircleI;
    if (color == Color.WHITE) {
      pawns = whitePawns;
      queen = whiteQueen;
    } else {
      pawns = blackPawns;
      queen = blackQueen;
    }
    for (int i = 1; i <= 5; i++) {
      pOnCircleI = pawns.andOperation(circles[i]);
      if (!pOnCircleI.isEmpty()) {
        BitBoard neighbors = getAllNeighbors(pOnCircleI);
        BitBoard legal =
            neighbors
                .andOperation(allowedDestinations[i])
                .andOperation(circles[0].complementOperation());
        legal = legal.andOperation(freeZones);
        legalMoves = legalMoves.orOperation(legal);
      }
    }
    if (!queen.isEmpty()) {
      for (int i = 0; i < 6; i++) {
        if (!circles[i].andOperation(queen).isEmpty()) {
          BitBoard queenNeighbors = getAllNeighbors(queen);
          queenNeighbors = queenNeighbors.andOperation(validZoneMask);
          legalMoves =
              legalMoves.orOperation(
                  queenNeighbors.andOperation(allowedDestinations[i]).andOperation(freeZones));
        }
      }
    }
    BitBoard suicideMask = getSuicideMask(color);
    legalMoves = legalMoves.andOperation(suicideMask.complementOperation());
    return legalMoves;
  }

  /**
   * Generates a comprehensive list of all legal {@link Move} objects for the current player.
   *
   * <p>This method translates the bitboard-based logic into discrete Move objects. It uses a
   * high-performance bit-scanning approach ({@code nextSetBit}) to iterate through active pieces
   * and their potential destinations.
   *
   * <ul>
   *   <li>If relocation is required, moves will have a source index of {@code -1}.
   *   <li>Pawn moves are restricted by the current circle's allowed destinations and cannot target
   *       the central Throne.
   *   <li>The Queen's moves are restricted by her current circle but include the Throne.
   * </ul>
   *
   * @param color The {@link Color} of the active player.
   * @return A {@link List} of legal {@link Move} objects.
   */
  public List<Move> generateLegalMoves(Color color) {
    List<Move> moves = new ArrayList<>();
    BitBoard suicideMask = getSuicideMask(color);
    // relocate case
    if (hasPiecesToRelocate(color)) {
      BitBoard targets = getRelocationMoves(color);
      // check every bits that is set to 1
      for (int to = targets.nextSetBit(-1); to != -1; to = targets.nextSetBit(to)) {
        moves.add(new Move(-1, to, color)); // -1 are captured pieces
      }
      return moves;
    }
    // normal case
    BitBoard freeZones = getFreeZones();
    BitBoard myPawns = getPawnsTable(color);
    BitBoard myQueen = getQueenTable(color);

    for (int i = 1; i <= 5; i++) {
      // get every pawns in the circle
      BitBoard pOnCircleI = myPawns.andOperation(circles[i]);

      // starting piece index
      for (int from = pOnCircleI.nextSetBit(-1); from != -1; from = pOnCircleI.nextSetBit(from)) {

        // get every valid position for pawns, a valid position means it can't get on throne can't
        // suicide and can't step away from the center.
        BitBoard dests =
            getNeighbors(from)
                .andOperation(allowedDestinations[i])
                .andOperation(circles[0].complementOperation())
                .andOperation(freeZones)
                .andOperation(suicideMask.complementOperation());

        // ending piece index
        for (int to = dests.nextSetBit(-1); to != -1; to = dests.nextSetBit(to)) {
          moves.add(new Move(from, to, color));
        }
      }
    }

    // Do the same for queen
    for (int from = myQueen.nextSetBit(-1); from != -1; from = myQueen.nextSetBit(from)) {
      for (int i = 0; i < 6; i++) {
        if (circles[i].isSet(from)) {
          BitBoard queenDestinations =
              getNeighbors(from)
                  .andOperation(allowedDestinations[i])
                  .andOperation(freeZones)
                  .andOperation(suicideMask.complementOperation());

          for (int to = queenDestinations.nextSetBit(-1);
              to != -1;
              to = queenDestinations.nextSetBit(to)) {
            moves.add(new Move(from, to, color));
          }
          break;
        }
      }
    }

    return moves;
  }

  /**
   * Identifies the type of piece occupying a specific tile on the board.
   *
   * <p>This method checks the internal bitboards for White/Black Queens and Pawns to determine the
   * state of the requested index.
   *
   * @param index The tile index (0 to 120) to inspect.
   * @return The {@link PieceType} present at the index, or {@code null} if the tile is empty or the
   * index is out of bounds.
   */
  public PieceType getPieceAt(int index) {
    if (index < 0 || index > 120) {
      return null;
    }
    if (whiteQueen.isSet(index)) {
      return PieceType.WHITE_QUEEN;
    }
    if (blackQueen.isSet(index)) {
      return PieceType.BLACK_QUEEN;
    }
    if (whitePawns.isSet(index)) {
      return PieceType.WHITE_PAWN;
    }
    if (blackPawns.isSet(index)) {
      return PieceType.BLACK_PAWN;
    }
    return null;
  }

  /**
   * Generates a bitboard mask of tiles where a piece of the specified color would be sandwiched. A
   * sandwich is formed when a tile is flanked by two enemy threats on opposite sides.
   *
   * @param color The color for which to check potential captures.
   * @return A bitboard mask of capture zones.
   */
  private BitBoard getSuicideMask(Color color) {
    Color enemyColor = (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    // get every enemy position to projet them in all directions
    BitBoard Occupied = getOccupiedBy(enemyColor);
    BitBoard totalSuicideMask = new BitBoard();
    // Project the pawns in all directions and it's opposite if a tile is seen in both's shifted
    for (Direction d1 : Direction.values()) {
      Direction d2 = Direction.getOpposite(d1);
      BitBoard shift1 = Occupied.shiftBitboard(d1.getValue());
      BitBoard shift2 = Occupied.shiftBitboard(d2.getValue());
      totalSuicideMask = totalSuicideMask.orOperation(shift1.andOperation(shift2));
    }
    // We clean the result with the validZone

    return totalSuicideMask.andOperation(validZoneMask);
  }

  /**
   * Generates a bitboard representing all empty tiles within the board boundaries.
   *
   * @return A bitboard of free zones.
   */
  private BitBoard getFreeZones() {
    BitBoard occupied =
        whiteQueen.orOperation(blackQueen.orOperation(whitePawns.orOperation(blackPawns)));
    BitBoard freeZones = occupied.complementOperation().andOperation(validZoneMask);
    return freeZones;
  }

  /**
   * Combines all piece bitboards to show every occupied tile on the board.
   *
   * @return A bitboard of total occupancy.
   */
  private BitBoard getOccupiedTotal() {
    BitBoard occupied =
        whiteQueen.orOperation(blackQueen.orOperation(whitePawns.orOperation(blackPawns)));
    return occupied;
  }

  /**
   * Internal logic for calculating legal relocation moves.
   *
   * @param color       The player color.
   * @param allowedZone The geometric zone allowed for relocation (e.g., Circle 5).
   * @param isQueen     True if calculating for a queen relocation.
   * @return A bitboard of legal relocation destinations.
   */
  private BitBoard getRelocationMovesInternal(Color color, BitBoard allowedZone, boolean isQueen) {
    // 1. On part de TOUTES les cases de la zone autorisée
    BitBoard legalMoves = new BitBoard(allowedZone);
    BitBoard occupied = getOccupiedTotal();
    BitBoard suicide = getSuicideMask(color);
    //let free tiles
    legalMoves = legalMoves.andOperation(occupied.complementOperation());
    // erase suicide tiles
    legalMoves = legalMoves.andOperation(suicide.complementOperation());
    //specific case for the queen she can't go on the throne with a relocation
    if (isQueen) {
      legalMoves.setBit(THRONE, 0L);
    }
    return legalMoves;
  }

  /**
   * Determines legal relocation moves based on game priority (Queen first).
   *
   * @param color The player color.
   * @return A bitboard of valid relocation spots.
   */
  private BitBoard getRelocationMoves(Color color) {
    if (color == Color.WHITE ? whiteQueenToRelocate : blackQueenToRelocate) {
      return getRelocationMovesInternal(color, validZoneMask, true);
    } else {
      return getRelocationMovesInternal(color, circles[5], false);
    }
  }

  /**
   * Calculates the raw physical neighbors of all set bits in the provided bitboard.
   *
   * <p>This method performs a pure mathematical dilation (expansion) in all six hexagonal
   * directions. It does not check if the resulting tiles are within the legal board boundaries. *
   *
   * @param board The source {@link BitBoard} to expand.
   * @return A new {@link BitBoard} representing the dilated area.
   */
  private BitBoard getAllNeighborsInternal(BitBoard board) {
    return board.dilation();
  }

  /**
   * Calculates all valid adjacent tiles for the pieces present on the given bitboard.
   *
   * <p>Unlike the internal version, this method filters the results against {@code validZoneMask}
   * to ensure that only tiles actually belonging to the 121-tile Agon board are returned. * @param
   * board The source {@link BitBoard} containing the pieces.
   *
   * @return A {@link BitBoard} containing only the legal neighboring tiles.
   */
  private BitBoard getAllNeighbors(BitBoard board) {
    return board.dilation().andOperation(this.validZoneMask);
  }

  /**
   * Validates if a move is physically possible (in the valid zone, adjacent, free and not moving
   * backwards/outwards).
   *
   * @param from  Start index.
   * @param to    Destination index.
   * @param color Player color.
   * @return true if the move follows game rules.
   */
  private boolean isValid(int from, int to, Color color) {
    if (isFree(to) && isAdjacent(from, to)) {
      BitBoard maskTo = new BitBoard(to);
      BitBoard legalMoves = generateLegalMovesBitboard(color);
      return !maskTo.andOperation(legalMoves).isEmpty();
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
    BitBoard fromNeighbors = getNeighbors(from);
    BitBoard toBitboard = new BitBoard(to);
    return !fromNeighbors.andOperation(toBitboard).isEmpty();
  }

  /**
   * Checks if a tile is within the valid board area and unoccupied.
   *
   * @param index Tile index.
   * @return true if the tile is available for a piece.
   */
  private boolean isFree(int index) {
    BitBoard indexMask = new BitBoard(index);
    BitBoard occupiedZone = getOccupiedTotal();
    return (!validZoneMask.andOperation(indexMask).isEmpty())
        && (occupiedZone.andOperation(indexMask).isEmpty());
  }

  /**
   * Checks if the specified player has any captured pieces (Queen or Pawns) currently waiting in
   * the relocation queue.
   *
   * @param color The {@link Color} of the player to check.
   * @return {@code true} if at least one piece is pending relocation; {@code false} otherwise.
   */
  private boolean hasPiecesToRelocate(Color color) {
    if (color == Color.WHITE) {
      return whiteQueenToRelocate || whitePawnsToRelocate > 0;
    }
    return blackQueenToRelocate || blackPawnsToRelocate > 0;
  }

  /**
   * Pre-calculates the allowed destination masks for each concentric circle.
   *
   * <p>This implements the "no retreating" rule: a piece can only move to a tile within its
   * current circle or to a circle closer to the center (Throne).
   */
  private void initAllowedDestinations() {
    // A piece can move only forward or on the sides
    for (int i = 0; i < 6; i++) {
      allowedDestinations[i] = new BitBoard();
    }
    for (int i = 5; i >= 1; i--) {
      // For each circle i, allowed destinations are tiles in circle i and circle i-1
      for (int j = i; j >= i - 1; j--) {
        allowedDestinations[i] = allowedDestinations[i].orOperation(circles[j]);
      }
    }
  }

  /**
   * Initializes the concentric circles and the global valid board mask.
   *
   * <p>This method uses a flood-fill (dilation) technique:
   *
   * <ol>
   *   <li>Starts at the {@code THRONE} (Circle 0).
   *   <li>Iteratively expands outwards to define Circles 1 through 5.
   *   <li>Ensures each tile belongs to only one circle by using complements.
   * </ol>
   */
  private void initCirclesAndValidZones() {
    for (int i = 1; i < 6; i++) {
      circles[i] = new BitBoard();
    }
    // Starting with THRONE (center)
    circles[0] = new BitBoard(THRONE);
    validZoneMask.setBit(THRONE, 1);

    // Create outer circles using the expansion (dilation) method
    for (int i = 1; i < 6; i++) {
      BitBoard expansion = getAllNeighborsInternal(circles[i - 1]);

      // Remove tiles that are already part of a previously processed inner circle
      expansion = expansion.andOperation(validZoneMask.complementOperation());
      circles[i] = expansion;

      // Update the global mask with the newly discovered valid tiles
      this.validZoneMask = this.validZoneMask.orOperation(circles[i]);
    }
  }

  public BitBoard getWhiteQueen() {
    return whiteQueen;
  }
  public BitBoard getBlackQueen() {
    return blackQueen;
  }
  public BitBoard getWhitePawns() {
    return whitePawns;
  }
  public BitBoard getBlackPawns() {
    return blackPawns;
  }

  public List<HistoryInformations> getHistory() {
    return history.toList();
  }
}
