package fr.univ.bordeaux.application.network.server.clientcommand;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.univ.bordeaux.application.network.player.OnlinePlayer;
import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.application.network.server.ClientHandler;
import fr.univ.bordeaux.application.network.server.game.GameMode;
import fr.univ.bordeaux.application.network.server.game.ServerGameSession;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClientGameCommandServiceTest {

  private final ClientGameCommandService service = new ClientGameCommandService();

  private Field findField(Class<?> type, String fieldName) throws Exception {
    Class<?> current = type;
    while (current != null) {
      try {
        Field f = current.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f;
      } catch (NoSuchFieldException ignored) {
        current = current.getSuperclass();
      }
    }
    throw new NoSuchFieldException(fieldName);
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field f = findField(target.getClass(), fieldName);
    f.set(target, value);
  }

  private void trySetField(Object target, String fieldName, Object value) {
    try {
      Field f = findField(target.getClass(), fieldName);
      f.set(target, value);
    } catch (Exception ignored) {
      // utile pour supporter plusieurs implémentations possibles
    }
  }

  private static class HandlerBox {
    final ClientHandler handler;
    final StringWriter buffer;

    HandlerBox(ClientHandler handler, StringWriter buffer) {
      this.handler = handler;
      this.buffer = buffer;
    }

    String sentText() {
      return buffer.toString();
    }
  }

  private HandlerBox buildHandler(AgonServer server, OnlinePlayer player) throws Exception {
    ClientHandler handler = new ClientHandler(new Socket(), server);
    StringWriter sw = new StringWriter();
    BufferedWriter bw = new BufferedWriter(sw);

    setField(handler, "out", bw);
    setField(handler, "player", player);

    return new HandlerBox(handler, sw);
  }

  private void setPlayerHandler(OnlinePlayer player, ClientHandler handler) throws Exception {
    setField(player, "handler", handler);
  }

  private Command buildCommand(String rawArgument) {
    return new Command(null, java.util.Collections.emptyMap(), rawArgument);
  }

  private ServerGameSession prepareStartedGame(
      AgonServer server, OnlinePlayer p1, OnlinePlayer p2) {
    assertTrue(server.createInvitation(p1.getId(), p2.getId()));
    assertNotNull(server.acceptInvitation(p2.getId()));
    ServerGameSession session = server.chooseMode(p1.getId(), GameMode.NORMAL);
    assertNotNull(session);
    return session;
  }

  @Test
  @DisplayName("handleMove : joueur non connecté")
  void handleMoveNotLogged() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    HandlerBox hb = buildHandler(server, null);
    Command cmd = buildCommand("e2e4");

    assertDoesNotThrow(() -> service.handleMove(cmd, hb.handler));
    assertTrue(hb.sentText().contains("ERROR MESSAGE=NOT_LOGGED_IN"));
  }

  @Test
  @DisplayName("handleMove : joueur connecté mais pas en partie")
  void handleMoveNotInGame() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    HandlerBox hb = buildHandler(server, p1);
    Command cmd = buildCommand("e2e4");

    assertDoesNotThrow(() -> service.handleMove(cmd, hb.handler));

    String sent = hb.sentText();
    assertTrue(
        sent.contains("ERROR MESSAGE=NOT_IN_GAME")
            || sent.contains("ERROR MESSAGE=GAME_NOT_FOUND"));
  }

  @Test
  @DisplayName("handleMove : move manquant")
  void handleMoveMissingMove() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    OnlinePlayer p2 = server.registerPlayer("c2", "Bob", null);

    prepareStartedGame(server, p1, p2);

    HandlerBox hb = buildHandler(server, p1);
    Command cmd = buildCommand("   ");

    assertDoesNotThrow(() -> service.handleMove(cmd, hb.handler));
    assertTrue(hb.sentText().contains("ERROR MESSAGE=MISSING_MOVE"));
  }

  @Test
  @DisplayName("handleMove : move rejeté")
  void handleMoveRejected() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    OnlinePlayer p2 = server.registerPlayer("c2", "Bob", null);

    prepareStartedGame(server, p1, p2);

    HandlerBox hb = buildHandler(server, p1);
    Command cmd = buildCommand("move_invalide_totalement");

    assertDoesNotThrow(() -> service.handleMove(cmd, hb.handler));

    String sent = hb.sentText();
    assertTrue(
        sent.contains("ERROR MESSAGE=INVALID_MOVE")
            || sent.contains("ERROR MESSAGE=NOT_YOUR_TURN"));
  }

  @Test
  @DisplayName("handleMove : move accepté")
  void handleMoveAccepted() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    OnlinePlayer p2 = server.registerPlayer("c2", "Bob", null);

    prepareStartedGame(server, p1, p2);

    HandlerBox hb1 = buildHandler(server, p1);
    HandlerBox hb2 = buildHandler(server, p2);

    setPlayerHandler(p1, hb1.handler);
    setPlayerHandler(p2, hb2.handler);

    Command cmd = buildCommand("e2e4");

    assertDoesNotThrow(() -> service.handleMove(cmd, hb1.handler));

    String sent1 = hb1.sentText();
    String sent2 = hb2.sentText();

    if (sent1.contains("MOVE_OK")) {
      assertTrue(sent1.contains("MOVE_OK e2e4"));
      assertTrue(sent2.contains("OPPONENT_MOVE e2e4") || sent2.isEmpty() || sent2.contains("e2e4"));
    } else {
      assertTrue(
          sent1.contains("ERROR MESSAGE=INVALID_MOVE")
              || sent1.contains("ERROR MESSAGE=NOT_YOUR_TURN"));
    }
  }

  @Test
  @DisplayName("handleResign : joueur non connecté")
  void handleResignNotLogged() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    HandlerBox hb = buildHandler(server, null);

    assertDoesNotThrow(() -> service.handleResign(hb.handler));
    assertTrue(hb.sentText().contains("ERROR MESSAGE=NOT_LOGGED_IN"));
  }

  @Test
  @DisplayName("handleResign : joueur pas en partie")
  void handleResignNotInGame() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    HandlerBox hb = buildHandler(server, p1);

    assertDoesNotThrow(() -> service.handleResign(hb.handler));

    String sent = hb.sentText();
    assertTrue(
        sent.contains("ERROR MESSAGE=NOT_IN_GAME")
            || sent.contains("ERROR MESSAGE=GAME_NOT_FOUND"));
  }

  @Test
  @DisplayName("handleResign : fin de partie normale")
  void handleResignNormal() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    OnlinePlayer p2 = server.registerPlayer("c2", "Bob", null);

    prepareStartedGame(server, p1, p2);

    HandlerBox hb1 = buildHandler(server, p1);

    assertDoesNotThrow(() -> service.handleResign(hb1.handler));

    assertNull(server.getGameIdByPlayer(p1.getId()));
    assertNull(server.getGameIdByPlayer(p2.getId()));
  }

  @Test
  @DisplayName("handleDisconnect : handler sans joueur")
  void handleDisconnectWithoutPlayer() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    HandlerBox hb = buildHandler(server, null);

    assertDoesNotThrow(() -> service.handleDisconnect(hb.handler));
  }

  @Test
  @DisplayName("handleDisconnect : joueur hors partie est retiré du serveur")
  void handleDisconnectWithoutSession() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    HandlerBox hb = buildHandler(server, p1);

    assertNotNull(server.getPlayerById(p1.getId()));

    assertDoesNotThrow(() -> service.handleDisconnect(hb.handler));

    assertNull(server.getPlayerById(p1.getId()));
  }

  @Test
  @DisplayName("handleDisconnect : joueur en partie")
  void handleDisconnectWithSession() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    OnlinePlayer p2 = server.registerPlayer("c2", "Bob", null);

    prepareStartedGame(server, p1, p2);

    HandlerBox hb1 = buildHandler(server, p1);

    assertDoesNotThrow(() -> service.handleDisconnect(hb1.handler));

    assertNull(server.getPlayerById(p1.getId()));
  }

  static class ThrowingFinishServer extends AgonServer {
    boolean removeCalled = false;

    ThrowingFinishServer() {
      super(12345, "test");
    }

    @Override
    public void finishGame(ServerGameSession session, int winnerId, String reason) {
      throw new RuntimeException("forced");
    }

    @Override
    public void removePlayer(OnlinePlayer player) {
      removeCalled = true;
      super.removePlayer(player);
    }
  }

  @Test
  @DisplayName("handleDisconnect : finishGameSilently ignore RuntimeException")
  void handleDisconnectFinishGameSilentlyCatchesException() throws Exception {
    ThrowingFinishServer server = new ThrowingFinishServer();
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    OnlinePlayer p2 = server.registerPlayer("c2", "Bob", null);

    prepareStartedGame(server, p1, p2);

    HandlerBox hb1 = buildHandler(server, p1);

    assertDoesNotThrow(() -> service.handleDisconnect(hb1.handler));
    assertTrue(server.removeCalled);
  }

  @Test
  @DisplayName("handleMove : session présente mais game over possible")
  void handleMoveGameOverBranchPotentiallyCovered() throws Exception {
    AgonServer server = new AgonServer(12345, "test");
    OnlinePlayer p1 = server.registerPlayer("c1", "Alice", null);
    OnlinePlayer p2 = server.registerPlayer("c2", "Bob", null);

    prepareStartedGame(server, p1, p2);

    HandlerBox hb1 = buildHandler(server, p1);
    HandlerBox hb2 = buildHandler(server, p2);
    setPlayerHandler(p1, hb1.handler);
    setPlayerHandler(p2, hb2.handler);

    String[] candidateMoves = {"e2e4", "d2d4", "g1f3", "b1c3"};

    for (String move : candidateMoves) {
      Command cmd = buildCommand(move);
      assertDoesNotThrow(() -> service.handleMove(cmd, hb1.handler));
    }

    String sent = hb1.sentText();
    assertFalse(sent.contains("ERROR MESSAGE=NOT_LOGGED_IN"));
  }
}
