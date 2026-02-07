package fr.univ.bordeaux.agonCore.bitboard;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.PieceType;

public interface RestrictedAgonBoard{
  long[] generateLegalMoves(Color color);
  PieceType getPieceAt(int index);
}
