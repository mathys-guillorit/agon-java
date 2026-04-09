package fr.univ.bordeaux.application.network.client.runtime;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class SocketClientTransportTest {

  private static class FakeTcpServer implements AutoCloseable {
    private final ServerSocket serverSocket;
    private final Thread thread;
    private volatile boolean running = true;
    private volatile Socket socket;
    private volatile BufferedReader in;
    private volatile BufferedWriter out;
    private final BlockingQueue<String> receivedLines = new LinkedBlockingQueue<>();

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

                  while (running && socket != null && !socket.isClosed()) {
                    String line = in.readLine();
                    if (line == null) break;
                    receivedLines.add(line);
                  }
                } catch (IOException ignored) {
                }
              });
      thread.setDaemon(true);
      thread.start();
    }

    int getPort() {
      return serverSocket.getLocalPort();
    }

    String takeReceived(long timeoutMs) throws InterruptedException {
      return receivedLines.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }

    void sendLine(String line) throws Exception {
      long deadline = System.currentTimeMillis() + 3000;
      while (out == null && System.currentTimeMillis() < deadline) {
        Thread.sleep(10);
      }
      if (out == null) throw new IllegalStateException("not connected");
      out.write(line);
      out.write('\n');
      out.flush();
    }

    @Override
    public void close() throws Exception {
      running = false;
      try {
        if (socket != null) socket.close();
      } catch (Exception ignored) {
      }
      try {
        serverSocket.close();
      } catch (Exception ignored) {
      }
    }
  }

  @Test
  void sendLineThrowsWhenNotConnected() {
    SocketClientTransport transport = new SocketClientTransport();
    assertThrows(IOException.class, () -> transport.sendLine("PING"));
  }

  @Test
  void connectSendAndStop() throws Exception {
    try (FakeTcpServer server = new FakeTcpServer()) {
      SocketClientTransport transport = new SocketClientTransport();
      transport.connect("127.0.0.1", server.getPort());

      assertTrue(transport.isConnected());

      transport.sendLine("PING");
      assertEquals("PING", server.takeReceived(1000));

      transport.stop();
      assertFalse(transport.isConnected());
    }
  }

  @Test
  void startReaderQueuesNormalResponses() throws Exception {
    try (FakeTcpServer server = new FakeTcpServer()) {
      SocketClientTransport transport = new SocketClientTransport();
      transport.connect("127.0.0.1", server.getPort());
      transport.setAsyncEventPredicate(line -> false);
      transport.startReader();

      server.sendLine("STATUS_OK port=9999 clients=1");
      assertEquals("STATUS_OK port=9999 clients=1", transport.waitResponse(1000));

      transport.stop();
    }
  }

  @Test
  void startReaderDispatchesAsyncEvents() throws Exception {
    try (FakeTcpServer server = new FakeTcpServer()) {
      SocketClientTransport transport = new SocketClientTransport();
      AtomicReference<String> asyncLine = new AtomicReference<>();

      transport.connect("127.0.0.1", server.getPort());
      transport.setAsyncEventPredicate(line -> line.startsWith("MOVE_OK"));
      transport.setAsyncEventConsumer(asyncLine::set);
      transport.startReader();

      server.sendLine("MOVE_OK e2e4");

      long deadline = System.currentTimeMillis() + 1000;
      while (asyncLine.get() == null && System.currentTimeMillis() < deadline) {
        Thread.sleep(10);
      }

      assertEquals("MOVE_OK e2e4", asyncLine.get());
      assertNull(transport.waitResponse(100));

      transport.stop();
    }
  }

  @Test
  void waitResponseSkipsBlankLinesAndTimeouts() throws Exception {
    try (FakeTcpServer server = new FakeTcpServer()) {
      SocketClientTransport transport = new SocketClientTransport();
      transport.connect("127.0.0.1", server.getPort());
      transport.setAsyncEventPredicate(line -> false);
      transport.startReader();

      server.sendLine("   ");
      server.sendLine("");
      server.sendLine("STATUS_OK ok");

      assertEquals("STATUS_OK ok", transport.waitResponse(1000));
      assertNull(transport.waitResponse(100));

      transport.stop();
    }
  }

  @Test
  void byeStopsTransport() throws Exception {
    try (FakeTcpServer server = new FakeTcpServer()) {
      SocketClientTransport transport = new SocketClientTransport();
      transport.connect("127.0.0.1", server.getPort());
      transport.setAsyncEventPredicate(line -> false);
      transport.startReader();

      server.sendLine("BYE");

      long deadline = System.currentTimeMillis() + 1000;
      while (transport.isConnected() && System.currentTimeMillis() < deadline) {
        Thread.sleep(10);
      }

      assertFalse(transport.isConnected());
    }
  }

  @Test
  void secondStartReaderDoesNotBreak() throws Exception {
    try (FakeTcpServer server = new FakeTcpServer()) {
      SocketClientTransport transport = new SocketClientTransport();
      transport.connect("127.0.0.1", server.getPort());
      transport.setAsyncEventPredicate(line -> false);

      assertDoesNotThrow(transport::startReader);
      assertDoesNotThrow(transport::startReader);

      transport.stop();
    }
  }
}
