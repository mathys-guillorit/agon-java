package fr.univ.bordeaux.application.match.player;

import static org.junit.jupiter.api.Assertions.*;

import fr.univ.bordeaux.agoncore.agonelements.Color;
import fr.univ.bordeaux.application.commands.AgonRegister;
import fr.univ.bordeaux.application.commands.CmdAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NetworkPlayerTest {

  @Test
  @DisplayName("Constructor initializes name and color correctly")
  void constructorInitializesFields() {
    NetworkPlayer player = new NetworkPlayer("Alice", Color.WHITE);

    assertEquals("Alice", player.getName());
    assertEquals(Color.WHITE, player.getColor());
  }

  @Test
  @DisplayName("getAction returns null for network player")
  void getActionReturnsNull() {
    NetworkPlayer player = new NetworkPlayer("Bob", Color.BLACK);
    AgonRegister<CmdAction> cmds = new AgonRegister<>();

    CmdAction action = player.getAction(cmds);

    assertNull(action);
  }

  @Test
  @DisplayName("getColor returns assigned color")
  void getColorReturnsAssignedColor() {
    NetworkPlayer player = new NetworkPlayer("Charlie", Color.BLACK);

    assertEquals(Color.BLACK, player.getColor());
  }

  @Test
  @DisplayName("getName returns assigned name")
  void getNameReturnsAssignedName() {
    NetworkPlayer player = new NetworkPlayer("Dave", Color.WHITE);

    assertEquals("Dave", player.getName());
  }
}
