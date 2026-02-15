package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.TestcontainersConfiguration;
import dev.rabauer.lille.chat.backend.dto.FriendshipDto;
import dev.rabauer.lille.chat.backend.dto.FriendshipStatus;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class FriendshipServiceTest {

    @Autowired
    FriendshipService friendshipService;

    @Autowired
    UserService userService;

    private UserDto alice;
    private UserDto bob;

    @BeforeEach
    void setUp() {
        alice = userService.getOrCreateUser(buildToken(UUID.randomUUID(), "alice_" + UUID.randomUUID(), "alice@test.dev"));
        bob = userService.getOrCreateUser(buildToken(UUID.randomUUID(), "bob_" + UUID.randomUUID(), "bob@test.dev"));
    }

    private JwtAuthenticationToken buildToken(UUID sub, String username, String email) {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", sub.toString())
                .claim("preferred_username", username)
                .claim("email", email)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        return new JwtAuthenticationToken(jwt);
    }

    @Test
    void sendRequest_createsPendingFriendship() {
        FriendshipDto friendship = friendshipService.sendRequest(alice.id(), bob.id());

        assertThat(friendship).isNotNull();
        assertThat(friendship.id()).isNotNull();
        assertThat(friendship.status()).isEqualTo(FriendshipStatus.PENDING);
        assertThat(friendship.createdAt()).isNotNull();
    }

    @Test
    void sendRequest_toSelf_throws() {
        assertThatThrownBy(() -> friendshipService.sendRequest(alice.id(), alice.id()))
                .isInstanceOf(Exception.class);
    }

    @Test
    void sendRequest_duplicate_throws() {
        friendshipService.sendRequest(alice.id(), bob.id());

        assertThatThrownBy(() -> friendshipService.sendRequest(alice.id(), bob.id()))
                .isInstanceOf(Exception.class);
    }

    @Test
    void acceptRequest_setsFriendshipToAccepted() {
        FriendshipDto pending = friendshipService.sendRequest(alice.id(), bob.id());

        FriendshipDto accepted = friendshipService.acceptRequest(pending.id(), bob.id());

        assertThat(accepted.status()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    void acceptRequest_byWrongUser_throws() {
        FriendshipDto pending = friendshipService.sendRequest(alice.id(), bob.id());

        assertThatThrownBy(() -> friendshipService.acceptRequest(pending.id(), alice.id()))
                .isInstanceOf(Exception.class);
    }

    @Test
    void declineRequest_setsFriendshipToDeclined() {
        FriendshipDto pending = friendshipService.sendRequest(alice.id(), bob.id());

        FriendshipDto declined = friendshipService.declineRequest(pending.id(), bob.id());

        assertThat(declined.status()).isEqualTo(FriendshipStatus.DECLINED);
    }

    @Test
    void listFriends_returnsOnlyAccepted() {
        FriendshipDto pending = friendshipService.sendRequest(alice.id(), bob.id());
        friendshipService.acceptRequest(pending.id(), bob.id());

        List<FriendshipDto> aliceFriends = friendshipService.listFriends(alice.id());
        List<FriendshipDto> bobFriends = friendshipService.listFriends(bob.id());

        assertThat(aliceFriends).hasSize(1);
        assertThat(bobFriends).hasSize(1);
    }

    @Test
    void listFriends_excludesPending() {
        friendshipService.sendRequest(alice.id(), bob.id());

        List<FriendshipDto> friends = friendshipService.listFriends(alice.id());

        assertThat(friends).isEmpty();
    }

    @Test
    void listPendingRequests_returnsIncoming() {
        friendshipService.sendRequest(alice.id(), bob.id());

        List<FriendshipDto> bobPending = friendshipService.listPendingRequests(bob.id());

        assertThat(bobPending).hasSize(1);
    }

    @Test
    void listPendingRequests_excludesOutgoing() {
        friendshipService.sendRequest(alice.id(), bob.id());

        List<FriendshipDto> alicePending = friendshipService.listPendingRequests(alice.id());

        assertThat(alicePending).isEmpty();
    }
}
