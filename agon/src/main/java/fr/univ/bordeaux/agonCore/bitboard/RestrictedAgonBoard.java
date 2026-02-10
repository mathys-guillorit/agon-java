package fr.univ.bordeaux.agonCore.bitboard;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.agonElements.PieceType;
import java.util.List;

public interface RestrictedAgonBoard {

  List<Move> generateLegalMoves(Color color);

  PieceType getPieceAt(int index);
}
