package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.network.client.AgonClient;

/**
 * Command to handle joining a remote server.
 * Format: join [IP[:PORT]]
 */
public class CmdJoin extends Cmd {

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 12345;
    private final AppContext context;

    /**
     * Constructs a Join command with the application context.
     * @param context The application context to retrieve the network client.
     */
    public CmdJoin(AppContext context) {
        this.context = context;
    }

    /**
     * Executes the join command. Parses the input arguments and attempts connection.
     * @param args Command arguments. args[0] can be "host" or "host:port".
     */
    @Override
    public void execute(String[] args) {
        AgonClient client = context.getClient();

        if (client.isConnected()) {
            System.out.println("[CLIENT] Already connected.");
            return;
        }

        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        // 2. Parse arguments if provided
        if (args != null && args.length > 0 && !args[0].isBlank()) {
            String raw = args[0].trim();
            int colonIndex = raw.lastIndexOf(':');

            if (colonIndex != -1) {
                String hostPart = raw.substring(0, colonIndex).trim();
                String portPart = raw.substring(colonIndex + 1).trim();

                if (!hostPart.isEmpty()) {
                    host = hostPart;
                }

                try {
                    port = Integer.parseInt(portPart);
                } catch (NumberFormatException e) {
                    System.out.println("[CLIENT] Invalid port: " + portPart + " (using default " + DEFAULT_PORT + ")");
                }
            } else {
                host = raw;
            }
        }

        if (client.connect(host, port)) {
            System.out.println("[CLIENT] Connected to " + host + ":" + port);
        } else {
            System.out.println("[CLIENT] Connection failed to " + host + ":" + port);
        }
    }
}
