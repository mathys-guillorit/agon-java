package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.commands.CmdAction;
import fr.univ.bordeaux.application.match.MatchManager;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.ui.GameUserInterface;

/**
 * Command used to stop the local TCP server.
 *
 * <p>Usage:
 * <ul>
 *     <li>{@code server_stop}</li>
 * </ul>
 *
 * <p>This command:
 * <ul>
 *     <li>Checks if a server is currently running</li>
 *     <li>Stops it safely</li>
 *     <li>Removes it from the application context</li>
 * </ul>
 */
public class CmdServerStop extends Cmd {

    private final AppContext context;

    public CmdServerStop(GameUserInterface ui, AppContext context) {
        super(ui);
        this.context = context;
        this.setName("server_stop");
        this.setDesc(
                "Usage: server stop\n"
                        + "Description: stops the local TCP server if running.\n"
        );
    }

    @Override
    public CmdAction createNew(String[] args) {
        return new CmdServerStop(getCtx(), context);
    }

    @Override
    public boolean execute(MatchManager match) {

        AgonServer server = context.getServer();

        // Check if server exists
        if (server == null || !server.isRunning()) {
            getCtx().showWarn("[SERVER] No server is currently running.");
            return false;
        }

        // Stop server
        server.stop();
        context.setServer(null);

        getCtx().showMessage("[SERVER] Server stopped successfully.\n");
        return true;
    }
}