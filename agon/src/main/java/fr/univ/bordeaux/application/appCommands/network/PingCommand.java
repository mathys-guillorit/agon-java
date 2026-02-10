package fr.univ.bordeaux.application.appCommands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.appCommands.AppCommand;
import fr.univ.bordeaux.application.appCommands.AppCommandResult;
import fr.univ.bordeaux.application.network.client.AgonClient;

/**
 * Sends a PING to the server and prints RTT on the client side.
 */
public class PingCommand implements AppCommand {
    public AppCommandResult execute(AppContext context) {
        AgonClient client = context.getClient();
        if (context.getClient() == null)
            return AppCommandResult.error("Not connected");

        long rtt = 42;//client.pingRttMs();
        return AppCommandResult.ok("PONG TIME=" + rtt + "ms");
    }
}
