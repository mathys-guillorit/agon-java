package fr.univ.bordeaux.application.network.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/** Server-side UDP broadcaster used for local network discovery. */
public class ServerDiscovery {

  public static final int UDP_PORT = 12346;

  // Dans tes constantes ou ta config
  @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
  private static final String BROADCAST_ADDRESS = "255.255.255.255";

  private final String name;
  private final int tcpPort;

  private DatagramSocket socket;
  private Thread thread;
  private volatile boolean running = false;

  /**
   * Constructs a server broadcaster.
   *
   * @param name server name
   * @param tcpPort TCP port used by the server
   */
  public ServerDiscovery(String name, int tcpPort) {
    this.name = name;
    this.tcpPort = tcpPort;
  }

  /**
   * Starts periodic UDP broadcasting.
   *
   * @throws SocketException if the UDP socket cannot be created
   */
  public void start() throws SocketException {
    if (running) {
      return;
    }

    running = true;

    socket = new DatagramSocket();
    socket.setBroadcast(true);

    thread =
        new Thread(
            () -> {
              while (running) {
                try {
                  // Create presence message
                  PresenceMessage msg = new PresenceMessage(name, tcpPort);
                  byte[] data = msg.toBytes();

                  // Broadcast to local network
                  InetAddress broadcast = InetAddress.getByName(BROADCAST_ADDRESS);
                  DatagramPacket packet =
                      new DatagramPacket(data, data.length, broadcast, UDP_PORT);

                  socket.send(packet);

                  // Wait 10 seconds before next broadcast
                  Thread.sleep(10_000);

                } catch (Exception ignored) {
                  // Ignored
                }
              }
            });

    thread.setDaemon(true);
    thread.start();
  }

  /** Stops UDP broadcasting and releases resources. */
  public void stop() {
    running = false;
    if (socket != null) {
      socket.close();
    }
  }
}
