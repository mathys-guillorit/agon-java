package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.agonElements.PieceType;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;
import fr.univ.bordeaux.agonCore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.agonCore.history.HistoryInformations;
import fr.univ.bordeaux.application.match.player.Player;
import java.util.ArrayList;
import java.util.List;

public abstract class Match {
  private AgonBoard agonBoard;
  private Player currentPlayer;
  private Player player1;
  private Player player2;
  private MatchStatus status;

  public Match(AgonBoard agonBoard,Player player1,Player player2) {
    this.agonBoard = agonBoard;
    this.player1=player1;
    this.player2=player2;
    this.currentPlayer=player1;
    this.status=MatchStatus.RUNNING;
  }

  public boolean playMove(Move move) {
    this.startActions();
    PieceType piece=agonBoard.getPieceAt(move.getFrom());
    if (piece == null || piece.getColor() != currentPlayer.getColor()) {
      return false;
    }
    if (agonBoard.applyMove(move)) {
      if (agonBoard.isGameWon(currentPlayer.getColor())) {
        this.status=MatchStatus.FINISHED;
        System.out.println("win");
      }
      this.endActions();
      this.switchPlayer();
      return true;
    }
    return false;
  }
public abstract void startActions();
  public abstract void endActions();
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

  public MatchStatus getMatchStatus(){
    return status;
  }
  protected void setMatchStatus(MatchStatus status){
    this.status=status;
  }
  public Player getCurrentPlayer(){
    return currentPlayer;
  }

  protected void switchPlayer() {
    currentPlayer = (currentPlayer.equals(player1)) ? player2 : player1;
  }
}
