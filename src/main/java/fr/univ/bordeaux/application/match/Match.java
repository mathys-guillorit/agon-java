package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.agoncore.bitboard.RestrictedAgonBoard;
import fr.univ.bordeaux.agoncore.history.HistoryInformations;
import fr.univ.bordeaux.application.ai.strategy.AgonAi;
import fr.univ.bordeaux.application.ai.strategy.AiFactory;
import fr.univ.bordeaux.application.match.player.Player;
import fr.univ.bordeaux.ui.MatchObserver;
import fr.univ.bordeaux.ui.ObservableMatch;
import java.util.ArrayList;
import java.util.List;

public abstract class Match implements MatchManager, ObservableMatch {

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
    this.currentPlayer = player1.getColor() == Color.WHITE ? player1 : player2;
    this.status = MatchStatus.RUNNING;
  }

  public boolean isRunning() {
    return status == MatchStatus.RUNNING;
  }

  public boolean move(Move move) {
    this.startActions();
    if (this.status == MatchStatus.FINISHED) {
      return false;
    }
    PieceType piece = agonBoard.getPieceAt(move.getFrom());
    if (piece != null) {
      if (piece.getColor() != currentPlayer.getColor()) {
        return false;
      }
    }
    if (agonBoard.applyMove(move)) {
      if (agonBoard.isGameWon(currentPlayer.getColor())) {
        this.status = MatchStatus.FINISHED;
        System.out.println("win");
      }
      this.endActions();
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
              CoordinateMapper.toAbaPro(mainMove.getDestination()),
              mainMove.getPieceType().toString()));
    }
    return uiList;
  }

  public void quit() {
    this.setMatchStatus(MatchStatus.FINISHED);
  }

  public boolean redo() {
    boolean res1 = agonBoard.redoMove();
    boolean res2 = agonBoard.redoMove();
    return res1 && res2;
  }

  @Override
  public boolean undo() {
    agonBoard.undoMove();
    return agonBoard.undoMove();
  }

  public Move hint() {
    AgonAi ai = AiFactory.createHintAi(currentPlayer.getColor());
    Move hint = ai.getBestMove(agonBoard);
    return hint;
  }

  public boolean pause() {
    return false;
  }

  public String getRemainingTime() {
    return null;
  }

  public MatchStatus getMatchStatus() {
    return status;
  }

  public boolean isMatchOver() {
    return this.status == MatchStatus.FINISHED;
  }

  protected void setMatchStatus(MatchStatus status) {
    this.status = status;
  }

  public Player getCurrentPlayer() {
    return currentPlayer;
  }

  @Override
  public void setObserver(MatchObserver observer) {
    this.observer = observer;
  }

  public RestrictedAgonBoard getAgonBoard() {
    return agonBoard;
  }

  protected void switchPlayer() {
    currentPlayer = (currentPlayer.equals(player1)) ? player2 : player1;
  }
}
