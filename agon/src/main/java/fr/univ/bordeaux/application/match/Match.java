package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agonCore.agonElements.Color;
import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;
import fr.univ.bordeaux.agonCore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.agonCore.history.HistoryInformations;
import java.util.ArrayList;
import java.util.List;

public abstract class Match {
  private AgonBoard agonBoard;
  private Color currentPlayer = Color.BLACK;

  public Match(AgonBoard agonBoard) {
    this.agonBoard = agonBoard;
  }

  public boolean executeMove(Move move) {
    if (move.getColor() != currentPlayer) {
      return false;
    }
    if (agonBoard.applyMove(move)) {
      if (agonBoard.isGameWon(currentPlayer)) {
        System.out.println("win");
      }
      return true;
    }
    return false;
  }

  public List<MoveDTO> getHistory() {
    List<HistoryInformations> domainHistory = agonBoard.getHistory();
    List<MoveDTO> uiList = new ArrayList<>();
    for (HistoryInformations info : domainHistory) {
      Move mainMove = info.getMoves().getFirst();
      uiList.add(
          new MoveDTO(
              CoordinateMapper.toAbaPro(mainMove.getFrom()),
              CoordinateMapper.toAbaPro(mainMove.getTo()),
              mainMove.getPieceType().toString()));
    }
    return uiList;
  }

  public void switchPlayer() {
    currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
  }
}
