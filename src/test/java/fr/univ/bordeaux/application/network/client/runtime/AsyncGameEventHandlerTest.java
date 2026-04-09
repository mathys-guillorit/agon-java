package fr.univ.bordeaux.application.network.client.runtime;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.network.OnlineGameInfo;
import fr.univ.bordeaux.application.network.OnlineGameStartListener;
import org.junit.jupiter.api.Test;

class AsyncGameEventHandlerTest {

  private static class RecordingListener implements OnlineGameStartListener {
    OnlineGameInfo startedInfo;
    String localMove;
    String opponentMove;
    String gameOverLine;
    int refreshCount;

    @Override
    public void onOnlineGameStarted(OnlineGameInfo info) {
      startedInfo = info;
    }

    @Override
    public void onLocalMoveConfirmed(String rawMove) {
      localMove = rawMove;
    }

    @Override
    public void onOpponentMoveReceived(String rawMove) {
      opponentMove = rawMove;
    }

    @Override
    public void onGameOver(String line) {
      gameOverLine = line;
    }

    @Override
    public void onOnlineBoardRefreshRequested() {
      refreshCount++;
    }
  }

  @Test
  void handleMoveConfirmedAndOpponentMove() {
    AsyncGameEventHandler handler = new AsyncGameEventHandler();
    RecordingListener listener = new RecordingListener();

    handler.handleMoveConfirmed("MOVE_OK e2e4", listener);
    handler.handleOpponentMove("OPPONENT_MOVE e7e5", listener);

    assertEquals("e2e4", listener.localMove);
    assertEquals("e7e5", listener.opponentMove);
  }

  @Test
  void handleMoveBranchesIgnoreBlankPayloadForListener() {
    AsyncGameEventHandler handler = new AsyncGameEventHandler();
    RecordingListener listener = new RecordingListener();

    handler.handleMoveConfirmed("MOVE_OK   ", listener);
    handler.handleOpponentMove("OPPONENT_MOVE   ", listener);

    assertNull(listener.localMove);
    assertNull(listener.opponentMove);
  }

  @Test
  void handleMoveBranchesSupportNullListener() {
    AsyncGameEventHandler handler = new AsyncGameEventHandler();

    assertDoesNotThrow(() -> handler.handleMoveConfirmed("MOVE_OK e2e4", null));
    assertDoesNotThrow(() -> handler.handleMoveConfirmed("MOVE_OK   ", null));
    assertDoesNotThrow(() -> handler.handleOpponentMove("OPPONENT_MOVE e7e5", null));
    assertDoesNotThrow(() -> handler.handleOpponentMove("OPPONENT_MOVE   ", null));
  }

  @Test
  void handleGameOverCallsListener() {
    AsyncGameEventHandler handler = new AsyncGameEventHandler();
    RecordingListener listener = new RecordingListener();

    handler.handleGameOver("GAME_OVER RESULT=LOSS REASON=OPPONENT_LEFT", listener);
    assertEquals("GAME_OVER RESULT=LOSS REASON=OPPONENT_LEFT", listener.gameOverLine);

    handler.handleGameOver("GAME_OVER RESULT=LOSS", listener);
    assertEquals("GAME_OVER RESULT=LOSS", listener.gameOverLine);

    handler.handleGameOver("GAME_OVER RESULT=DRAW", listener);
    assertEquals("GAME_OVER RESULT=DRAW", listener.gameOverLine);
  }

  @Test
  void handleGameOverSupportsNullListenerAndAllLogBranches() {
    AsyncGameEventHandler handler = new AsyncGameEventHandler();

    assertDoesNotThrow(
        () -> handler.handleGameOver("GAME_OVER RESULT=WIN REASON=OPPONENT_LEFT", null));
    assertDoesNotThrow(
        () -> handler.handleGameOver("GAME_OVER RESULT=LOSS REASON=OPPONENT_LEFT", null));
    assertDoesNotThrow(() -> handler.handleGameOver("GAME_OVER RESULT=WIN", null));
    assertDoesNotThrow(() -> handler.handleGameOver("GAME_OVER RESULT=LOSS", null));
    assertDoesNotThrow(() -> handler.handleGameOver("GAME_OVER RESULT=DRAW", null));
  }

