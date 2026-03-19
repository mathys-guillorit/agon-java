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

    private final int port;
    private final String name;
    private ServerSocket serverSocket;
    private Thread acceptClientThread;

    /* For F39 */
    private final List<ClientHandler> currentClients = Collections.synchronizedList(new ArrayList<>());;

    private ServerDiscovery discovery;
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
            System.out.println("[SERVER] is running");
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
     * This method runs in acceptClientThread and continuously waits for incoming TCP connections.
     */
    private void acceptClientLoop() {
        System.out.println("[SERVER] Accept loop started.");
        while(running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVER] Client connected: " + clientSocket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(clientSocket, this);
                currentClients.add(handler);
                new Thread(handler).start();

            } catch (IOException e) {
                break;
            }
        }
        System.out.println("[SERVER] Accept loop ended.");
    }

    /**
     * Stops the server, disconnects the current client (if any), stops UDP broadcast, and closes the server socket.
     *
     * @return true if the server was stopped (or already stopped)
     */
    public boolean stop() {
        if (!running) {
            System.out.println("[SERVER] Is off");
            return true;
        }

        running = false;
        System.out.println("[SERVER] Stopping...");

        synchronized(currentClients) {
            for (ClientHandler handler : currentClients) {
                handler.stop();
            }
            currentClients.clear();
        }

        if (discovery != null) {
            discovery.stop();
            discovery = null;
        }

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
    public int getPort() { return port; }

    /**
     * Returns the server name used in UDP presence broadcasts.
     *
     * @return server name
     */
    public String getName() { return name; }

    /**
     * Indicates if the server is currently running.
     *
     * @return true if running
     */
    public boolean isRunning() { return running; }

    /**
     * Removes a client from the list when they disconnect.
     *
     * @param handler The client handler to be removed
     */
    public void removeClient(ClientHandler handler) {
        currentClients.remove(handler);
    }

    /**
     * Returns the number of connected clients.
     */
    public int getConnectedClientsCount() {
        return currentClients.size();
    }
}

