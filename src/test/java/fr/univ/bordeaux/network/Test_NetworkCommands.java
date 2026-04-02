package fr.univ.bordeaux.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.commands.network.CmdJoin;
import fr.univ.bordeaux.application.commands.network.CmdPing;
import fr.univ.bordeaux.application.commands.network.CmdServerList;
import fr.univ.bordeaux.application.commands.network.CmdServerStart;
import fr.univ.bordeaux.application.commands.network.CmdServerStop;
import fr.univ.bordeaux.application.match.MoveDtO;
import fr.univ.bordeaux.application.match.ReadOnlyMatch;
import fr.univ.bordeaux.application.network.client.ClientDiscovery;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.application.network.client.ServerInfo;
import fr.univ.bordeaux.application.network.server.AgonServer;
import fr.univ.bordeaux.ui.GameUserInterface;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class Test_NetworkCommands {

    private AgonServer server;

    private final GameUserInterface ui = new GameUserInterface() {
        @Override
        public boolean isRunning() {
            return true;
        }

        @Override
        public void quit() {
        }

        @Override
        public void onMatchUpdate(ReadOnlyMatch match) {
        }

        @Override
        public void showMessage(String message) {
        }

        @Override
        public void showError(String error) {
        }

        @Override
        public void showHelp() {
        }

        @Override
        public void showWarn(String msg) {
        }

        @Override
        public void showInfo(String msg) {
        }

        @Override
        public AtomicBoolean getDebugMode() {
            return new AtomicBoolean(false);
        }

        @Override
        public void setVerbose(boolean state) {
        }

        @Override
        public String getUserInput() {
            return "";
        }

        @Override
        public void displayHistory(List<MoveDtO> moves) {
        }
    };

    @AfterEach
    void cleanup() {
        if (server != null) {
            server.stop();
        }
        server = null;
    }

    @Test
    void start_valid_port() throws Exception {
        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        AppContext ctx = new AppContext(new LocalProfile("TestPlayer"));

        new CmdServerStart(ui, ctx)
                .createNew(new String[]{String.valueOf(port)})
                .execute(null);

        assertNotNull(ctx.getServer());
        assertTrue(ctx.getServer().isRunning());

        server = ctx.getServer();
    }

    @Test
    void start_invalid_port() {
        AppContext ctx = new AppContext(new LocalProfile("TestPlayer"));

        new CmdServerStart(ui, ctx)
                .createNew(new String[]{"abc"})
                .execute(null);

        assertNull(ctx.getServer());
    }

    @Test
    void start_port_used() throws Exception {
        int port = 33001;

        try (ServerSocket lock = new ServerSocket(port)) {
            AppContext ctx = new AppContext(new LocalProfile("TestPlayer"));

            new CmdServerStart(ui, ctx)
                    .createNew(new String[]{String.valueOf(port)})
                    .execute(null);

            assertNull(ctx.getServer());
        }
    }

    @Test
    void start_default_args_cases() {
        AppContext ctx1 = new AppContext(new LocalProfile("TestPlayer"));
        new CmdServerStart(ui, ctx1).createNew(null).execute(null);

        AppContext ctx2 = new AppContext(new LocalProfile("TestPlayer"));
        new CmdServerStart(ui, ctx2).createNew(new String[0]).execute(null);

        AppContext ctx3 = new AppContext(new LocalProfile("TestPlayer"));
        new CmdServerStart(ui, ctx3).createNew(new String[]{""}).execute(null);

        if (ctx1.getServer() != null) ctx1.getServer().stop();
        if (ctx2.getServer() != null) ctx2.getServer().stop();
        if (ctx3.getServer() != null) ctx3.getServer().stop();
    }

    @Test
    void stop_running_server() throws Exception {
        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port, "TestServer");
        server.start();

        AppContext ctx = new AppContext(new LocalProfile("TestPlayer"));
        ctx.setServer(server);

        new CmdServerStop(ui, ctx)
                .createNew(new String[0])
                .execute(null);

        assertNull(ctx.getServer());
    }

    @Test
    void stop_no_server() {
        AppContext ctx = new AppContext(new LocalProfile("TestPlayer"));

        new CmdServerStop(ui, ctx)
                .createNew(new String[0])
                .execute(null);

        assertNull(ctx.getServer());
    }

    @Test
    void stop_server_off() {
        AppContext ctx = new AppContext(new LocalProfile("TestPlayer"));

        AgonServer s = new AgonServer(12345, "TestServer");
        ctx.setServer(s);

        new CmdServerStop(ui, ctx)
                .createNew(new String[0])
                .execute(null);

        assertFalse(s.isRunning());
    }

    @Test
    void join_ok() throws Exception {
        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port, "TestServer");
        server.start();

        AppContext ctx = new AppContext(new LocalProfile("TestPlayer"));

        new CmdJoin(ui, ctx)
                .createNew(new String[]{"127.0.0.1:" + port})
                .execute(null);

        assertTrue(ctx.getClient().isConnected());
    }

    @Test
    void join_bad_port() {
        AppContext ctx = new AppContext(new LocalProfile("TestPlayer"));

        new CmdJoin(ui, ctx)
                .createNew(new String[]{"127.0.0.1:abc"})
                .execute(null);

        assertFalse(ctx.getClient().isConnected());
    }

    @Test
    void join_already_connected() throws Exception {
        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port, "TestServer");
        server.start();

        AppContext ctx = new AppContext(new LocalProfile("TestPlayer"));
        ctx.getClient().connect("127.0.0.1", port);

        new CmdJoin(ui, ctx)
                .createNew(new String[]{"127.0.0.1:" + port})
                .execute(null);

        assertTrue(ctx.getClient().isConnected());
    }

    @Test
    void ping_not_connected() {
        AppContext ctx = new AppContext(new LocalProfile("TestPlayer"));

        new CmdPing(ui, ctx)
                .createNew(new String[0])
                .execute(null);

        assertFalse(ctx.getClient().isConnected());
    }

    @Test
    void ping_connected() throws Exception {
        int port;
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        server = new AgonServer(port, "TestServer");
        server.start();

        AppContext ctx = new AppContext(new LocalProfile("TestPlayer"));
        ctx.getClient().connect("127.0.0.1", port);

        new CmdPing(ui, ctx)
                .createNew(new String[0])
                .execute(null);

        assertTrue(ctx.getClient().isConnected());
    }

    @Test
    void list_start_discovery() {
        AppContext ctx = new AppContext(new LocalProfile("TestPlayer"));

        new CmdServerList(ui, ctx)
                .createNew(new String[0])
                .execute(null);

        assertNotNull(ctx.getDiscovery());
    }

    @Test
    void list_exception() {
        AppContext ctx = new AppContext(new LocalProfile("TestPlayer")) {
            @Override
            public void ensureDiscoveryStarted() {
                throw new RuntimeException("error");
            }
        };

        new CmdServerList(ui, ctx)
                .createNew(new String[0])
                .execute(null);

        assertTrue(true);
    }

    @Test
    void list_with_servers() {
        AppContext ctx = new AppContext(new LocalProfile("TestPlayer")) {

            @Override
            public void ensureDiscoveryStarted() {
            }

            @Override
            public ClientDiscovery getDiscovery() {
                return new ClientDiscovery() {
                    @Override
                    public List<ServerInfo> getServers() {
                        return List.of(new ServerInfo("S1", "1.1.1.1", 1234));
                    }
                };
            }
        };

        new CmdServerList(ui, ctx)
                .createNew(new String[0])
                .execute(null);

        assertTrue(true);
    }
}