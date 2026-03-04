package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.network.server.AgonServer;

/**
 * server stop
 * Stops the TCP server.
 * If a client is connected, it must be notified and disconnected (BYE) -> handled in ClientHandler.stop().
 */
public class CmdServerStop extends Cmd {

    private final AppContext context;

    /**
     * Creates a new server stop command.
     *
     * @param context the shared application context
     */
    public CmdServerStop(AppContext context) {
        this.context = context;
    }

    /**
     * Executes the server stop command.
     *
     * @param args unused command arguments
     */
    @Override
    public void execute(String[] args) {
        AgonServer server = context.getServer();
        if (server == null || !server.isRunning()) {
            System.out.println("[SERVER] Server is not running.");
            return;
        }

        server.stop();
        context.setServer(null);

        System.out.println("[SERVER] Stopped.");
    }


}
