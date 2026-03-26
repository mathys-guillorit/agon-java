package fr.univ.bordeaux.application;

import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.client.ClientDiscovery;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.application.network.server.AgonServer;

/**
 * Application shared context.
 */
public class AppContext {

    /** Local profile of the current user. */
    private final LocalProfile profile;

    /** Shared TCP client instance. */
    private final AgonClient client;

    /** Current local server instance. */
    private AgonServer server;

    /** UDP discovery instance. */
    private ClientDiscovery discovery;

    /**
     * Creates an application context from an existing local profile.
     *
     * @param profile the local profile of the user
     */
    public AppContext(LocalProfile profile) {
        this.profile = profile;
        this.client = new AgonClient(profile);
    }

    /**
     * Returns the shared TCP client instance.
     *
     * @return the {@link AgonClient} instance
     */
    public AgonClient getClient() {
        return client;
    }

    /**
     * Returns the local profile.
     *
     * @return the {@link LocalProfile} instance
     */
    public LocalProfile getProfile() {
        return profile;
    }

    /**
     * Returns the current local server instance.
     *
     * @return the {@link AgonServer} instance, or {@code null} if none
     */
    public AgonServer getServer() {
        return server;
    }

    /**
     * Sets the current local server instance.
     *
     * @param server the {@link AgonServer} instance to store
     */
    public void setServer(AgonServer server) {
        this.server = server;
    }

    /**
     * Convenience method to check whether the client is connected.
     *
     * @return true if the client is connected
     */
    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * Returns the UDP discovery instance, if already started.
     *
     * @return the {@link ClientDiscovery} instance, or {@code null}
     */
    public ClientDiscovery getDiscovery() {
        return discovery;
    }

    /**
     * Starts UDP discovery if not already started.
     *
     * @throws Exception if discovery cannot be started
     */
    public void ensureDiscoveryStarted() throws Exception {
        if (discovery == null) {
            discovery = new ClientDiscovery();
            discovery.start();
        }
    }
}