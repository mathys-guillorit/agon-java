package fr.univ.bordeaux.application.commands.specialized.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.Cmd;
import fr.univ.bordeaux.application.network.client.AgonClient;

/**
 * Sends "PING" to the connected server and prints the RTT on the client side.
 */
public class CmdPing extends Cmd {

    private final AppContext context;

    public CmdPing(AppContext context) {
        this.context = context;
    }

    public void execute(String[] args) {
        AgonClient client = context.getClient();
        if (client == null || !client.isConnected()) {
            System.out.println("[CLIENT] Not connected. Use join first.");
            return;
        }

        String response = client.pingRttMs();

        if (response != null) {
            System.out.println(response);
        } else {
            System.out.println("[CLIENT] Error: Server did not respond to ping.");
        }
    }
}
