package fr.univ.bordeaux.application.commands.network;

import fr.univ.bordeaux.application.AppContext;
import fr.univ.bordeaux.application.match.MoveDtO;
import fr.univ.bordeaux.application.match.ReadOnlyMatch;
import fr.univ.bordeaux.application.network.client.AgonClient;
import fr.univ.bordeaux.application.network.client.LocalProfile;
import fr.univ.bordeaux.ui.GameUserInterface;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

final class NetworkCommandTestSupport {

  private NetworkCommandTestSupport() {}

  static int freePort() throws Exception {
    try (ServerSocket tmp = new ServerSocket(0)) {
      return tmp.getLocalPort();
    }
  }

  static AppContext newContext() {
    return new AppContext(new LocalProfile("TestPlayer"));
  }

  static AppContext contextWithClient(AgonClient client) {
    return new AppContext(new LocalProfile("TestPlayer")) {
      @Override
      public AgonClient getClient() {
        return client;
      }
    };
  }

  static final class TestUi implements GameUserInterface {
    final List<String> messages = new ArrayList<>();
    final List<String> warnings = new ArrayList<>();
    final List<String> errors = new ArrayList<>();

    @Override
    public boolean isRunning() {
      return true;
    }

    @Override
    public void quit() {}

    @Override
    public void onMatchUpdate(ReadOnlyMatch match) {}

    @Override
    public void showMessage(String message) {
      messages.add(message);
    }

    @Override
    public void showError(String error) {
      errors.add(error);
    }

    @Override
    public void showHelp() {}

    @Override
    public void showWarn(String msg) {
      warnings.add(msg);
    }

    @Override
    public void showInfo(String msg) {}

    @Override
    public String getUserInput() {
      return "";
    }

    @Override
    public void displayHistory(List<MoveDtO> moves) {}
  }

  static class FakeAgonClient extends AgonClient {
    boolean connected;
    boolean alive = true;
    boolean connectResult = true;
    boolean disconnectSilentlyCalled;

    String lastHost;
    int lastPort;

    String pingResponse;
    String playersResponse;
    String scoreboardResponse;
    String serverStatusResponse;
    String newGameResponse;
    Integer requestedNewGamePlayerId;
    String playerDetailsResponse;
    String awayResponse;
    String backResponse;

    boolean acceptInvitationResult;
    boolean declineInvitationResult;
    boolean cancelInvitationResult;
    boolean chooseModeResult;
    boolean acceptInvitationCalled;
    boolean declineInvitationCalled;
    boolean cancelInvitationCalled;
    String chosenMode;

    FakeAgonClient() {
      super(new LocalProfile("TestPlayer"));
    }

    @Override
    public boolean isConnected() {
      return connected;
    }

    @Override
    public boolean isAlive() {
      return alive;
    }

    @Override
    public void disconnectSilently() {
      disconnectSilentlyCalled = true;
      connected = false;
    }

    @Override
    public boolean connect(String host, int port) {
      lastHost = host;
      lastPort = port;
      connected = connectResult;
      return connectResult;
    }

    @Override
    public String pingRttMs() {
      return pingResponse;
    }

    @Override
    public String requestPlayers() {
      return playersResponse;
    }

    @Override
    public String requestScoreboard() {
      return scoreboardResponse;
    }

    @Override
    public String requestServerStatus() {
      return serverStatusResponse;
    }

    @Override
    public String requestNewGame(int targetPlayerId) {
      requestedNewGamePlayerId = targetPlayerId;
      return newGameResponse;
    }

    @Override
    public String requestPlayerDetails(int playerId) {
      return playerDetailsResponse;
    }

    @Override
    public String requestAwayStatus() {
      return awayResponse;
    }

    @Override
    public String requestBackStatus() {
      return backResponse;
    }

    @Override
    public boolean acceptInvitation() {
      acceptInvitationCalled = true;
      return acceptInvitationResult;
    }

    @Override
    public boolean declineInvitation() {
      declineInvitationCalled = true;
      return declineInvitationResult;
    }

    @Override
    public boolean cancelInvitation() {
      cancelInvitationCalled = true;
      return cancelInvitationResult;
    }

    @Override
    public boolean chooseMode(String mode) {
      chosenMode = mode;
      return chooseModeResult;
    }
  }
}
