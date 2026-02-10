package fr.univ.bordeaux.application.appCommands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.appCommands.AppCommand;
import fr.univ.bordeaux.application.appCommands.AppCommandResult;
import fr.univ.bordeaux.application.network.client.AgonClient;

/**
 * Disconnects from the server.
 */
public class QuitCommand implements AppCommand {

    @Override
    public AppCommandResult execute(AppContext ctx) {
        AgonClient client = ctx.getClient();
        if (client == null) {
            return AppCommandResult.ok("Not connected.");
        }

        //client.quit();
        ctx.setClient(null);
        return AppCommandResult.ok("Disconnected (local mode).");
    }
}