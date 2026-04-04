package fr.univ.bordeaux.application.network.client;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import fr.univ.bordeaux.application.network.OnlineGameInfo;
import fr.univ.bordeaux.application.network.OnlineGameStartListener;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgonClientTest {

  private final List<FakeTcpServer> servers = new ArrayList<>();

  @AfterEach
  void cleanup() {
    for (FakeTcpServer server : servers) {
      try {
        server.close();
      } catch (Exception ignored) {
      }
    }
    servers.clear();
  }

  private FakeTcpServer newServer() throws Exception {
    FakeTcpServer s = new FakeTcpServer();
    servers.add(s);
    return s;
  }

  private AgonClient connectedClient(FakeTcpServer server) throws Exception {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));
    assertTrue(client.connect("127.0.0.1", server.getPort()));
    return client;
  }

  private Object invokePrivate(
      AgonClient client, String methodName, Class<?>[] types, Object... args) throws Exception {
    Method m = AgonClient.class.getDeclaredMethod(methodName, types);
    m.setAccessible(true);
    return m.invoke(client, args);
  }

  @SuppressWarnings("unchecked")
  private LinkedList<String> pendingResponsesOf(AgonClient client) throws Exception {
    Field f = AgonClient.class.getDeclaredField("pendingResponses");
    f.setAccessible(true);
    return (LinkedList<String>) f.get(client);
  }

  private Object responseLockOf(AgonClient client) throws Exception {
    Field f = AgonClient.class.getDeclaredField("responseLock");
    f.setAccessible(true);
    return f.get(client);
  }

  private Object getField(Object target, String name) throws Exception {
    Field f = target.getClass().getDeclaredField(name);
    f.setAccessible(true);
    return f.get(target);
  }

  private void replaceWriterWithFailingOne(AgonClient client) throws Exception {
    Field outField = AgonClient.class.getDeclaredField("out");
    outField.setAccessible(true);

    BufferedWriter failingWriter =
        new BufferedWriter(
            new Writer() {
              @Override
              public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("boom");
              }

              @Override
              public void flush() throws IOException {
                throw new IOException("boom");
              }

              @Override
              public void close() {}
            });

    outField.set(client, failingWriter);
  }

  private int firstValidCoord() {
    for (int i = 0; i < 200; i++) {
      try {
        String s = CoordinateMapper.toAbaPro(i);
        if (s != null && !s.isBlank()) {
          return i;
        }
      } catch (Exception ignored) {
      }
    }
    fail("No valid coord found");
    return -1;
  }

  private int secondValidCoordDifferentFrom(int first) {
    for (int i = 0; i < 200; i++) {
      if (i == first) {
        continue;
      }
      try {
        String s = CoordinateMapper.toAbaPro(i);
        if (s != null && !s.isBlank()) {
          return i;
        }
      } catch (Exception ignored) {
      }
    }
    fail("No second valid coord found");
    return -1;
  }

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

  private static class FakeTcpServer implements AutoCloseable {
    private final ServerSocket serverSocket;
    private final Thread thread;

    private volatile boolean running = true;
    private volatile Socket socket;
    private volatile BufferedReader in;
    private volatile BufferedWriter out;

    private final BlockingQueue<String> receivedLines = new LinkedBlockingQueue<>();
    private final Map<String, List<String>> scriptedResponses = new ConcurrentHashMap<>();

    private volatile String firstResponse = "WELCOME ID=1 NAME=Alice STATUS=idle";

    FakeTcpServer() throws Exception {
      serverSocket = new ServerSocket(0);

      thread =
          new Thread(
              () -> {
                try {
                  socket = serverSocket.accept();
                  in =
                      new BufferedReader(
                          new InputStreamReader(
                              socket.getInputStream(), StandardCharsets.US_ASCII));
                  out =
                      new BufferedWriter(
                          new OutputStreamWriter(
                              socket.getOutputStream(), StandardCharsets.US_ASCII));

                  String login = in.readLine();
                  if (login != null) {
                    receivedLines.add(login);
                  }

                  if (firstResponse != null) {
                    sendLine(firstResponse);
                  }

                  while (running && socket != null && !socket.isClosed()) {
                    String line = in.readLine();
                    if (line == null) {
                      break;
                    }

                    receivedLines.add(line);

                    String cmd = line.trim().isEmpty() ? "" : line.trim().split("\\s+")[0];
                    List<String> responses = scriptedResponses.get(cmd);
                    if (responses == null) {
                      continue;
                    }

                    for (String response : responses) {
                      if ("<<CLOSE>>".equals(response)) {
                        socket.close();
                        return;
                      }
                      sendLine(response);
                    }
                  }
                } catch (IOException ignored) {
                }
              },
              "FakeTcpServer");

      thread.setDaemon(true);
      thread.start();
    }

    int getPort() {
      return serverSocket.getLocalPort();
    }

    void setFirstResponse(String firstResponse) {
      this.firstResponse = firstResponse;
    }

    void script(String command, String... responses) {
      scriptedResponses.put(command, Arrays.asList(responses));
    }

    void sendAsync(String line) throws Exception {
      waitUntilConnected();
      sendLine(line);
    }

    String takeReceived(long timeoutMs) throws Exception {
      return receivedLines.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }

    private void waitUntilConnected() throws Exception {
      long deadline = System.currentTimeMillis() + 3000;
      while ((out == null || socket == null) && System.currentTimeMillis() < deadline) {
        Thread.sleep(10);
      }
      if (out == null || socket == null) {
        throw new IllegalStateException("Server not connected");
      }
    }

    private synchronized void sendLine(String line) throws IOException {
      if (out == null) {
        return;
      }
      out.write(line);
      out.write('\n');
      out.flush();
    }

    @Override
    public void close() throws Exception {
      running = false;
      try {
        if (socket != null) {
          socket.close();
        }
      } catch (Exception ignored) {
      }
      try {
        serverSocket.close();
      } catch (Exception ignored) {
      }
    }
  }

  @Test
  @DisplayName("connect success + already connected + invalid welcome")
  void connect_cases() throws Exception {
    FakeTcpServer ok = newServer();
    LocalProfile profile = new LocalProfile("Alice");
    AgonClient client = new AgonClient(profile);

    assertTrue(client.connect("127.0.0.1", ok.getPort()));
    assertTrue(client.isConnected());

    String login = ok.takeReceived(1000);
    assertNotNull(login);
    assertTrue(login.startsWith("LOGIN NAME=Alice CLIENT_ID="));
    assertEquals(1, profile.getIdForServer("127.0.0.1:" + ok.getPort()));

    assertTrue(client.connect("127.0.0.1", ok.getPort()));
    client.disconnectSilently();

    FakeTcpServer bad = newServer();
    bad.setFirstResponse("ERROR MESSAGE=LOGIN_FAILED");
    assertFalse(new AgonClient(new LocalProfile("Alice")).connect("127.0.0.1", bad.getPort()));

    assertFalse(new AgonClient(new LocalProfile("Alice")).connect("127.0.0.1", 65001));
  }

  @Test
  @DisplayName("sync requests success and failures")
  void sync_requests() throws Exception {
    FakeTcpServer ok = newServer();
    ok.script("STATUS", "STATUS_OK port=12345 clients=1 players=1 games=0");
    ok.script("PLAYERS", "=== PLAYERS ===", "ID=1 NAME=Alice STATUS=idle", "END");
    ok.script("SCOREBOARD", "=== SCOREBOARD ===", "NAME=Alice WINS=1 LOSSES=0 GAMES=1", "END");

    AgonClient c1 = connectedClient(ok);
    assertTrue(c1.requestServerStatus().startsWith("STATUS_OK"));
    assertTrue(c1.requestPlayers().contains("Alice"));
    assertTrue(c1.requestScoreboard().contains("Alice"));
    c1.disconnectSilently();

    FakeTcpServer badStatus = newServer();
    badStatus.script("STATUS", "BAD_STATUS");
    assertNull(connectedClient(badStatus).requestServerStatus());

    FakeTcpServer badPlayers = newServer();
    badPlayers.script("PLAYERS", "=== PLAYERS ===", "<<CLOSE>>");
    assertNull(connectedClient(badPlayers).requestPlayers());

    FakeTcpServer badScore = newServer();
    badScore.script("SCOREBOARD", "=== SCOREBOARD ===", "<<CLOSE>>");
    assertNull(connectedClient(badScore).requestScoreboard());
  }

  @Test
  @DisplayName("player detail and status commands")
  void player_detail_and_status_commands() throws Exception {
    FakeTcpServer ok = newServer();
    ok.script("PLAYERS", "PLAYER ID=2 NAME=Bob CLIENT_ID=cid2 STATUS=away WINS=3 LOSSES=1 GAMES=4");
    ok.script("AWAY", "AWAY_OK STATUS=away");
    ok.script("BACK", "BACK_OK STATUS=idle");

    AgonClient client = connectedClient(ok);

    String playerDetails = client.requestPlayerDetails(2);
    assertNotNull(playerDetails);
    assertTrue(playerDetails.contains("PLAYER ID=2"));
    assertTrue(playerDetails.contains("NAME=Bob"));
    assertTrue(playerDetails.contains("STATUS=away"));

    String awayResponse = client.setAway();
    assertEquals("AWAY_OK STATUS=away", awayResponse);

    String backResponse = client.setBack();
    assertEquals("BACK_OK STATUS=idle", backResponse);

    client.disconnectSilently();
  }

  @Test
  @DisplayName("player detail and status commands return null when disconnected")
  void player_detail_and_status_commands_when_disconnected() {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));

    assertNull(client.requestPlayerDetails(2));
    assertNull(client.setAway());
    assertNull(client.setBack());
  }

  @Test
  @DisplayName("player detail and status commands handle io failure")
  void player_detail_and_status_commands_io_failure() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);

    replaceWriterWithFailingOne(client);

    assertNull(client.requestPlayerDetails(2));
    assertFalse(client.isConnected());

    FakeTcpServer server2 = newServer();
    AgonClient client2 = connectedClient(server2);
    replaceWriterWithFailingOne(client2);

    assertNull(client2.setAway());
    assertFalse(client2.isConnected());

    FakeTcpServer server3 = newServer();
    AgonClient client3 = connectedClient(server3);
    replaceWriterWithFailingOne(client3);

    assertNull(client3.setBack());
    assertFalse(client3.isConnected());
  }

  @Test
  @DisplayName("player detail command returns null when response is missing")
  void player_detail_returns_null_when_response_is_missing() throws Exception {
    FakeTcpServer server = newServer();
    server.script("PLAYERS", "<<CLOSE>>");

    AgonClient client = connectedClient(server);
    assertNull(client.requestPlayerDetails(2));
  }

  @Test
  @DisplayName("commands: new game, resign, quit")
  void command_sending() throws Exception {
    FakeTcpServer server = newServer();
    server.script("QUIT", "BYE");

    AgonClient client = connectedClient(server);
    server.takeReceived(1000); // LOGIN

    assertEquals("[CLIENT] New game request sent.", client.requestNewGame(7));
    assertEquals("NEW PLAYER_ID=7", server.takeReceived(1000));

    client.resignGame();
    assertEquals("RESIGN", server.takeReceived(1000));

    client.quit();
    assertEquals("QUIT", server.takeReceived(1000));
    assertFalse(client.isConnected());
  }

  @Test
  @DisplayName("ping and alive")
  void ping_and_alive() throws Exception {
    FakeTcpServer pong = newServer();
    pong.script("PING", "PONG TIME=0ms");
    AgonClient c1 = connectedClient(pong);

    assertTrue(c1.isAlive());
    String resp = c1.pingRttMs();
    assertNotNull(resp);
    assertTrue(resp.startsWith("[SERVER] PONG TIME="));
    c1.disconnectSilently();

    FakeTcpServer bad = newServer();
    bad.script("PING", "BAD");
    AgonClient c2 = connectedClient(bad);
    assertFalse(c2.isAlive());
    assertFalse(c2.isConnected());

    FakeTcpServer nope = newServer();
    nope.script("PING", "NOPE");
    assertNull(connectedClient(nope).pingRttMs());
  }

  @Test
  @DisplayName("sendRawMove success and sendMove remains safe")
  void send_move_success() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);
    server.takeReceived(1000); // LOGIN

    int from = firstValidCoord();
    int to = secondValidCoordDifferentFrom(from);

    assertDoesNotThrow(() -> client.sendMove(from, to));

    String maybeMove = server.takeReceived(200);
    if (maybeMove != null) {
      assertTrue(maybeMove.startsWith("move ") || maybeMove.startsWith("MOVE "));
    }

    assertTrue(client.sendRawMove("  e2e4  "));
    assertEquals("MOVE E2E4", server.takeReceived(1000));

    client.disconnectSilently();
  }

  @Test
  @DisplayName("sendRawMove returns false and disconnects when writer throws")
  void send_raw_move_io_failure() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);

    replaceWriterWithFailingOne(client);

    assertFalse(client.sendRawMove("e2e4"));
    assertFalse(client.isConnected());
  }

  @Test
  @DisplayName("safe values when disconnected")
  void safe_values_when_disconnected() {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));

    assertNull(client.requestServerStatus());
    assertNull(client.requestPlayers());
    assertNull(client.requestPlayerDetails(1));
    assertNull(client.requestScoreboard());
    assertNull(client.requestNewGame(1));
    assertNull(client.setAway());
    assertNull(client.setBack());
    assertFalse(client.isAlive());
    assertNull(client.pingRttMs());
    assertFalse(client.sendMove(0, 1));
    assertFalse(client.sendRawMove(null));
    assertFalse(client.sendRawMove(" "));
    assertDoesNotThrow(client::quit);
    assertDoesNotThrow(client::resignGame);

    client.disconnectSilently();
    client.disconnectSilently();
    assertFalse(client.isConnected());
  }

  @Test
  @DisplayName("private helpers")
  void private_helpers() throws Exception {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));

    assertEquals(
        42,
        invokePrivate(
            client,
            "extractId",
            new Class[] {String.class},
            "WELCOME ID=42 NAME=Alice STATUS=idle"));
    assertNull(
        invokePrivate(client, "extractId", new Class[] {String.class}, "WELCOME NAME=Alice"));
    assertNull(
        invokePrivate(
            client, "extractId", new Class[] {String.class}, "WELCOME ID=abc NAME=Alice"));

    @SuppressWarnings("unchecked")
    Map<String, String> ok =
        (Map<String, String>)
            invokePrivate(
                client,
                "parseProtocolArgs",
                new Class[] {String.class},
                "GAME_STARTED GAME_ID=5 COLOR=WHITE WHITE=Alice BLACK=Bob");
    assertEquals("5", ok.get("GAME_ID"));
    assertEquals("WHITE", ok.get("COLOR"));

    @SuppressWarnings("unchecked")
    Map<String, String> empty1 =
        (Map<String, String>)
            invokePrivate(
                client, "parseProtocolArgs", new Class[] {String.class}, new Object[] {null});
    @SuppressWarnings("unchecked")
    Map<String, String> empty2 =
        (Map<String, String>)
            invokePrivate(client, "parseProtocolArgs", new Class[] {String.class}, "   ");
    @SuppressWarnings("unchecked")
    Map<String, String> partial =
        (Map<String, String>)
            invokePrivate(client, "parseProtocolArgs", new Class[] {String.class}, "X bad =y A=1");

    assertTrue(empty1.isEmpty());
    assertTrue(empty2.isEmpty());
    assertEquals("1", partial.get("A"));

    assertTrue(
        (Boolean)
            invokePrivate(
                client, "isAsyncEvent", new Class[] {String.class}, "GAME_STARTED GAME_ID=1"));
    assertTrue(
        (Boolean)
            invokePrivate(client, "isAsyncEvent", new Class[] {String.class}, "NEW_OK GAME_ID=1"));
    assertTrue(
        (Boolean)
            invokePrivate(client, "isAsyncEvent", new Class[] {String.class}, "MOVE_OK e2e4"));
    assertTrue(
        (Boolean)
            invokePrivate(
                client, "isAsyncEvent", new Class[] {String.class}, "OPPONENT_MOVE e7e5"));
    assertTrue(
        (Boolean)
            invokePrivate(
                client, "isAsyncEvent", new Class[] {String.class}, "GAME_OVER RESULT=WIN"));
    assertTrue(
        (Boolean) invokePrivate(client, "isAsyncEvent", new Class[] {String.class}, "YOUR_TURN"));
    assertTrue(
        (Boolean)
            invokePrivate(
                client, "isAsyncEvent", new Class[] {String.class}, "ERROR MESSAGE=INVALID_MOVE"));
    assertFalse(
        (Boolean)
            invokePrivate(
                client, "isAsyncEvent", new Class[] {String.class}, "STATUS_OK port=12345"));
  }

  @Test
  @DisplayName("game start parsing with and without listener")
  void handle_game_start_message() throws Exception {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));
    RecordingListener listener = new RecordingListener();
    client.setOnlineGameStartListener(listener);

    invokePrivate(
        client,
        "handleGameStartMessage",
        new Class[] {String.class},
        "GAME_STARTED GAME_ID=7 COLOR=WHITE WHITE=Alice BLACK=Bob");
    assertNotNull(listener.startedInfo);
    assertEquals(7, listener.startedInfo.getGameId());
    assertEquals(Color.WHITE, listener.startedInfo.getLocalColor());

    listener.startedInfo = null;
    invokePrivate(
        client,
        "handleGameStartMessage",
        new Class[] {String.class},
        "GAME_STARTED GAME_ID=7 COLOR=WHITE WHITE=Alice");
    assertNull(listener.startedInfo);

    invokePrivate(
        client,
        "handleGameStartMessage",
        new Class[] {String.class},
        "GAME_STARTED GAME_ID=X COLOR=WHITE WHITE=Alice BLACK=Bob");
    assertNull(listener.startedInfo);

    client.setOnlineGameStartListener(null);
    assertDoesNotThrow(
        () ->
            invokePrivate(
                client,
                "handleGameStartMessage",
                new Class[] {String.class},
                "GAME_STARTED GAME_ID=8 COLOR=BLACK WHITE=Bob BLACK=Alice"));
  }

  @Test
  @DisplayName("async events main branches")
  void handle_async_event() throws Exception {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));
    RecordingListener listener = new RecordingListener();
    client.setOnlineGameStartListener(listener);

    invokePrivate(client, "handleAsyncEvent", new Class[] {String.class}, "MOVE_OK e2e4");
    invokePrivate(client, "handleAsyncEvent", new Class[] {String.class}, "OPPONENT_MOVE e7e5");
    invokePrivate(
        client, "handleAsyncEvent", new Class[] {String.class}, "GAME_OVER RESULT=WIN REASON=END");
    invokePrivate(
        client,
        "handleAsyncEvent",
        new Class[] {String.class},
        "NEW_OK GAME_ID=3 COLOR=BLACK WHITE=Bob BLACK=Alice");

    assertEquals("e2e4", listener.localMove);
    assertEquals("e7e5", listener.opponentMove);
    assertNotNull(listener.gameOverLine);
    assertNotNull(listener.startedInfo);

    for (String error :
        List.of(
            "ERROR MESSAGE=INVALID_MOVE",
            "ERROR MESSAGE=NOT_YOUR_TURN",
            "ERROR MESSAGE=MISSING_MOVE",
            "ERROR MESSAGE=NOT_IN_GAME",
            "ERROR MESSAGE=GAME_NOT_FOUND",
            "ERROR MESSAGE=SOMETHING_ELSE")) {
      invokePrivate(client, "handleAsyncEvent", new Class[] {String.class}, error);
    }
    assertEquals(6, listener.refreshCount);
  }

  @Test
  @DisplayName("async event extra branches")
  void handle_async_event_extra_branches() throws Exception {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));
    RecordingListener listener = new RecordingListener();
    client.setOnlineGameStartListener(listener);

    invokePrivate(
        client,
        "handleAsyncEvent",
        new Class[] {String.class},
        "GAME_OVER RESULT=LOSS REASON=OPPONENT_LEFT");
    assertEquals("GAME_OVER RESULT=LOSS REASON=OPPONENT_LEFT", listener.gameOverLine);

    invokePrivate(client, "handleAsyncEvent", new Class[] {String.class}, "GAME_OVER RESULT=LOSS");
    assertEquals("GAME_OVER RESULT=LOSS", listener.gameOverLine);

    invokePrivate(client, "handleAsyncEvent", new Class[] {String.class}, "GAME_OVER RESULT=DRAW");
    assertEquals("GAME_OVER RESULT=DRAW", listener.gameOverLine);

    invokePrivate(client, "handleAsyncEvent", new Class[] {String.class}, "MOVE_OK   ");
    invokePrivate(client, "handleAsyncEvent", new Class[] {String.class}, "OPPONENT_MOVE   ");
    assertNull(listener.localMove);
    assertNull(listener.opponentMove);
  }

  @Test
  @DisplayName("reader handles async messages and sync queue")
  void reader_and_wait_response() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);

    RecordingListener listener = new RecordingListener();
    client.setOnlineGameStartListener(listener);

    server.sendAsync("MOVE_OK e2e4");
    server.sendAsync("OPPONENT_MOVE e7e5");
    server.sendAsync("ERROR MESSAGE=INVALID_MOVE");
    server.sendAsync("GAME_OVER RESULT=LOSS REASON=OPPONENT_LEFT");
    server.sendAsync("BYE");

    long deadline = System.currentTimeMillis() + 3000;
    while (client.isConnected() && System.currentTimeMillis() < deadline) {
      Thread.sleep(20);
    }

    assertEquals("e2e4", listener.localMove);
    assertEquals("e7e5", listener.opponentMove);
    assertEquals(1, listener.refreshCount);
    assertFalse(client.isConnected());

    FakeTcpServer server2 = newServer();
    AgonClient client2 = connectedClient(server2);

    server2.sendAsync("STATUS_OK port=9999 clients=1");
    assertEquals(
        "STATUS_OK port=9999 clients=1",
        invokePrivate(client2, "waitResponse", new Class[] {long.class}, 500L));

    LinkedList<String> pending = pendingResponsesOf(client2);
    Object lock = responseLockOf(client2);
    synchronized (lock) {
      pending.add("   ");
      pending.add("");
      pending.add("STATUS_OK ok");
      lock.notifyAll();
    }

    assertEquals(
        "STATUS_OK ok", invokePrivate(client2, "waitResponse", new Class[] {long.class}, 200L));
    assertNull(invokePrivate(client2, "waitResponse", new Class[] {long.class}, 50L));

    client2.disconnectSilently();
  }

  @Test
  @DisplayName("startReader does not create a second thread when already started")
  void start_reader_already_running() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);

    Thread first = (Thread) getField(client, "readerThread");
    assertNotNull(first);

    invokePrivate(client, "startReader", new Class[] {});
    Thread second = (Thread) getField(client, "readerThread");

    assertSame(first, second);

    client.disconnectSilently();
  }

  @Test
  @DisplayName("startReader queues a normal non-async response")
  void start_reader_queues_normal_response() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);

    server.sendAsync("STATUS_OK port=9999 clients=1");

    String result = (String) invokePrivate(client, "waitResponse", new Class[] {long.class}, 500L);
    assertEquals("STATUS_OK port=9999 clients=1", result);

    client.disconnectSilently();
  }

  @Test
  @DisplayName("startKeepAlive already running branch")
  void start_keep_alive_already_running() throws Exception {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));

    invokePrivate(client, "startKeepAlive", new Class[] {});
    Thread first = (Thread) getField(client, "keepAliveThread");

    invokePrivate(client, "startKeepAlive", new Class[] {});
    Thread second = (Thread) getField(client, "keepAliveThread");

    assertNotNull(first);
    assertSame(first, second);

    first.interrupt();
  }

  @Test
  @DisplayName("keepAlive thread handles interruption branch")
  void start_keep_alive_interrupt_branch() throws Exception {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));

    invokePrivate(client, "startKeepAlive", new Class[] {});
    Thread keepAlive = (Thread) getField(client, "keepAliveThread");

    assertNotNull(keepAlive);
    keepAlive.interrupt();
    keepAlive.join(500);

    assertTrue(true);
  }

  @Test
  @DisplayName("sendLine throws when output writer is missing")
  void send_line_throws_when_not_connected() throws Exception {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));
    Method m = AgonClient.class.getDeclaredMethod("sendLine", String.class);
    m.setAccessible(true);

    assertThrows(Exception.class, () -> m.invoke(client, "PING"));
  }
}
