package fr.univ.bordeaux.application.network.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
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
     * Connect to a TCP server. If already connected, returns true.
     *
     * @param host server host (e.g. "127.0.0.1")
     * @param port server port (e.g. 12345)
     * @return true if the connection succeeds (or is already established), false otherwise
     */
    public boolean connect(String host, int port) {
        if (isConnected()) {
            return true;
        }

        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(5000);

            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            out = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));

            return true;

        } catch (IOException e) {
            disconnectSilently();
            return false;
        }
    }

    /**
     * Indicates whether the client is currently connected.
     *
     * @return true if the socket is connected and not closed
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * Checks whether the connection is still alive by sending a PING.
     *
     * @return true if the server replies with PONG, false otherwise
     */
    public boolean isAlive() {
        if (!isConnected()) {
            return false;
        }

        try {
            sendLine("PING");
            String resp = readProtocolLine();

            if (resp == null || !resp.startsWith("PONG")) {
                disconnectSilently();
                return false;
            }

            return true;

        } catch (IOException e) {
            disconnectSilently();
            return false;
        }
    }

    /**
     * Sends PING to the server and waits for a PONG reply.
     *
     * @return a formatted RTT string (in milliseconds) if the server replies correctly, null otherwise
     */
    public String pingRttMs() {
        if (!isConnected()) {
            return null;
        }

        long t0 = System.currentTimeMillis();

        try {
            sendLine("PING");
            String resp = readProtocolLine();

            if (resp == null || !resp.startsWith("PONG")) {
                return null;
            }

            long rtt = System.currentTimeMillis() - t0;
            return "[SERVER] PONG TIME=" + rtt + "ms";

        } catch (IOException e) {
            disconnectSilently();
            return null;
        }
    }

    /**
     * Requests the status of the connected remote server.
     *
     * @return the raw server response if successful, null otherwise
     */
    public String requestServerStatus() {
        if (!isConnected()) {
            return null;
        }

        try {
            sendLine("STATUS");
            String resp = readProtocolLine();

            if (resp == null || !resp.startsWith("STATUS_OK")) {
                return null;
            }

            return resp;

        } catch (IOException e) {
            disconnectSilently();
            return null;
        }
    }

    /**
     * Sends QUIT to the server then closes.
     */
    public void quit() {
        if (!isConnected()) {
            disconnectSilently();
            return;
        }

        try {
            sendLine("QUIT");
            readProtocolLine(); // BYE expected
        } catch (IOException ignored) {
        } finally {
            disconnectSilently();
        }
    }

    /**
     * Close everything without throwing.
     */
    public void disconnectSilently() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }

        socket = null;
        in = null;
        out = null;
    }

    /**
     * Writes a single line to the server followed by a newline character.
     *
     * @param msg message to send
     * @throws IOException if the client is not connected or the write fails
     */
    private void sendLine(String msg) throws IOException {
        if (out == null) {
            throw new IOException("Not connected");
        }

        out.write(msg);
        out.write('\n');
        out.flush();
    }

    /**
     * Reads a single protocol line from the server.
     *
     * <p>If the server sends {@code BYE}, the client disconnects immediately.
     *
     * @return the received line, or null if end-of-stream or remote shutdown
     * @throws IOException if the client is not connected or the read fails
     */
    private String readProtocolLine() throws IOException {
        if (in == null) {
            throw new IOException("Not connected");
        }

        String line = in.readLine();

        if (line == null) {
            disconnectSilently();
            return null;
        }

        if ("BYE".equalsIgnoreCase(line.trim())) {
            disconnectSilently();
            return "BYE";
        }

        return line;
    }
}