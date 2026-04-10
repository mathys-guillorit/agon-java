package fr.univ.bordeaux.application.network.server;

import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Server-side UDP broadcaster used for local network discovery. */
@SuppressWarnings("PMD.DoNotUseThreads")
public class ServerDiscovery {

  /** UDP port used for local discovery broadcasts. */
  public static final int UDP_PORT = 12_346;

  /** Broadcast IPv4 address used to advertise the server on the local network. */
  @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
  private static final String BROADCAST_ADDRESS = "255.255.255.255";

  /** Server name advertised to clients on the local network. */
  private final String name;

  /** TCP port advertised to clients for incoming connections. */
  private final int tcpPort;

  /** UDP socket used to send broadcast packets. */
  private DatagramSocket socket;

  /** Indicates whether periodic broadcasting is currently active. */
  private final AtomicBoolean running = new AtomicBoolean();

  /**
   * Constructs a server broadcaster.
   *
   * @param name server name
   * @param tcpPort TCP port used by the server
   */
  public ServerDiscovery(final String name, final int tcpPort) {
    this.name = name;
    this.tcpPort = tcpPort;
  }

  /**
   * Starts periodic UDP broadcasting.
   *
   * @throws SocketException if the UDP socket cannot be created
   */
  public void start() throws SocketException {
    if (!running.compareAndSet(false, true)) {
      return;
    }

    socket = new DatagramSocket();
    socket.setBroadcast(true);

    final PresenceMessage message = new PresenceMessage(name, tcpPort);
    final byte[] data = message.toBytes();

    final InetAddress broadcastAddress;
    try {
      broadcastAddress = InetAddress.getByName(BROADCAST_ADDRESS);
    } catch (UnknownHostException exception) {
      running.set(false);
      socket.close();
      final SocketException socketException =
          new SocketException("Invalid broadcast address: " + BROADCAST_ADDRESS);
      socketException.initCause(exception);
      throw socketException;
    }

    final DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, UDP_PORT);

    final Thread broadcasterThread =
        new Thread(
            () -> {
              while (running.get()) {
                try {
                  socket.send(packet);
                  Thread.sleep(10_000);
                } catch (InterruptedException exception) {
                  Thread.currentThread().interrupt();
                  running.set(false);
                } catch (IllegalStateException exception) {
                  running.set(false);
                } catch (RuntimeException exception) {
                  running.set(false);
                } catch (Exception exception) {
                  running.set(false);
                }
              }
            },
            "AgonServer-Discovery");

    broadcasterThread.setDaemon(true);
    broadcasterThread.start();
  }

  /** Stops UDP broadcasting and releases resources. */
  public void stop() {
    running.set(false);
    if (socket != null) {
      socket.close();
    }
  }
}
