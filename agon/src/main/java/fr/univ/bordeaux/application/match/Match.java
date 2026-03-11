package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agonCore.agonElements.Move;
import fr.univ.bordeaux.agonCore.agonElements.PieceType;
import fr.univ.bordeaux.agonCore.bitboard.AgonBoard;
import fr.univ.bordeaux.agonCore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.agonCore.history.HistoryInformations;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.player.Player;
import  fr.univ.bordeaux.ui.ObservableMatch;
import  fr.univ.bordeaux.ui.MatchObserver;
import java.util.ArrayList;
import java.util.List;

public abstract class Match implements MatchManager,ObservableMatch{
  private AgonBoard agonBoard;
  private Player currentPlayer;
  private Player player1;
  private Player player2;
  private MatchStatus status;
  MatchObserver observer;

  public Match(AgonBoard agonBoard, Player player1, Player player2) {
    this.agonBoard = agonBoard;
    this.player1 = player1;
    this.player2 = player2;
    this.currentPlayer = player1;
    this.status = MatchStatus.RUNNING;
  }

  public void loopGame(){
    while(this.status!=MatchStatus.FINISHED){
      this.observer.updateBoard(agonBoard);
      CmdAction cmd=currentPlayer.getAction();
      cmd.execute(this);
      switchPlayer();
    }
    this.observer.updateBoard(agonBoard);
  }

  public boolean move(Move move) {
    System.out.println("je suis dans move de match");
    //this.startActions();
    PieceType piece = agonBoard.getPieceAt(move.getFrom());
    if (piece == null || piece.getColor() != currentPlayer.getColor()) {
      return false;
    }
    if (agonBoard.applyMove(move)) {
      System.out.println("le move a marché");
      if (agonBoard.isGameWon(currentPlayer.getColor())) {
        this.status = MatchStatus.FINISHED;
        System.out.println("win");
      }
      //this.endActions();
      return true;
    }
    System.out.println("le move a pas marcher");
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

  public MatchStatus getMatchStatus() {
    return status;
  }

  protected void setMatchStatus(MatchStatus status) {
    this.status = status;
  }

  public Player getCurrentPlayer() {
    return currentPlayer;
  }
  @Override
  public void setObserver(MatchObserver observer){
    this.observer = observer;
  }
  @Override
  public void notifyObserver(){
    this.observer.updateBoard(this.agonBoard);
  }
  protected void switchPlayer() {
    currentPlayer = (currentPlayer.equals(player1)) ? player2 : player1;
  }
}
