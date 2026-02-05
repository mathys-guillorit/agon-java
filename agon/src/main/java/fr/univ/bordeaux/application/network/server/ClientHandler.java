package fr.univ.bordeaux.application.network.server;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private volatile boolean running = true;

    // Constructeur qui reçoit le socket du client
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // Stop le socket du client
    public void stop() {
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("[SERVER] Error while closing server socket");
        }
    }

    // Boucle principale qui gère la connexion client
    @Override
    public void run() {
        try {
            socket.setSoTimeout(60_000);
        } catch (IOException e) { //
        } finally {
            stop();
            System.out.println("[SERVER] Client disconnected.");
        }
    }
}
