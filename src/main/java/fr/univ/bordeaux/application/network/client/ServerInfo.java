package fr.univ.bordeaux.application.network.client;

/** Represents a server discovered via UDP broadcast. */
public class ServerInfo {

  public final String name;
  public final String ip;
  public final int tcpPort;
  public long lastSeen;

  /**
   * Constructs a discovered server entry.
   *
   * @param name server name
   * @param ip server IP address
   * @param tcpPort server TCP port
   */
  public ServerInfo(String name, String ip, int tcpPort) {
    this.name = name;
    this.ip = ip;
    this.tcpPort = tcpPort;
    this.lastSeen = System.currentTimeMillis();
  }

  /**
   * Return server identifier.
   *
   * @return A unique server identifier string in the format {name@ip:port}.
   */
  public String key() {
    return name + "@" + ip + ":" + tcpPort;
  }
}
