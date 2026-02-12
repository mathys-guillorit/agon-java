package fr.univ.bordeaux.application.network.client;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * The AgonClient class manages the TCP connection to the game server.
 */
public class AgonClient {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    /**
     * Default constructor.
     */
    public AgonClient() {
    }

    /**
     * Connect to a server. If already connected, returns true.
     *
     * @param host server host (e.g. "127.0.0.1")
     * @param port server port (e.g. 12345)
     */
    public boolean connect(String host, int port) {
        if (isConnected()) return true;

        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(5000);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));
            return true;

        } catch (IOException e) {
            disconnectSilently();
            return false;
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Sends PING and waits for PONG, returns RTT in ms.
     */
    public String pingRttMs() {
        if (!isConnected()) return null;

        long t0 = System.currentTimeMillis();

        try {
            System.out.println("[CLIENT] PING");
            sendLine("PING");
            String resp = readLine();

            if (resp == null || !resp.startsWith("PONG")) return null;

            long rtt = System.currentTimeMillis() - t0;

            String response = "[SERVER] PONG TIME=" + rtt + "ms";

            return response;

        } catch (IOException e) {
            disconnectSilently();
            return null;
        }
    }

    /**
     * Sends QUIT then closes.
     */
    public void quit() {
        if (!isConnected()) {
            disconnectSilently();
            return;
        }

        try {
            System.out.println("[CLIENT] QUIT");
            sendLine("QUIT");
            String resp = readLine();
            if (resp != null) {
                System.out.println("[SERVER] " +resp);
            }
        } catch (IOException ignored) {
        } finally {
            disconnectSilently();
        }
    }

    /**
     * Close everything without throwing.
     */
    public void disconnectSilently() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        socket = null;
        in = null;
        out = null;
    }

    /**
     * Internal helper to send a line of text followed by a newline character.
     * * @param msg The message to send.
     */
    private void sendLine(String msg) throws IOException {
        if (out == null) throw new IOException("Not connected");
        out.write(msg);
        out.write('\n');
        out.flush();
    }

    /**
     * Internal helper to read a single line of text from the server.
     * * @return The line received from the server.
     * */
    private String readLine() throws IOException {
        if (in == null) throw new IOException("Not connected");
        return in.readLine();
    }

}
