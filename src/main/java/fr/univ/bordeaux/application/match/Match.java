package fr.univ.bordeaux.application.match;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.agonelements.Move;
import fr.univ.bordeaux.agoncore.agonelements.PieceType;
import fr.univ.bordeaux.agoncore.bitboard.AgonBoard;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
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

  private final AgonBoard agonBoard;
  private Player currentPlayer;
  private final Player player1;
  private final Player player2;
  private MatchStatus status;
  private final GameConfig gameConfig;
  private boolean isSaved = false;
  private MatchObserver UiObserver;
  private Player winner;

  /**
   * Play a {@link Match}.
   *
   * @param agonBoard {@link AgonBoard} board to play on
   * @param player1 {@link Player} first player that plays the party
   * @param player2 {@link Player} second player that plays the party
   */
  public Match(AgonBoard agonBoard, Player player1, Player player2, GameConfig gameConfig) {
    this(agonBoard, player1, player2, gameConfig, Color.WHITE);
  }

  /**
   * Play a {@link Match}.
   *
   * @param agonBoard {@link AgonBoard} board to play on
   * @param player1 {@link Player} first player that plays the party
   * @param player2 {@link Player} second player that plays the party
   * @param gameConfig {@link GameConfig} game configuration
   * @param startingColor {@link Color} the color of the player who starts
   */
  public Match(
      AgonBoard agonBoard,
      Player player1,
      Player player2,
      GameConfig gameConfig,
      Color startingColor) {
    this.agonBoard = agonBoard;
    this.player1 = player1;
    this.player2 = player2;
    this.gameConfig = gameConfig;
    this.currentPlayer = player1.getColor() == startingColor ? player1 : player2;
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
    // this.startActions();
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
        this.winner = currentPlayer;
      }
      this.isSaved = false;
      this.endActions();
      this.notifyUi();
      return true;
    }
    return false;
  }

  /**
   * Returns the winner of the match.
   *
   * @return the winning player, or null if the match is not finished yet
   */
  @Override
  public Player getWinner() {
    return winner;
  }

  /**
   * Returns the player controlling the white pieces.
   *
   * @return the white player
   */
  public Player getWhitePlayer() {
    return (player1.getColor() == Color.WHITE) ? player1 : player2;
  }

  /**
   * Returns the player controlling the black pieces.
   *
   * @return the black player
   */
  public Player getBlackPlayer() {
    return (player1.getColor() == Color.BLACK) ? player1 : player2;
  }

  /**
   * Sets the winner of the match.
   *
   * @param winner the player who won the match
   */
  protected void setWinner(Player winner) {
    this.winner = winner;
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
    if (res1 || res2) {
      this.isSaved = false;
    }
    this.notifyUi();
    return res1 && res2;
  }

  /** Notify the UI about a state change in the match. */
  public void notifyUi() {
    if (this.UiObserver != null) {
      this.UiObserver.onMatchUpdate(this);
    }
  }

  /**
   * Undo a turn.
   *
   * @return true has succeeded else false not possible (not enough history)
   */
  @Override
  public boolean undo() {
    boolean res1 = agonBoard.undoMove();
    boolean res2 = agonBoard.undoMove();
    if (res1 || res2) {
      this.isSaved = false;
    }
    this.notifyUi();
    return res1 && res2;
  }

  /**
   * Check if the match is saved.
   *
   * @return true if saved, false otherwise.
   */
  public boolean isSaved() {
    return isSaved;
  }

  /**
   * Set the saved status of the match.
   *
   * @param isSaved the saved status.
   */
  @Override
  public void setIsSaved(boolean isSaved) {
    this.isSaved = isSaved;
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

  /**
   * Registers an observer notified when the match state changes.
   *
   * @param observer the observer to register
   */
  @Override
  public void setObserver(MatchObserver observer) {
    this.UiObserver = observer;
  }

  /**
   * Returns the board associated with this match.
   *
   * @return the Agon board used by this match
   */
  public AgonBoard getAgonBoard() {
    return agonBoard;
  }

  /**
   * Returns the configuration used for this match.
   *
   * @return the game configuration
   */
  public GameConfig getGameConfig() {
    return gameConfig;
  }

  /**
   * Performs actions at the beginning of a player's turn.
   *
   * <p>This default implementation does nothing and may be overridden
   * by subclasses that need turn-specific behavior.
   */
  public void startTurn() {}

  /**
   * Switches the current player to the other player in the match.
   */
  protected void switchPlayer() {
    currentPlayer = (currentPlayer.equals(player1)) ? player2 : player1;
  }

  /**
   * Checks whether the given player color must perform a replacement move.
   *
   * @param color the color to check
   * @return true if a replacement move is required, false otherwise
   */
  public boolean isReplacementMoveRequired(Color color) {
    return agonBoard != null && agonBoard.hasPiecesToRelocate(color);
  }
}
