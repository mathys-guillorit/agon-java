package fr.univ.bordeaux.application.network.client.runtime;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AsyncEventParserTest {

  private final AsyncEventParser parser = new AsyncEventParser();

  @Test
  void isAsyncEventRecognizesSupportedEvents() {
    assertTrue(parser.isAsyncEvent("GAME_STARTED GAME_ID=1"));
    assertTrue(parser.isAsyncEvent("NEW_OK GAME_ID=1"));
    assertTrue(parser.isAsyncEvent("MOVE_OK e2e4"));
    assertTrue(parser.isAsyncEvent("OPPONENT_MOVE e7e5"));
    assertTrue(parser.isAsyncEvent("GAME_OVER RESULT=WIN"));
    assertTrue(parser.isAsyncEvent("YOUR_TURN"));
    assertTrue(parser.isAsyncEvent("ERROR MESSAGE=INVALID_MOVE"));
    assertTrue(parser.isAsyncEvent("INVITATION_SENT PLAYER=Bob"));
    assertTrue(parser.isAsyncEvent("INVITATION_RECEIVED FROM=Alice"));
    assertTrue(parser.isAsyncEvent("INVITATION_ACCEPTED PLAYER=Bob"));
    assertTrue(parser.isAsyncEvent("INVITATION_DECLINED PLAYER=Bob"));
    assertTrue(parser.isAsyncEvent("INVITATION_CANCELED PLAYER=Bob"));
    assertTrue(parser.isAsyncEvent("LOBBY_JOINED HOST=Alice"));
    assertTrue(parser.isAsyncEvent("WAITING_MODE"));
    assertTrue(parser.isAsyncEvent("CHOOSE_MODE COMMAND=mode OPTIONS=normal|blitz"));
    assertTrue(parser.isAsyncEvent("DECLINE_OK"));
  }

  @Test
  void isAsyncEventRejectsInvalidLines() {
    assertFalse(parser.isAsyncEvent(null));
    assertFalse(parser.isAsyncEvent(""));
    assertFalse(parser.isAsyncEvent("   "));
    assertFalse(parser.isAsyncEvent("STATUS_OK port=12345"));
  }

  @Test
  void extractEventTypeWorks() {
    assertEquals("ERROR", parser.extractEventType("ERROR MESSAGE=INVALID_MOVE"));
    assertEquals("GAME_STARTED", parser.extractEventType("GAME_STARTED GAME_ID=1"));
    assertEquals("WAITING_MODE", parser.extractEventType("WAITING_MODE"));
  }

  @Test
  void parseProtocolArgsWorks() {
    Map<String, String> ok =
        parser.parseProtocolArgs(
            "GAME_STARTED GAME_ID=5 COLOR=WHITE WHITE=Alice BLACK=Bob MODE=NORMAL");
    assertEquals("5", ok.get("GAME_ID"));
    assertEquals("WHITE", ok.get("COLOR"));
    assertEquals("Alice", ok.get("WHITE"));
    assertEquals("Bob", ok.get("BLACK"));
    assertEquals("NORMAL", ok.get("MODE"));

    assertTrue(parser.parseProtocolArgs(null).isEmpty());
    assertTrue(parser.parseProtocolArgs("   ").isEmpty());

    Map<String, String> partial = parser.parseProtocolArgs("X bad =y A=1");
    assertEquals("1", partial.get("A"));
    assertFalse(partial.containsKey("bad"));
  }

  @Test
  void helperClassificationsWork() {
    assertTrue(parser.isGameStartEvent("GAME_STARTED"));
    assertTrue(parser.isGameStartEvent("NEW_OK"));
    assertFalse(parser.isGameStartEvent("MOVE_OK"));

    assertTrue(parser.isInfoOnlyEvent("INVITATION_SENT"));
    assertTrue(parser.isInfoOnlyEvent("CHOOSE_MODE"));
    assertFalse(parser.isInfoOnlyEvent("GAME_OVER"));
  }
}
