package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.network.protocol.Command;
import fr.univ.bordeaux.application.network.protocol.CommandParser;
import fr.univ.bordeaux.application.network.protocol.CommandType;
import fr.univ.bordeaux.application.network.server.AgonServer;

/**
 * Displays the current status of the TCP server (port, clients, games).
 */
public class CmdServerStatus extends Cmd {

    private final AppContext context;

    /**
     * Constructs a Server Status command.
     *
     * @param context The application context to access the server instance.
     */
    public CmdServerStatus(AppContext context) {
        this.context = context;
    }

    @Override
    public void execute() {
    }

    /**
     * Executes the server status command.
     * Prints the port, number of connected clients, and active games.
     *
     * @param args Command arguments (unused for server status).
     */
    @Override
    public void execute(String[] args) {

        // CASE 1: Local server is running in this application
        if (context.getServer() != null && context.getServer().isRunning()) {
            AgonServer server = context.getServer();

            printStatus(server.getPort(), server.getConnectedClientsCount(), 0);
            return;
        }

        // CASE 2: No local server, but client is connected remotely
        if (context.getClient() != null && context.getClient().isConnected()) {

            String response = context.getClient().requestServerStatus();

            if (response == null) {
                System.out.println("[SERVER] Unable to retrieve remote server status.");
                return;
            }

            Command cmd = CommandParser.parse(response);

            if (cmd.getType() != CommandType.STATUS_OK) {
                System.out.println("[SERVER] Invalid status response.");
                return;
            }

            int port = -1;
            int clients = -1;
            int games = 0;

            try { port = Integer.parseInt(cmd.getArg("port")); } catch (Exception ignored) {}
            try { clients = Integer.parseInt(cmd.getArg("clients")); } catch (Exception ignored) {}

            printStatus(port, clients, games);
            return;
        }

        // CASE 3: No server and no client connection
        System.out.println("[SERVER] No local server running and no remote server connection.");
    }

    /**
     * Displays the server status in a formatted way.
     *
     * @param port    TCP port of the server
     * @param clients number of connected clients
     * @param games   number of active games
     */
    private void printStatus(int port, int clients, int games) {
        System.out.println("=== SERVER STATUS ===");
        System.out.println("  Port:              " + port);
        System.out.println("  Connected Clients: " + clients);
        System.out.println("  Active Games:      " + games);
        System.out.println("=====================");
    }
}