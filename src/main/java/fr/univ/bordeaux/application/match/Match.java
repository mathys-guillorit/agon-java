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
import fr.univ.bordeaux.technical.utils.GameLogger;
import fr.univ.bordeaux.ui.MatchObserver;
import fr.univ.bordeaux.ui.ObservableMatch;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class representing an Agon Match between two players.
 *
 * <p>This class manages the core game state, including the board, players, turn switching, and move
 * history. It implements the Observer pattern to notify the UI of any state changes.
 */
public abstract class Match implements MatchManager, ObservableMatch {

  /** The physical game board. */
  private final AgonBoard agonBoard;

  /** The player whose turn it is currently. */
  private Player currentPlayer;

  /** The first player participant. */
  private final Player player1;

  /** The second player participant. */
  private final Player player2;

  /** The current status of the match (RUNNING, FINISHED, etc.). */
  private MatchStatus status;

  /** Configuration settings for the current game. */
  private final GameConfig gameConfig;

  /** Flag indicating if the current state has been saved to a file. */
  private boolean isSaved = false;

  /** The UI observer to be notified of updates. */
  private MatchObserver uiObserver;

  /** The winner of the match, null if the game is ongoing or a draw. */
  private Player winner;

  /**
   * Initializes a new Match. By default, the White player starts.
   *
   * @param agonBoard The board to play on.
   * @param player1 The first player.
   * @param player2 The second player.
   * @param gameConfig The game configuration.
   */
  public Match(AgonBoard agonBoard, Player player1, Player player2, GameConfig gameConfig) {
    this(agonBoard, player1, player2, gameConfig, Color.WHITE);
  }

  /**
   * Initializes a new Match with a specific starting color.
   *
   * @param agonBoard The board to play on.
   * @param player1 The first player.
   * @param player2 The second player.
   * @param gameConfig The game configuration.
   * @param startingColor The color of the player who should move first.
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
   * Checks if the match is currently in progress.
   *
   * @return {@code true} if the status is RUNNING, {@code false} otherwise.
   */
  public boolean isRunning() {
    return status == MatchStatus.RUNNING;
  }

