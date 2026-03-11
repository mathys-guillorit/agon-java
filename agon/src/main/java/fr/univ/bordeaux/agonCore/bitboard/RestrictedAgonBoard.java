package fr.univ.bordeaux.agonCore.bitboard;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.agonElements.PieceType;
import java.util.List;

/**
 * Provides a read-only or limited access view of the Agon board state.
 *
 * <p>This interface is designed for components that need to inspect the board or calculate
 * potential actions without having the authority to modify the game state directly.
 */
public interface RestrictedAgonBoard {
  /**
   * Generates a complete list of all legal moves for the specified player.
   *
   * <p>The method accounts for the current game state, including:
   *
   * <ul>
   *   <li>Mandatory relocations if the player has captured pieces.
   *   <li>Movement constraints (no retreating to outer circles).
   *   <li>Specific piece rules (e.g., pawns cannot enter the throne).
   *   <li>Suicide prevention (pieces cannot move into a sandwich).
   * </ul>
   *
   * @param color The {@link Color} of the player whose moves are being generated.
   * @return A {@link List} of all valid {@link Move} objects available.
   */
  List<Move> generateLegalMoves(Color color);

  /**
   * Identifies the piece located at a specific tile on the board. *
   *
   * <p>This is a helper method used to inspect the board state without direct bitboard
   * manipulation.
   *
   * @param index The tile index (0 to 120) to inspect.
   * @return The {@link PieceType} at the given index, or {@code null} if the tile is empty or the
   *     index is out of bounds.
   */
  PieceType getPieceAt(int index);
}
