package fr.univ.bordeaux.application.network.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TCP game server entry point.
 */
public class AgonServer {

    /** TCP port used by the server. */
    private final int port;

    /** Logical server name used by discovery. */
    private final String name;

    /** Main TCP server socket. */
    private ServerSocket serverSocket;

    /** Thread dedicated to accepting incoming client connections. */
    private Thread acceptClientThread;

    /** Current connected clients. */
    private final List<ClientHandler> currentClients =
            Collections.synchronizedList(new ArrayList<>());

    /** UDP discovery service associated with this server. */
    private ServerDiscovery discovery;

    /** Indicates whether the server is currently running. */
    private volatile boolean running = false;

    /**
     * Default constructor (uses TCP port 12345 as required by the specification).
     */
    public AgonServer() {
        this.port = 12345;
        this.name = "AgonServer" + this.port;
    }

    /**
     * Constructor with a custom TCP port.
     *
     * @param port the TCP port to listen on
     */
    public AgonServer(int port) {
        this.port = port;
        this.name = "AgonServer" + this.port;
    }

    /**
     * Starts the TCP server.
     *
     * @return true if the server is running or started successfully, false otherwise
     */
    public boolean start() {
        if (running) {
            return true;
        }

        try {
            serverSocket = new ServerSocket(port);
            discovery = new ServerDiscovery(name, port);
            discovery.start();
        } catch (IOException e) {
            System.err.println("[SERVER] Failed to start on port " + port);
            System.err.println("[SERVER] " + e.getMessage());
            return false;
        }

        running = true;
        acceptClientThread = new Thread(this::acceptClientLoop, "acceptClientThread");
        acceptClientThread.start();

        return true;
    }

    /**
     * Main accept loop.
     */
    private void acceptClientLoop() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(clientSocket, this);
                currentClients.add(handler);
                new Thread(handler).start();

            } catch (IOException e) {
                break;
            }
        }
    }

    /**
     * Stops the server and disconnects all connected clients cleanly.
     *
     * @return true if the server was stopped successfully (or already stopped)
     */
    public boolean stop() {
        if (!running) {
            return true;
        }

        running = false;

        // Stop all connected clients cleanly
        List<ClientHandler> clientsSnapshot;
        synchronized (currentClients) {
            clientsSnapshot = new ArrayList<>(currentClients);
        }

        for (ClientHandler handler : clientsSnapshot) {
            handler.stop();
        }

        synchronized (currentClients) {
            currentClients.clear();
        }

        // Stop discovery service
        if (discovery != null) {
            discovery.stop();
            discovery = null;
        }

        // Close server socket
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("[SERVER] Error while closing server socket: " + e.getMessage());
            } finally {
                serverSocket = null;
            }
        }

        return true;
    }

    /**
     * Returns the TCP port used by this server.
     *
     * @return TCP port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the server name used in UDP presence broadcasts.
     *
     * @return server name
     */
    public String getName() {
        return name;
    }

    /**
     * Indicates if the server is currently running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Removes a client from the list when they disconnect.
     *
     * @param handler the client handler to remove
     */
    public void removeClient(ClientHandler handler) {
        currentClients.remove(handler);
    }

    /**
     * Returns the number of connected clients.
     *
     * @return number of connected clients
     */
    public int getConnectedClientsCount() {
        return currentClients.size();
    }
}