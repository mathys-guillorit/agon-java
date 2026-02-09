package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.PieceType;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;

public abstract class AbstractHeuristic implements Heuristic {

    protected int pawnWeight;
    protected int queenWeight;

    public AbstractHeuristic(int pawnWeight, int queenWeight) {
        this.pawnWeight = pawnWeight;
        this.queenWeight = queenWeight;
    }

    protected abstract long getFactor(AgonBoard board, int index);

    @Override
    public long evaluate(AgonBoard board, Color aiColor) {
        long score = 0;
        for (int i = 0; i < 90; i++) {
            PieceType piece = board.getPieceAt(i);
            if (piece == null) continue;
            long factor = getFactor(board, i);
            long pieceValue = 0;
            if (piece == PieceType.WHITE_QUEEN || piece == PieceType.BLACK_QUEEN) {
                pieceValue = factor * queenWeight;
            } else {
                pieceValue = factor * pawnWeight;
            }
            boolean isWhitePiece = (piece == PieceType.WHITE_PAWN || piece == PieceType.WHITE_QUEEN);
            if (aiColor == Color.WHITE) {
                score += (isWhitePiece ? pieceValue : -pieceValue);
            } else {
                score += (isWhitePiece ? -pieceValue : pieceValue);
            }
        }
        return score;
    }
}