  @Test
  void handleGameStartMessageBuildsInfo() {
    AsyncGameEventHandler handler = new AsyncGameEventHandler();
    AsyncEventParser parser = new AsyncEventParser();
    RecordingListener listener = new RecordingListener();

    handler.handleGameStartMessage(
        "GAME_STARTED GAME_ID=7 COLOR=WHITE WHITE=Alice BLACK=Bob MODE=NORMAL", listener, parser);

    assertNotNull(listener.startedInfo);
    assertEquals(7, listener.startedInfo.getGameId());
    assertEquals(Color.WHITE, listener.startedInfo.getLocalColor());
    assertFalse(listener.startedInfo.isBlitzMode());
  }

  @Test
  void handleGameStartMessageSupportsBlitzBlack() {
    AsyncGameEventHandler handler = new AsyncGameEventHandler();
    AsyncEventParser parser = new AsyncEventParser();
    RecordingListener listener = new RecordingListener();

    handler.handleGameStartMessage(
        "GAME_STARTED GAME_ID=8 COLOR=BLACK WHITE=Bob BLACK=Alice MODE=BLITZ", listener, parser);

    assertNotNull(listener.startedInfo);
    assertEquals(8, listener.startedInfo.getGameId());
    assertEquals(Color.BLACK, listener.startedInfo.getLocalColor());
    assertTrue(listener.startedInfo.isBlitzMode());
  }

  @Test
  void handleGameStartMessageSupportsLowerCaseColorAndMode() {
    AsyncGameEventHandler handler = new AsyncGameEventHandler();
    AsyncEventParser parser = new AsyncEventParser();
    RecordingListener listener = new RecordingListener();

    handler.handleGameStartMessage(
        "GAME_STARTED GAME_ID=12 COLOR=white WHITE=Alice BLACK=Bob MODE=blitz", listener, parser);

    assertNotNull(listener.startedInfo);
    assertEquals(12, listener.startedInfo.getGameId());
    assertEquals(Color.WHITE, listener.startedInfo.getLocalColor());
    assertTrue(listener.startedInfo.isBlitzMode());
  }

  @Test
  void handleGameStartMessageRejectsMissingField() {
    AsyncGameEventHandler handler = new AsyncGameEventHandler();
    AsyncEventParser parser = new AsyncEventParser();
    RecordingListener listener = new RecordingListener();

    handler.handleGameStartMessage(
        "GAME_STARTED GAME_ID=7 COLOR=WHITE WHITE=Alice MODE=NORMAL", listener, parser);

    assertNull(listener.startedInfo);
  }

  @Test
  void handleGameStartMessageRejectsInvalidGameId() {
    AsyncGameEventHandler handler = new AsyncGameEventHandler();
    AsyncEventParser parser = new AsyncEventParser();
    RecordingListener listener = new RecordingListener();

    handler.handleGameStartMessage(
        "GAME_STARTED GAME_ID=X COLOR=WHITE WHITE=Alice BLACK=Bob MODE=NORMAL", listener, parser);

    assertNull(listener.startedInfo);
  }

  @Test
  void handleGameStartMessageRejectsInvalidColor() {
    AsyncGameEventHandler handler = new AsyncGameEventHandler();
    AsyncEventParser parser = new AsyncEventParser();
    RecordingListener listener = new RecordingListener();

    handler.handleGameStartMessage(
        "GAME_STARTED GAME_ID=7 COLOR=BLUE WHITE=Alice BLACK=Bob MODE=NORMAL", listener, parser);

    assertNull(listener.startedInfo);
  }

  @Test
  void handleGameStartMessageSupportsNullListener() {
    AsyncGameEventHandler handler = new AsyncGameEventHandler();
    AsyncEventParser parser = new AsyncEventParser();

    assertDoesNotThrow(
        () ->
            handler.handleGameStartMessage(
                "GAME_STARTED GAME_ID=9 COLOR=BLACK WHITE=Bob BLACK=Alice MODE=BLITZ",
                null,
                parser));
  }
}
