package fr.univ.bordeaux.application.network.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fr.univ.bordeaux.application.network.server.invitation.Invitation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InvitationTest {

  @Test
  @DisplayName("Invitation getters return constructor values")
  void invitationGetters() {
    Invitation invitation = new Invitation(10, 20, 123456789L);

    assertEquals(10, invitation.getInviterId());
    assertEquals(20, invitation.getInvitedId());
    assertEquals(123456789L, invitation.getExpiresAt());
  }
}
