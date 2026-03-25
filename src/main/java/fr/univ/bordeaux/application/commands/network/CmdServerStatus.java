package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Command used to display the current status of the server.
 *
 * <p>Usage:
 * <ul>
 *     <li>{@code server_status}</li>
 * </ul>
 *
 * <p>This command:
 * <ul>
 *     <li>Displays local server information if running</li>
 *     <li>Shows port and number of connected clients</li>
 * </ul>
 */
public class CmdServerStatus extends Cmd {

    private final AppContext context;

    public CmdServerStatus(GameUserInterface ui, AppContext context) {
        super(ui);
        this.context = context;
        this.setName("server_status");
        this.setDesc(
                "Usage: server status\n"
                        + "Description: displays the status of the local server.\n"
                        + "Shows port and number of connected clients.\n"
        );
    }

    @Override
    public CmdAction createNew(String[] args) {
        return new CmdServerStatus(getCtx(), context);
    }

    @Override
    public boolean execute(MatchManager match) {

        AgonServer server = context.getServer();

        // Check server state
        if (server != null && server.isRunning()) {
            getCtx().showMessage("=== SERVER STATUS ===\n");
            getCtx().showMessage("Port: " + server.getPort() + "\n");
            getCtx().showMessage("Connected Clients: " + server.getConnectedClientsCount() + "\n");
            getCtx().showMessage("=====================\n");
        } else {
            getCtx().showWarn("[SERVER] No server is currently running.");
        }

        return true;
    }
}