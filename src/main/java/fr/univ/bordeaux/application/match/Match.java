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
import fr.univ.bordeaux.technical.io.config.GameConfig;
import fr.univ.bordeaux.ui.MatchObserver;
import fr.univ.bordeaux.ui.ObservableMatch;
import java.util.ArrayList;
import java.util.List;

/** Play an Agon Match between two players. */
public abstract class Match implements MatchManager, ObservableMatch {

  private AgonBoard agonBoard;
  private Player currentPlayer;
  private Player player1;
  private Player player2;
  private MatchStatus status;
  private GameConfig gameConfig;
  MatchObserver observer;

  /**
   * Play a {@link Match}.
   *
   * @param agonBoard {@link AgonBoard} board to play on
   * @param player1 {@link Player} first player that plays the party
   * @param player2 {@link Player} second player that plays the party
   */
  public Match(AgonBoard agonBoard, Player player1, Player player2, GameConfig gameConfig) {
    this.agonBoard = agonBoard;
    this.player1 = player1;
    this.player2 = player2;
    this.gameConfig = gameConfig;
    this.currentPlayer = player1.getColor() == Color.WHITE ? player1 : player2;
    this.status = MatchStatus.RUNNING;
  }

  /**
   * get if the game is (ended or not runned) or not.
   *
   * @return true | false
   */
  public boolean isRunning() {
    return status == MatchStatus.RUNNING;
  }

  /**
   * Move a piece on the board.
   *
   * @param move {@link Move}
   * @return true succeeded else false
   */
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

  /** Beginning actions when a turn is about to start. */
  public abstract void startActions();

  /** Actions when a turn is about to end. */
  public abstract void endActions();

  /**
   * get last turns.
   *
   * @return {@link List}
   */
  public List<MoveDtO> getHistory() {
    List<HistoryInformations> domainHistory = agonBoard.getHistory();
    List<MoveDtO> uiList = new ArrayList<>();
    for (HistoryInformations info : domainHistory) {
      Move mainMove = info.getMoves().getFirst();
      uiList.add(
          new MoveDtO(
              CoordinateMapper.toAbaPro(mainMove.getFrom()),
              CoordinateMapper.toAbaPro(mainMove.getDestination()),
              mainMove.getPieceType().toString()));
    }
    return uiList;
  }

  /** Stop Match. */
  public void quit() {
    this.setMatchStatus(MatchStatus.FINISHED);
  }

  /**
   * Redo a turn.
   *
   * @return true has succeeded else false not possible (not enough history)
   */
  public boolean redo() {
    boolean res1 = agonBoard.redoMove();
    boolean res2 = agonBoard.redoMove();
    return res1 && res2;
  }

  /**
   * Undo a turn.
   *
   * @return true has succeeded else false not possible (not enough history)
   */
  @Override
  public boolean undo() {
    agonBoard.undoMove();
    return agonBoard.undoMove();
  }

  /**
   * Predict next turn.
   *
   * @return {@link Move}
   */
  public Move hint() {
    AgonAi ai = AiFactory.createHintAi(currentPlayer.getColor());
    Move hint = ai.getBestMove(agonBoard);
    return hint;
  }

  /**
   * Pause the game.
   *
   * @return false always
   */
  public boolean pause() {
    return false;
  }

  /**
   * Get the time left (blitz mode or runed game under time constraints).
   *
   * @return {@link String}
   */
  public String getRemainingTime() {
    return null;
  }

  /**
   * See if we play or not.
   *
   * @return {@link MatchStatus}
   */
  public MatchStatus getMatchStatus() {
    return status;
  }

  /**
   * Game not started or already ended or not finished.
   *
   * @return true | false
   */
  public boolean isMatchOver() {
    return this.status == MatchStatus.FINISHED;
  }

  /**
   * Explicit.
   *
   * @param status {@link MatchStatus}
   */
  protected void setMatchStatus(MatchStatus status) {
    this.status = status;
  }

  /**
   * Explicit.
   *
   * @return {@link Player}
   */
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

  public GameConfig getGameConfig() {
    return gameConfig;
  }

  protected void switchPlayer() {
    currentPlayer = (currentPlayer.equals(player1)) ? player2 : player1;
  }
}