  /**
   * Attempts to move a piece on the board.
   *
   * <p>Validates the move against the current player and the board state. If successful, it checks
   * for win conditions and triggers end-of-turn actions.
   *
   * @param move The move to be executed.
   * @return {@code true} if the move was successful and applied, {@code false} otherwise.
   */
  public boolean move(Move move) {
    GameLogger.info("trying to play move " + move.toString());
    if (this.status == MatchStatus.FINISHED) {
      return false;
    }
    PieceType piece = agonBoard.getPieceAt(move.getFrom());
    if (piece != null) {
      if (piece.getColor() != currentPlayer.getColor()) {
        GameLogger.info(
            "Move rejected: "
                + currentPlayer.getName()
                + " cannot move "
                + piece.getColor()
                + " pieces.");
        return false;
      }
    }
    if (agonBoard.applyMove(move)) {
      if (agonBoard.isGameWon(currentPlayer.getColor())) {
        GameLogger.info("MATCH FINISHED: " + currentPlayer.getName() + " wins!");
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
   * @return The winning {@link Player}, or {@code null} if no winner yet.
   */
  @Override
  public Player getWinner() {
    return winner;
  }

  /**
   * @return The player playing with the White pieces.
   */
  public Player getWhitePlayer() {
    return (player1.getColor() == Color.WHITE) ? player1 : player2;
  }

  /**
   * @return The player playing with the Black pieces.
   */
  public Player getBlackPlayer() {
    return (player1.getColor() == Color.BLACK) ? player1 : player2;
  }

  /**
   * @param winner The player to be set as the winner.
   */
  protected void setWinner(Player winner) {
    this.winner = winner;
  }

  /** Hook for actions to perform at the very beginning of a player's turn. */
  public abstract void startActions();

  /** Hook for actions to perform at the end of a player's turn (e.g., stopping timers). */
  public abstract void endActions();

  /**
   * Retrieves the history of moves formatted for the UI.
   *
   * @return A {@link List} of {@link MoveDtO} containing coordinates in AbaPro notation.
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

  /** Terminates the match immediately. */
  public void quit() {
    this.setMatchStatus(MatchStatus.FINISHED);
  }

  /**
   * Replays the last undone move.
   *
   * <p>In Agon, this typically involves replaying a full round (two moves).
   *
   * @return {@code true} if redo was successful.
   */
  public boolean redo() {
    GameLogger.info("Redoing round...");
    boolean res1 = agonBoard.redoMove();
    boolean res2 = agonBoard.redoMove();
    if (res1 || res2) {
      this.isSaved = false;
    }
    this.notifyUi();
    return res1 && res2;
  }

  /** Triggers an update on the registered UI observer. */
  public void notifyUi() {
    if (this.uiObserver != null) {
      this.uiObserver.onMatchUpdate(this);
    }
  }

  /**
   * Cancels the last moves performed.
   *
   * <p>In Agon, this typically undoes a full round (two moves).
   *
   * @return {@code true} if undo was successful.
   */
  public boolean undo() {
    GameLogger.info("Undoing round...");
    boolean resNoir = agonBoard.undoMove();
    if (!resNoir) return false;

    boolean resBlanc = agonBoard.undoMove();
    if (!resBlanc) {
      GameLogger.warn("Partial undo! Restoring last move...");
      agonBoard.redoMove();
      return false;
    }
    this.isSaved = false;
    this.notifyUi();
    return true;
  }

  /**
   * @return {@code true} if the current match state is saved to persistent storage.
   */
  public boolean isSaved() {
    return isSaved;
  }

  /**
   * @param isSaved The new saved status of the match.
   */
  @Override
  public void setIsSaved(boolean isSaved) {
    this.isSaved = isSaved;
  }

  /**
   * Provides a move suggestion for the current player using an AI strategy.
   *
   * @return A suggested {@link Move}.
   */
  public Move hint() {
    AgonAi ai = AiFactory.createHintAi(currentPlayer.getColor());
    return ai.getBestMove(agonBoard);
  }

  /**
   * Pauses the match.
   *
   * @return {@code false} by default (to be overridden in timed matches).
   */
  public boolean pause() {
    return false;
  }

  /**
   * Gets the remaining time for the current turn.
   *
   * @return A formatted time string or {@code null} if not applicable.
   */
  public String getCurrentPlayerRemainingTime() {
    return "null";
  }

  /**
   * @return The current {@link MatchStatus}.
   */
  public MatchStatus getMatchStatus() {
    return status;
  }

  /**
   * @return {@code true} if the match has reached a terminal state.
   */
  public boolean isMatchOver() {
    return this.status == MatchStatus.FINISHED;
  }

  /**
   * @param status The new status to be assigned to the match.
   */
  protected void setMatchStatus(MatchStatus status) {
    this.status = status;
  }

  /**
   * @return The {@link Player} who is currently active.
   */
  public Player getCurrentPlayer() {
    return currentPlayer;
  }

  /**
   * @param observer The observer to register for match updates.
   */
  @Override
  public void setObserver(MatchObserver observer) {
    this.uiObserver = observer;
  }

  /**
   * @return The underlying board instance.
   */
  public AgonBoard getAgonBoard() {
    return agonBoard;
  }

  /**
   * @return The game configuration associated with this match.
   */
  public GameConfig getGameConfig() {
    return gameConfig;
  }

  /** Hook for turn-specific initialization logic. */
  public void startTurn() {}

  /** Switches the current active player. */
  protected void switchPlayer() {
    currentPlayer = (currentPlayer.equals(player1)) ? player2 : player1;
    GameLogger.info(
        "Turn switched to: " + currentPlayer.getName() + " (" + currentPlayer.getColor() + ")");
  }
  /**
   * Retrieves the remaining reflection time for all players in the match.
   *
   * <p>This method provides a snapshot of the timers for every participant.
   * If the current match type does not support timed play, it returns an
   * empty array.
   *
   * @return An array of {@link String} where first element represents white player
   * remaining time and the second the black player timer (e.g., "05:30"). Returns an empty array if timers
   * are not applicable.
   */
  @Override
  public String[] getAllPlayersRemainingTime() {
    return new String[0];
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
