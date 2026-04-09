package fr.univ.bordeaux.application.network.client.runtime;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.network.OnlineGameInfo;
import fr.univ.bordeaux.application.network.OnlineGameStartListener;
import org.junit.jupiter.api.Test;

class ClientAsyncEventHandlerTest {

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
  void isAsyncEventDelegatesToParser() {
    ClientAsyncEventHandler handler = new ClientAsyncEventHandler();

    assertTrue(handler.isAsyncEvent("MOVE_OK e2e4"));
    assertTrue(handler.isAsyncEvent("GAME_STARTED GAME_ID=1"));
    assertTrue(handler.isAsyncEvent("ERROR MESSAGE=INVALID_MOVE"));

    assertFalse(handler.isAsyncEvent(null));
    assertFalse(handler.isAsyncEvent(""));
    assertFalse(handler.isAsyncEvent("   "));
    assertFalse(handler.isAsyncEvent("STATUS_OK port=12345"));
  }

  @Test
  void dispatchesMainBranches() {
    ClientAsyncEventHandler handler = new ClientAsyncEventHandler();
    RecordingListener listener = new RecordingListener();
    handler.setGameStartListener(listener);

    handler.handleAsyncEvent("MOVE_OK e2e4");
    handler.handleAsyncEvent("OPPONENT_MOVE e7e5");
    handler.handleAsyncEvent("GAME_OVER RESULT=WIN REASON=END");
    handler.handleAsyncEvent(
        "GAME_STARTED GAME_ID=3 COLOR=BLACK WHITE=Bob BLACK=Alice MODE=NORMAL");

    assertEquals("e2e4", listener.localMove);
    assertEquals("e7e5", listener.opponentMove);
    assertEquals("GAME_OVER RESULT=WIN REASON=END", listener.gameOverLine);
    assertNotNull(listener.startedInfo);
    assertEquals(3, listener.startedInfo.getGameId());
    assertEquals(Color.BLACK, listener.startedInfo.getLocalColor());
  }

  @Test
  void errorEventsRefreshBoard() {
    ClientAsyncEventHandler handler = new ClientAsyncEventHandler();
    RecordingListener listener = new RecordingListener();
    handler.setGameStartListener(listener);

    handler.handleAsyncEvent("ERROR MESSAGE=INVALID_MOVE");
    handler.handleAsyncEvent("ERROR MESSAGE=NOT_YOUR_TURN");
    handler.handleAsyncEvent("ERROR MESSAGE=MISSING_MOVE");
    handler.handleAsyncEvent("ERROR MESSAGE=NOT_IN_GAME");
    handler.handleAsyncEvent("ERROR MESSAGE=GAME_NOT_FOUND");
    handler.handleAsyncEvent("ERROR MESSAGE=SOMETHING_ELSE");

    assertEquals(6, listener.refreshCount);
  }

  @Test
  void ignoresNullOrBlank() {
    ClientAsyncEventHandler handler = new ClientAsyncEventHandler();
    RecordingListener listener = new RecordingListener();
    handler.setGameStartListener(listener);

    handler.handleAsyncEvent(null);
    handler.handleAsyncEvent("");
    handler.handleAsyncEvent("   ");

    assertNull(listener.localMove);
    assertNull(listener.opponentMove);
    assertNull(listener.startedInfo);
    assertNull(listener.gameOverLine);
    assertEquals(0, listener.refreshCount);
  }

  @Test
  void handlesMoveBranchesWithBlankPayload() {
    ClientAsyncEventHandler handler = new ClientAsyncEventHandler();
    RecordingListener listener = new RecordingListener();
    handler.setGameStartListener(listener);

    handler.handleAsyncEvent("MOVE_OK   ");
    handler.handleAsyncEvent("OPPONENT_MOVE   ");

    assertNull(listener.localMove);
    assertNull(listener.opponentMove);
  }

  @Test
  void handlesGameStartInvalidCases() {
    ClientAsyncEventHandler handler = new ClientAsyncEventHandler();
    RecordingListener listener = new RecordingListener();
    handler.setGameStartListener(listener);

    handler.handleAsyncEvent("GAME_STARTED GAME_ID=7 COLOR=WHITE WHITE=Alice MODE=NORMAL");
    assertNull(listener.startedInfo);

    handler.handleAsyncEvent(
        "GAME_STARTED GAME_ID=X COLOR=WHITE WHITE=Alice BLACK=Bob MODE=NORMAL");
    assertNull(listener.startedInfo);
  }

  @Test
  void dispatchesInfoOnlyWaitingModeAndUnknownEventsWithoutTouchingListener() {
    ClientAsyncEventHandler handler = new ClientAsyncEventHandler();
    RecordingListener listener = new RecordingListener();
    handler.setGameStartListener(listener);

    handler.handleAsyncEvent("INVITATION_SENT PLAYER=Bob");
    handler.handleAsyncEvent("INVITATION_RECEIVED FROM=Alice");
    handler.handleAsyncEvent("CHOOSE_MODE COMMAND=mode OPTIONS=normal|blitz");
    handler.handleAsyncEvent("DECLINE_OK");
    handler.handleAsyncEvent("WAITING_MODE");
    handler.handleAsyncEvent("SOMETHING_UNKNOWN foo=bar");

    assertNull(listener.startedInfo);
    assertNull(listener.localMove);
    assertNull(listener.opponentMove);
    assertNull(listener.gameOverLine);
    assertEquals(0, listener.refreshCount);
  }

  @Test
  void supportsNullListener() {
    ClientAsyncEventHandler handler = new ClientAsyncEventHandler();
    handler.setGameStartListener(null);

    assertDoesNotThrow(() -> handler.handleAsyncEvent("MOVE_OK e2e4"));
    assertDoesNotThrow(
        () ->
            handler.handleAsyncEvent(
                "GAME_STARTED GAME_ID=8 COLOR=BLACK WHITE=Bob BLACK=Alice MODE=BLITZ"));
    assertDoesNotThrow(() -> handler.handleAsyncEvent("ERROR MESSAGE=INVALID_MOVE"));
    assertDoesNotThrow(() -> handler.handleAsyncEvent("INVITATION_SENT PLAYER=Bob"));
    assertDoesNotThrow(() -> handler.handleAsyncEvent("WAITING_MODE"));
    assertDoesNotThrow(() -> handler.handleAsyncEvent("UNKNOWN_EVENT anything"));
  }
}
