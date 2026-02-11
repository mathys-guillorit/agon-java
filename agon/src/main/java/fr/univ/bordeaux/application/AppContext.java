package fr.univ.bordeaux.application;

import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.server.AgonServer;

public class AppContext {

    private final AgonClient client = new AgonClient();
    private AgonServer server;

    public AgonClient getClient() { return client; }

    public AgonServer getServer() { return server; }
    public void setServer(AgonServer server) { this.server = server; }

    public boolean isConnected() {
        return client.isConnected();
    }
}
