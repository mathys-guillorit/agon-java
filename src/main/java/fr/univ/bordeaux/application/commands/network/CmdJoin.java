package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Command used to connect a client to a remote game server.
 *
 * <p>This command supports the following formats:
 *
 * <ul>
 *   <li>{@code join} → connect to default localhost:12345
 *   <li>{@code join 192.168.1.10}
 *   <li>{@code join 192.168.1.10:12345}
 * </ul>
 *
 * <p>The command ensures:
 *
 * <ul>
 *   <li>Disconnection if a dead connection exists
 *   <li>Safe parsing of host and port
 *   <li>User feedback via UI
 * </ul>
 */
public class CmdJoin extends Cmd {

  /** Default host used if none is provided. */
  private static final String DEFAULT_HOST = "localhost";

  /** Default port used if none is provided. */
  private static final int DEFAULT_PORT = 12345;

  /** Shared application context (client, server, etc.). */
  private final AppContext context;

  /** Command arguments (provided by parser). */
  private String[] args;

  /**
   * Constructor used during command registration.
   *
   * @param ui User interface context
   * @param context Application context
   */
  public CmdJoin(GameUserInterface ui, AppContext context) {
    super(ui);
    this.context = context;
    this.setName("join");
    this.setDesc(
        "Usage: join [HOST[:PORT]]\n" + "Description: connects to a remote game server.\n");
  }

  /** Internal constructor used when the command is executed with arguments. */
  private CmdJoin(GameUserInterface ui, AppContext context, String[] args) {
    this(ui, context);
    this.args = args;
  }

  /** Creates a new instance of the command with parsed arguments. */
  @Override
  public CmdAction createNew(String[] args) {
    return new CmdJoin(getCtx(), context, args);
  }

  /**
   * Executes the command.
   *
   * @param match Not used (network command independent from game state)
   */
  @Override
  public boolean execute(MatchManager match) {
    return run(args);
  }

  /**
   * Core logic of the join command.
   *
   * @param args Command arguments
   * @return true if execution completed
   */
  private boolean run(String[] args) {

    AgonClient client = context.getClient();

    // If already connected, check if connection is still valid
    if (client.isConnected()) {
      if (client.isAlive()) {
        getCtx().showWarn("[CLIENT] Already connected.\n");
        return false;
      } else {
        // Clean dead connection
        client.disconnectSilently();
      }
    }

    String host = DEFAULT_HOST;
    int port = DEFAULT_PORT;

    // Parse input arguments (host[:port])
    if (args != null && args.length > 0 && !args[0].isBlank()) {

      String raw = args[0].trim();
      int colonIndex = raw.lastIndexOf(':');

      if (colonIndex != -1) {
        // Extract host and port
        String hostPart = raw.substring(0, colonIndex).trim();
        String portPart = raw.substring(colonIndex + 1).trim();

        if (!hostPart.isEmpty()) {
          host = hostPart;
        }

        try {
          port = Integer.parseInt(portPart);
        } catch (Exception e) {
          getCtx().showWarn("[CLIENT] Invalid port, using default.");
        }

      } else {
        host = raw;
      }
    }

    // Attempt connection
    if (client.connect(host, port)) {
      getCtx().showMessage("[CLIENT] Connected to " + host + ":" + port + "\n");
    } else {
      getCtx().showError("[CLIENT] Connection failed.");
    }

    return true;
  }
}
