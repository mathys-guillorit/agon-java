package fr.univ.bordeaux.application;

import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.application.network.client.ClientDiscovery;

/**
 * Application shared context.
 */
public class AppContext {

    private final AgonClient client = new AgonClient();
    private AgonServer server;
    private ClientDiscovery discovery;

    /**
     * Returns the shared TCP client instance.
     *
     * @return the {@link AgonClient} instance
     */
    public AgonClient getClient() { return client; }

    /**
     * Returns the current local server instance.
     *
     * @return the {@link AgonServer} instance, or {@code null} if none
     */
    public AgonServer getServer() { return server; }

    /**
     * Sets the current local server instance.
     *
     * @param server the {@link AgonServer} instance to store (may be {@code null})
     */
    public void setServer(AgonServer server) { this.server = server; }

    /**
     * Convenience method to check whether the client is connected.
     *
     * @return true if the {@link AgonClient} is connected, false otherwise
     */
    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * Returns the UDP discovery instance, if it has been created.
     *
     * @return the {@link ClientDiscovery} instance, or null if not started yet
     */
    public ClientDiscovery getDiscovery() {
        return discovery;
    }

    /**
     * Ensures that UDP discovery is started.
     *
     * @throws Exception if the UDP socket cannot be opened
     */
    public void ensureDiscoveryStarted() throws Exception {
        if (discovery == null) {
            discovery = new ClientDiscovery();
            discovery.start();
        }
    }
}
