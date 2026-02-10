package fr.univ.bordeaux.application;

import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.client.ClientDiscovery;
import fr.univ.bordeaux.application.network.server.AgonServer;

/**
 * Shared context used by application commands.
 * This prevents commands from instantiating their own network objects.
 */
public class AppContext {

    private AgonServer server;
    private AgonClient client;
    private ClientDiscovery discoveryClient;

    public AppContext() {
    }

    // -------- Server --------
    public AgonServer getServer() {
        return server;
    }

    public void setServer(AgonServer server) {
        this.server = server;
    }

    // -------- Client --------
    public AgonClient getClient() {
        return client;
    }

    public void setClient(AgonClient client) {
        this.client = client;
    }

    // -------- Discovery (UDP) --------
    public ClientDiscovery getDiscoveryClient() {
        return discoveryClient;
    }

    public void setDiscoveryClient(ClientDiscovery discoveryClient) {
        this.discoveryClient = discoveryClient;
    }
}
