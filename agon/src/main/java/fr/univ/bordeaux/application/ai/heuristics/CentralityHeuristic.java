package fr.univ.bordeaux.application.ai.heuristics;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.PieceType;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;

public class CentralityHeuristic implements Heuristics {

    @Override
    public long evaluate(AgonBoard board, Color aiColor) {
        long score = 0;
        long centrality = -1;
        for(int i=0; i<90; i++){
            if(board.getPieceAt(i) != null){
                centrality = board.getCentrality(i);
                if(board.getPieceAt(i) == PieceType.WHITE_PAWN){
                    if(aiColor == Color.WHITE){
                        score += (centrality*10);
                    }else{
                        score -= (centrality*10);
                    }
                }else if (board.getPieceAt(i) == PieceType.BLACK_PAWN){
                    if(aiColor == Color.BLACK){
                        score += (centrality*10);
                    }else{
                        score -= (centrality*10);
                    }
                }else if (board.getPieceAt(i) == PieceType.WHITE_QUEEN){
                    if(aiColor == Color.WHITE){
                        score += (centrality*1000);
                    }else {
                        score -= (centrality*1000);
                    }
                }else if  (board.getPieceAt(i) == PieceType.BLACK_QUEEN){
                    if(aiColor == Color.BLACK){
                        score += (centrality*1000);
                    }else{
                        score -= (centrality*1000);
                    }
                }
            }
        }
        return score;
    }
}
