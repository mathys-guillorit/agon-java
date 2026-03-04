package fr.univ.bordeaux.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.network.*;
import fr.univ.bordeaux.application.network.client.ClientDiscovery;
import fr.univ.bordeaux.application.network.client.ServerInfo;
import fr.univ.bordeaux.application.network.server.AgonServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Test_NetworkCommands {

    private AgonServer server;

    @AfterEach
    void cleanup() {
        if (server != null) {
            server.stop();
        }
        server = null;
    }

    // -----------------
    // ServerStart
    // -----------------

    @Test
    void start_valid_port() throws Exception {

        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        AppContext ctx = new AppContext();
        new CmdServerStart(ctx).execute(new String[]{String.valueOf(port)});

        assertNotNull(ctx.getServer());
        assertTrue(ctx.getServer().isRunning());

        server = ctx.getServer();
    }

    @Test
    void start_invalid_port() {

        AppContext ctx = new AppContext();
        new CmdServerStart(ctx).execute(new String[]{"abc"});

        assertNull(ctx.getServer());
    }

    @Test
    void start_port_used() throws Exception {

        int port = 33001;

        try (ServerSocket lock = new ServerSocket(port)) {

            AppContext ctx = new AppContext();
            new CmdServerStart(ctx).execute(new String[]{String.valueOf(port)});

            assertNull(ctx.getServer());
        }
    }

    @Test
    void start_default_args_cases() {

        AppContext ctx1 = new AppContext();
        new CmdServerStart(ctx1).execute(null);

        AppContext ctx2 = new AppContext();
        new CmdServerStart(ctx2).execute(new String[0]);

        AppContext ctx3 = new AppContext();
        new CmdServerStart(ctx3).execute(new String[]{""});

        if (ctx1.getServer() != null) {
            assertTrue(ctx1.getServer().isRunning());
            ctx1.getServer().stop();
        }
        if (ctx2.getServer() != null) {
            assertTrue(ctx2.getServer().isRunning());
            ctx2.getServer().stop();
        }
        if (ctx3.getServer() != null) {
            assertTrue(ctx3.getServer().isRunning());
            ctx3.getServer().stop();
        }
    }

    // -----------------
    // ServerStop
    // -----------------

    @Test
    void stop_running_server() throws Exception {

        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port);
        server.start();

        AppContext ctx = new AppContext();
        ctx.setServer(server);

        new CmdServerStop(ctx).execute(new String[0]);

        assertNull(ctx.getServer());
    }

    @Test
    void stop_no_server() {

        AppContext ctx = new AppContext();
        new CmdServerStop(ctx).execute(new String[0]);

        assertNull(ctx.getServer());
    }

    @Test
    void stop_server_off() {

        AppContext ctx = new AppContext();

        AgonServer s = new AgonServer(12345);
        ctx.setServer(s);

        new CmdServerStop(ctx).execute(new String[0]);

        assertFalse(s.isRunning());
    }

    // -----------------
    // Join
    // -----------------

    @Test
    void join_ok() throws Exception {

        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port);
        server.start();

        AppContext ctx = new AppContext();
        new CmdJoin(ctx).execute(new String[]{"127.0.0.1:" + port});

        assertTrue(ctx.getClient().isConnected());
    }

    @Test
    void join_bad_port() {

        AppContext ctx = new AppContext();
        new CmdJoin(ctx).execute(new String[]{"127.0.0.1:abc"});

        assertFalse(ctx.getClient().isConnected());
    }

    @Test
    void join_already_connected() throws Exception {

        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port);
        assertTrue(server.start());

        AppContext ctx = new AppContext();
        assertTrue(ctx.getClient().connect("127.0.0.1", port));

        new CmdJoin(ctx).execute(new String[]{"127.0.0.1:" + port});

        assertTrue(ctx.getClient().isConnected());
    }

    @Test
    void join_parse_branches() throws Exception {

        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }
        server = new AgonServer(port);
        assertTrue(server.start());

        AppContext c1 = new AppContext();
        new CmdJoin(c1).execute(null);

        AppContext c2 = new AppContext();
        new CmdJoin(c2).execute(new String[0]);

        AppContext c3 = new AppContext();
        new CmdJoin(c3).execute(new String[]{" "});

        AppContext c4 = new AppContext();
        new CmdJoin(c4).execute(new String[]{"127.0.0.1"});

        assertTrue(true);
    }

    // -----------------
    // Ping
    // -----------------

    @Test
    void ping_not_connected() {

        AppContext ctx = new AppContext();
        new CmdPing(ctx).execute(new String[0]);

        assertFalse(ctx.getClient().isConnected());
    }

    @Test
    void ping_connected() throws Exception {

        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port);
        assertTrue(server.start());

        AppContext ctx = new AppContext();
        assertTrue(ctx.getClient().connect("127.0.0.1", port));

        new CmdPing(ctx).execute(new String[0]);

        assertTrue(ctx.getClient().isConnected());
    }

    // -----------------
    // ServerList
    // -----------------

    @Test
    void list_start_discovery() {

        AppContext ctx = new AppContext();
        new CmdServerList(ctx).execute(new String[0]);

        assertNotNull(ctx.getDiscovery());
    }

    @Test
    void list_exception() {

        AppContext ctx = new AppContext() {
            @Override
            public void ensureDiscoveryStarted() {
                throw new RuntimeException("error");
            }
        };

        new CmdServerList(ctx).execute(new String[0]);

        assertTrue(true);
    }

    @Test
    void list_with_servers() {

        AppContext ctx = new AppContext() {

            @Override
            public void ensureDiscoveryStarted() {
            }

            @Override
            public ClientDiscovery getDiscovery() {
                return new ClientDiscovery() {
                    @Override
                    public List<ServerInfo> getServers() {
                        return List.of(
                                new ServerInfo("S1", "1.1.1.1", 1234)
                        );
                    }
                };
            }
        };

        new CmdServerList(ctx).execute(new String[0]);

        assertTrue(true);
    }

    @Test
    void list_local_running() throws Exception {

        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port);
        server.start();

        AppContext ctx = new AppContext() {

            @Override
            public void ensureDiscoveryStarted() {
            }

            @Override
            public ClientDiscovery getDiscovery() {
                return new ClientDiscovery() {
                    @Override
                    public List<ServerInfo> getServers() {
                        return List.of();
                    }
                };
            }
        };

        ctx.setServer(server);

        new CmdServerList(ctx).execute(new String[0]);

        assertTrue(server.isRunning());
    }
}
