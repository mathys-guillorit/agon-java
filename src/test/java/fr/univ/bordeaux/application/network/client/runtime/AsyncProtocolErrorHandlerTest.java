package fr.univ.bordeaux.application.network.client.runtime;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.application.network.OnlineGameInfo;
import fr.univ.bordeaux.application.network.OnlineGameStartListener;
import org.junit.jupiter.api.Test;

class AsyncProtocolErrorHandlerTest {

  private static class RecordingListener implements OnlineGameStartListener {
    int refreshCount;

    @Override
    public void onOnlineGameStarted(OnlineGameInfo info) {}

    @Override
    public void onLocalMoveConfirmed(String rawMove) {}

    @Override
    public void onOpponentMoveReceived(String rawMove) {}

    @Override
    public void onGameOver(String line) {}

    @Override
    public void onOnlineBoardRefreshRequested() {
      refreshCount++;
    }
  }

  @Test
  void refreshesBoardForEachKnownErrorCase() {
    AsyncProtocolErrorHandler handler = new AsyncProtocolErrorHandler();
    RecordingListener listener = new RecordingListener();

    handler.handleProtocolError("ERROR MESSAGE=INVALID_MOVE", listener);
    handler.handleProtocolError("ERROR MESSAGE=NOT_YOUR_TURN", listener);
    handler.handleProtocolError("ERROR MESSAGE=MISSING_MOVE", listener);
    handler.handleProtocolError("ERROR MESSAGE=NOT_IN_GAME", listener);
    handler.handleProtocolError("ERROR MESSAGE=GAME_NOT_FOUND", listener);

    assertEquals(5, listener.refreshCount);
  }

  @Test
  void refreshesBoardForUnknownErrorCase() {
    AsyncProtocolErrorHandler handler = new AsyncProtocolErrorHandler();
    RecordingListener listener = new RecordingListener();

    handler.handleProtocolError("ERROR MESSAGE=SOMETHING_ELSE", listener);

    assertEquals(1, listener.refreshCount);
  }

  @Test
  void supportsNullListenerForKnownAndUnknownErrors() {
    AsyncProtocolErrorHandler handler = new AsyncProtocolErrorHandler();

    assertDoesNotThrow(() -> handler.handleProtocolError("ERROR MESSAGE=INVALID_MOVE", null));
    assertDoesNotThrow(() -> handler.handleProtocolError("ERROR MESSAGE=NOT_YOUR_TURN", null));
    assertDoesNotThrow(() -> handler.handleProtocolError("ERROR MESSAGE=MISSING_MOVE", null));
    assertDoesNotThrow(() -> handler.handleProtocolError("ERROR MESSAGE=NOT_IN_GAME", null));
    assertDoesNotThrow(() -> handler.handleProtocolError("ERROR MESSAGE=GAME_NOT_FOUND", null));
    assertDoesNotThrow(() -> handler.handleProtocolError("ERROR MESSAGE=SOMETHING_ELSE", null));
  }
}
