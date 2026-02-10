package fr.univ.bordeaux.application.network.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// TCP game server entry point.
public class AgonServer {

    private final int port;
    private ServerSocket serverSocket;
    private Thread acceptClientThread;
    private volatile ClientHandler currentClientHandler;
    private volatile boolean running = false;

    /**
     * Default constructor (uses TCP port 12345 as required by the specification).
     */
    public AgonServer() {
        this.port = 12345;
    }

    /**
     * Constructor with a custom TCP port.
     *
     * @param port the TCP port to listen on
     */
    public AgonServer(int port) {
        this.port = port;
    }

    /**
     * Starts the TCP server.
     */
    public boolean start() {
        if (running) {
            System.out.println("[SERVER] is running");
            return true;
        }

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("[SERVER] Failed to start on port " + port);
            System.err.println("[SERVER] " + e.getMessage());
            return false;
        }

        running = true;
        acceptClientThread = new Thread(this::acceptClientLoop, "acceptClientThread");
        acceptClientThread.start();

        System.out.println("[SERVER] Started on port " + port);
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
                if (currentClientHandler != null) {
                    clientSocket.close();
                    continue;
                }
                System.out.println("[SERVER] Client connected");

                currentClientHandler = new ClientHandler(clientSocket);
                new Thread(currentClientHandler).start();
            } catch (IOException e) {
                break;
            }
        }
        System.out.println("[SERVER] Accept loop ended.");
    }

    /**
     * Stops the TCP server.
     */
    public boolean stop() {
        if (!running) {
            System.out.println("[SERVER] Is off");
            return true;
        }

        running = false;
        System.out.println("[SERVER] Stopping...");

        if (currentClientHandler != null) {
            currentClientHandler.stop();
            currentClientHandler = null;
        }


        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("[SERVER] Error while closing server socket");
        }

        System.out.println("[SERVER] Stopped");
        return true;
    }
}

