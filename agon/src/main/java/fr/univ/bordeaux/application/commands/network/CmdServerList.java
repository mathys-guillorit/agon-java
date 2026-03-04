package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.application.network.client.ServerInfo;
import java.util.List;

/**
 * Command that displays the list of available game servers
 * discovered on the local network using UDP broadcast.
 */
public class CmdServerList extends Cmd {

    private final AppContext context;

    /**
     * Creates a new server list command.
     *
     * @param context the shared application context
     */
    public CmdServerList(AppContext context) {
        this.context = context;
    }

    /**
     * Executes the server list command.
     *
     * @param args unused command arguments
     */
    @Override
    public void execute(String[] args) {

        try {
            context.ensureDiscoveryStarted();
        } catch (Exception e) {
            System.out.println("[SERVER] Discovery error: " + e.getMessage());
            return;
        }

        List<ServerInfo> servers = context.getDiscovery().getServers();

        System.out.println("[SERVER] server list");

        if (servers.isEmpty()) {
            System.out.println("[SERVER] No server discovered on the local network.");
        } else {
            for (ServerInfo s : servers) {
                System.out.println("[SERVER] - " + s.name + " @ " + s.ip + ":" + s.tcpPort);
            }
        }

        AgonServer local = context.getServer();
        if (local != null && local.isRunning()) {
            System.out.println("[SERVER] Local server: RUNNING on port " + local.getPort());
        } else {
            System.out.println("[SERVER] Local server: OFF");
        }
    }
}
