package fr.univ.bordeaux.application.network.server;

import java.nio.charset.StandardCharsets;

/** UDP presence message broadcast by a server on the local network. */
public class PresenceMessage {

  /** Name advertised by the server on the local network. */
  private final String serverName;

  /** TCP port exposed by the server for client connections. */
  private final int tcpPort;

  /**
   * Constructs a presence message.
   *
   * @param serverName the name of the server
   * @param tcpPort the TCP port of the server
   */
  public PresenceMessage(final String serverName, final int tcpPort) {
    this.serverName = serverName;
    this.tcpPort = tcpPort;
  }

  /**
   * Returns the server name.
   *
   * @return server name
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * Returns the TCP port.
   *
   * @return TCP port
   */
  public int getTcpPort() {
    return tcpPort;
  }

  /**
   * Encodes the presence message into ASCII bytes suitable for UDP transmission.
   *
   * @return encoded ASCII byte array
   */
  public byte[] toBytes() {
    final String message = "name=" + serverName + ";tcp=" + tcpPort;
    return message.getBytes(StandardCharsets.US_ASCII);
  }

  /**
   * Parses raw UDP data into a PresenceMessage.
   *
   * @param data raw byte buffer
   * @param length number of valid bytes
   * @return a {@link PresenceMessage} instance if parsing succeeds, otherwise null
   */
  public static PresenceMessage parse(final byte[] data, final int length) {
    PresenceMessage presenceMessage = null;
    final String message = new String(data, 0, length, StandardCharsets.US_ASCII);
    final String[] parts = message.split(";");

    String name = null;
    Integer tcp = null;

    for (final String part : parts) {
      if (part.startsWith("name=")) {
        name = part.substring("name=".length());
      } else if (part.startsWith("tcp=")) {
        tcp = Integer.parseInt(part.substring("tcp=".length()));
      }
    }

    if (name != null && tcp != null) {
      presenceMessage = new PresenceMessage(name, tcp);
    }

    return presenceMessage;
  }
}
