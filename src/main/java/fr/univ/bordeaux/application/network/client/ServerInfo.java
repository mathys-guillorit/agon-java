package fr.univ.bordeaux.application.network.client;

/** Represents a server discovered via UDP broadcast. */
public class ServerInfo {

  /** Display name announced by the discovered server. */
  public final String name;

  /** IP address of the discovered server. */
  public final String serverIp;

  /** TCP port exposed by the discovered server. */
  public final int tcpPort;

  /** Timestamp of the last received discovery message for this server. */
  public long lastSeen;

  /**
   * Constructs a discovered server entry.
   *
   * @param name server name
   * @param serverIp server IP address
   * @param tcpPort server TCP port
   */
  public ServerInfo(final String name, final String serverIp, final int tcpPort) {
    this.name = name;
    this.serverIp = serverIp;
    this.tcpPort = tcpPort;
    this.lastSeen = System.currentTimeMillis();
  }

  /**
   * Return server identifier.
   *
   * @return A unique server identifier string in the format {name@ip:port}.
   */
  public String key() {
    return name + "@" + serverIp + ":" + tcpPort;
  }
}
