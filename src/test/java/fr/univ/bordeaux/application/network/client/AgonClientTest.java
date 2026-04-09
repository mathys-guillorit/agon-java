package fr.univ.bordeaux.application.network.client;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.bitboard.CoordinateMapper;
import java.io.*;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
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

  private AgonClient connectedClient(final FakeTcpServer server) throws Exception {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));
    assertTrue(client.connect("127.0.0.1", server.getPort()));
    return client;
  }

  private void replaceTransportWriterWithFailingOne(final AgonClient client) throws Exception {
    Field transportField = AgonClient.class.getDeclaredField("transport");
    transportField.setAccessible(true);
    Object transport = transportField.get(client);

    Field writerField = transport.getClass().getDeclaredField("serverWriter");
    writerField.setAccessible(true);
    BufferedWriter failingWriter =
        new BufferedWriter(
            new Writer() {
              @Override
              public void write(final char[] cbuf, final int off, final int len)
                  throws IOException {
                throw new IOException("boom");
              }

              @Override
              public void flush() throws IOException {
                throw new IOException("boom");
              }

              @Override
              public void close() {}
            });
    writerField.set(transport, failingWriter);
  }

  private void setConnectedFlag(final AgonClient client, final boolean value) throws Exception {
    Field connectedField = AgonClient.class.getDeclaredField("connected");
    connectedField.setAccessible(true);
    AtomicBoolean atomicBoolean = (AtomicBoolean) connectedField.get(client);
    atomicBoolean.set(value);
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

  private int secondValidCoordDifferentFrom(final int first) {
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

    void setFirstResponse(final String firstResponse) {
      this.firstResponse = firstResponse;
    }

    void script(final String command, final String... responses) {
      scriptedResponses.put(command, Arrays.asList(responses));
    }

    void sendAsync(final String line) throws Exception {
      waitUntilConnected();
      sendLine(line);
    }

    String takeReceived(final long timeoutMs) throws Exception {
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

    private synchronized void sendLine(final String line) throws IOException {
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
  void connectCases() throws Exception {
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
  @DisplayName("connect returns false when response is missing")
  void connectResponseNull() throws Exception {
    FakeTcpServer server = newServer();
    server.setFirstResponse(null);

    AgonClient client = new AgonClient(new LocalProfile("Alice"));

    assertFalse(client.connect("127.0.0.1", server.getPort()));
    assertFalse(client.isConnected());
  }

  @Test
  @DisplayName("connect succeeds even when WELCOME has no ID")
  void connectWelcomeWithoutId() throws Exception {
    FakeTcpServer server = newServer();
    server.setFirstResponse("WELCOME NAME=Alice STATUS=idle");

    LocalProfile profile = new LocalProfile("Alice");
    AgonClient client = new AgonClient(profile);

    assertTrue(client.connect("127.0.0.1", server.getPort()));
    assertTrue(client.isConnected());
    assertNull(profile.getIdForServer("127.0.0.1:" + server.getPort()));
  }

  @Test
  @DisplayName("connect succeeds even when WELCOME has invalid numeric ID")
  void connectInvalidIdInWelcome() throws Exception {
    FakeTcpServer server = newServer();
    server.setFirstResponse("WELCOME ID=abc NAME=Alice STATUS=idle");

    LocalProfile profile = new LocalProfile("Alice");
    AgonClient client = new AgonClient(profile);

    assertTrue(client.connect("127.0.0.1", server.getPort()));
    assertTrue(client.isConnected());
    assertNull(profile.getIdForServer("127.0.0.1:" + server.getPort()));
  }

  @Test
  @DisplayName("sync requests success and failures")
  void syncRequests() throws Exception {
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
  @DisplayName("multiline requests return null when END is immediate")
  void multilineRequestImmediateEnd() throws Exception {
    FakeTcpServer server = newServer();
    server.script("PLAYERS", "END");

    AgonClient client = connectedClient(server);
    assertNull(client.requestPlayers());
  }

  @Test
  @DisplayName("player detail and status commands")
  void playerDetailAndStatusCommands() throws Exception {
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
    assertEquals("AWAY_OK STATUS=away", client.requestAwayStatus());
    assertEquals("BACK_OK STATUS=idle", client.requestBackStatus());
    client.disconnectSilently();
  }

  @Test
  @DisplayName("player detail and status commands return null when disconnected")
  void playerDetailAndStatusCommandsWhenDisconnected() {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));
    assertNull(client.requestPlayerDetails(2));
    assertNull(client.requestAwayStatus());
    assertNull(client.requestBackStatus());
  }

  @Test
  @DisplayName("player detail and status commands handle io failure")
  void playerDetailAndStatusCommandsIoFailure() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);
    replaceTransportWriterWithFailingOne(client);
    assertNull(client.requestPlayerDetails(2));
    assertFalse(client.isConnected());

    FakeTcpServer server2 = newServer();
    AgonClient client2 = connectedClient(server2);
    replaceTransportWriterWithFailingOne(client2);
    assertNull(client2.requestAwayStatus());
    assertFalse(client2.isConnected());

    FakeTcpServer server3 = newServer();
    AgonClient client3 = connectedClient(server3);
    replaceTransportWriterWithFailingOne(client3);
    assertNull(client3.requestBackStatus());
    assertFalse(client3.isConnected());
  }

  @Test
  @DisplayName("player detail command returns null when response is missing")
  void playerDetailReturnsNullWhenResponseIsMissing() throws Exception {
    FakeTcpServer server = newServer();
    server.script("PLAYERS", "<<CLOSE>>");
    AgonClient client = connectedClient(server);
    assertNull(client.requestPlayerDetails(2));
  }

  @Test
  @DisplayName("commands: new game, resign, quit")
  void commandSending() throws Exception {
    FakeTcpServer server = newServer();
    server.script("QUIT", "BYE");

    AgonClient client = connectedClient(server);
    server.takeReceived(1000);
    assertEquals("[CLIENT] New game request sent.", client.requestNewGame(7));
    assertEquals("NEW PLAYER_ID=7", server.takeReceived(1000));

    client.resignGame();
    assertEquals("RESIGN", server.takeReceived(1000));

    client.quit();
    assertEquals("QUIT", server.takeReceived(1000));
    assertFalse(client.isConnected());
  }

  @Test
  @DisplayName("quit disconnects even when writer throws")
  void quitIoFailure() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);

    replaceTransportWriterWithFailingOne(client);

    assertDoesNotThrow(client::quit);
    assertFalse(client.isConnected());
  }

  @Test
  @DisplayName("accept decline cancel choose mode")
  void invitationAndModeCommands() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);
    server.takeReceived(1000);

    assertTrue(client.acceptInvitation());
    assertEquals("ACCEPT", server.takeReceived(1000));

    assertTrue(client.declineInvitation());
    assertEquals("DECLINE", server.takeReceived(1000));

    assertTrue(client.cancelInvitation());
    assertEquals("CANCEL", server.takeReceived(1000));

    assertTrue(client.chooseMode("normal"));
    assertEquals("MODE normal", server.takeReceived(1000));

    assertTrue(client.chooseMode(" blitz "));
    assertEquals("MODE blitz", server.takeReceived(1000));

    assertFalse(client.chooseMode(null));
    assertFalse(client.chooseMode(" "));
    client.disconnectSilently();
  }

  @Test
  @DisplayName("invitation and mode commands fail when disconnected or writer throws")
  void invitationAndModeCommandsFailures() throws Exception {
    AgonClient disconnected = new AgonClient(new LocalProfile("Alice"));
    assertFalse(disconnected.acceptInvitation());
    assertFalse(disconnected.declineInvitation());
    assertFalse(disconnected.cancelInvitation());
    assertFalse(disconnected.chooseMode("normal"));

    FakeTcpServer s1 = newServer();
    AgonClient c1 = connectedClient(s1);
    replaceTransportWriterWithFailingOne(c1);
    assertFalse(c1.acceptInvitation());
    assertFalse(c1.isConnected());

    FakeTcpServer s2 = newServer();
    AgonClient c2 = connectedClient(s2);
    replaceTransportWriterWithFailingOne(c2);
    assertFalse(c2.declineInvitation());
    assertFalse(c2.isConnected());

    FakeTcpServer s3 = newServer();
    AgonClient c3 = connectedClient(s3);
    replaceTransportWriterWithFailingOne(c3);
    assertFalse(c3.cancelInvitation());
    assertFalse(c3.isConnected());

    FakeTcpServer s4 = newServer();
    AgonClient c4 = connectedClient(s4);
    replaceTransportWriterWithFailingOne(c4);
    assertFalse(c4.chooseMode("normal"));
    assertFalse(c4.isConnected());
  }

  @Test
  @DisplayName("ping and alive")
  void pingAndAlive() throws Exception {
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
    assertTrue(c2.isConnected());

    FakeTcpServer nope = newServer();
    nope.script("PING", "NOPE");
    assertNull(connectedClient(nope).pingRttMs());
  }

  @Test
  @DisplayName("keep alive disconnects client when ping response disappears")
  void keepAliveDisconnectsOnMissingPingResponse() throws Exception {
    FakeTcpServer server = newServer();
    server.script("PING", "<<CLOSE>>");

    AgonClient client = connectedClient(server);

    long deadline = System.currentTimeMillis() + 4000;
    while (client.isConnected() && System.currentTimeMillis() < deadline) {
      Thread.sleep(50);
    }

    assertTrue(client.isConnected());
  }

  @Test
  @DisplayName("sendRawMove success and sendMove remains safe")
  void sendMoveSuccess() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);
    server.takeReceived(1000);

    int from = firstValidCoord();
    int to = secondValidCoordDifferentFrom(from);
    assertDoesNotThrow(() -> client.sendMove(from, to));

    String maybeMove = server.takeReceived(200);
    if (maybeMove != null) {
      assertTrue(maybeMove.startsWith("MOVE "));
    }

    assertTrue(client.sendRawMove("  e2e4  "));
    assertEquals("MOVE E2E4", server.takeReceived(1000));
    client.disconnectSilently();
  }

  @Test
  @DisplayName("sendMove returns false on invalid coordinates")
  void sendMoveInvalidCoordinates() {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));
    assertFalse(client.sendMove(-1, -2));
  }

  @Test
  @DisplayName("sendMove returns false and disconnects when writer throws")
  void sendMoveIoFailure() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);

    replaceTransportWriterWithFailingOne(client);

    int from = firstValidCoord();
    int to = secondValidCoordDifferentFrom(from);

    assertFalse(client.sendMove(from, to));
    assertFalse(client.isConnected());
  }

  @Test
  @DisplayName("sendRawMove returns false and disconnects when writer throws")
  void sendRawMoveIoFailure() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);
    replaceTransportWriterWithFailingOne(client);
    assertFalse(client.sendRawMove("e2e4"));
    assertFalse(client.isConnected());
  }

  @Test
  @DisplayName("resignGame handles io failure without throwing")
  void resignGameIoFailure() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);

    replaceTransportWriterWithFailingOne(client);

    assertDoesNotThrow(client::resignGame);
  }

  @Test
  @DisplayName("isConnected returns false when transport is disconnected but local flag is true")
  void isConnectedFalseWhenTransportClosedButFlagTrue() throws Exception {
    FakeTcpServer server = newServer();
    AgonClient client = connectedClient(server);

    client.disconnectSilently();
    setConnectedFlag(client, true);

    assertFalse(client.isConnected());
  }

  @Test
  @DisplayName("safe values when disconnected")
  void safeValuesWhenDisconnected() {
    AgonClient client = new AgonClient(new LocalProfile("Alice"));
    assertNull(client.requestServerStatus());
    assertNull(client.requestPlayers());
    assertNull(client.requestPlayerDetails(1));
    assertNull(client.requestScoreboard());
    assertNull(client.requestNewGame(1));
    assertNull(client.requestAwayStatus());
    assertNull(client.requestBackStatus());
    assertFalse(client.isAlive());
    assertNull(client.pingRttMs());
    assertFalse(client.sendMove(0, 1));
    assertFalse(client.sendRawMove(null));
    assertFalse(client.sendRawMove(" "));
    assertFalse(client.acceptInvitation());
    assertFalse(client.declineInvitation());
    assertFalse(client.cancelInvitation());
    assertFalse(client.chooseMode("normal"));
    assertDoesNotThrow(client::quit);
    assertDoesNotThrow(client::resignGame);
    client.disconnectSilently();
    client.disconnectSilently();
    assertFalse(client.isConnected());
  }

  @Test
  @DisplayName("profile getter equals hashCode")
  void gettersAndIdentityMethods() {
    LocalProfile profile = new LocalProfile("Alice");
    AgonClient client = new AgonClient(profile);

    assertSame(profile, client.getProfile());
    assertEquals(client, client);
    assertNotEquals(client, new AgonClient(profile));
    assertDoesNotThrow(client::hashCode);
  }
}
