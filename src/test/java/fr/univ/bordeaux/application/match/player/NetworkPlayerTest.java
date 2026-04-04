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
  void constructor_initializes_fields() {
    NetworkPlayer player = new NetworkPlayer("Alice", Color.WHITE);

    assertEquals("Alice", player.getName());
    assertEquals(Color.WHITE, player.getColor());
  }

  @Test
  @DisplayName("getAction returns null for network player")
  void get_action_returns_null() {
    NetworkPlayer player = new NetworkPlayer("Bob", Color.BLACK);
    AgonRegister<CmdAction> cmds = new AgonRegister<>();

    CmdAction action = player.getAction(cmds);

    assertNull(action);
  }

  @Test
  @DisplayName("getColor returns assigned color")
  void get_color_returns_assigned_color() {
    NetworkPlayer player = new NetworkPlayer("Charlie", Color.BLACK);

    assertEquals(Color.BLACK, player.getColor());
  }

  @Test
  @DisplayName("getName returns assigned name")
  void get_name_returns_assigned_name() {
    NetworkPlayer player = new NetworkPlayer("Dave", Color.WHITE);

    assertEquals("Dave", player.getName());
  }
}
