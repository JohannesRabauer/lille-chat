package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.TestcontainersConfiguration;
import dev.rabauer.lille.chat.backend.TestSecurityConfig;
import dev.rabauer.lille.chat.backend.dto.ConversationDto;
import dev.rabauer.lille.chat.backend.dto.ConversationType;
import dev.rabauer.lille.chat.backend.dto.CreateGroupConversationRequest;
import dev.rabauer.lille.chat.backend.dto.FriendshipDto;
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

@Import({TestcontainersConfiguration.class, TestSecurityConfig.class})
@SpringBootTest
class ConversationServiceTest {

    @Autowired
    ConversationService conversationService;

    @Autowired
    UserService userService;

    @Autowired
    FriendshipService friendshipService;

    private UserDto alice;
    private UserDto bob;
    private UserDto charlie;

    @BeforeEach
    void setUp() {
        alice = userService.getOrCreateUser(buildToken(UUID.randomUUID(), "alice_" + UUID.randomUUID(), "alice@test.dev"));
        bob = userService.getOrCreateUser(buildToken(UUID.randomUUID(), "bob_" + UUID.randomUUID(), "bob@test.dev"));
        charlie = userService.getOrCreateUser(buildToken(UUID.randomUUID(), "charlie_" + UUID.randomUUID(), "charlie@test.dev"));

        FriendshipDto ab = friendshipService.sendRequest(alice.id(), bob.id());
        friendshipService.acceptRequest(ab.id(), bob.id());
        FriendshipDto ac = friendshipService.sendRequest(alice.id(), charlie.id());
        friendshipService.acceptRequest(ac.id(), charlie.id());
        FriendshipDto bc = friendshipService.sendRequest(bob.id(), charlie.id());
        friendshipService.acceptRequest(bc.id(), charlie.id());
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
    void getOrCreateDirectConversation_createsConversation() {
        ConversationDto conversation = conversationService.getOrCreateDirectConversation(alice.id(), bob.id());

        assertThat(conversation).isNotNull();
        assertThat(conversation.id()).isNotNull();
        assertThat(conversation.type()).isEqualTo(ConversationType.DIRECT);
        assertThat(conversation.participants()).hasSize(2);
    }

    @Test
    void getOrCreateDirectConversation_isIdempotent() {
        ConversationDto first = conversationService.getOrCreateDirectConversation(alice.id(), bob.id());
        ConversationDto second = conversationService.getOrCreateDirectConversation(alice.id(), bob.id());

        assertThat(first.id()).isEqualTo(second.id());
    }

    @Test
    void getOrCreateDirectConversation_symmetricLookup() {
        ConversationDto fromAlice = conversationService.getOrCreateDirectConversation(alice.id(), bob.id());
        ConversationDto fromBob = conversationService.getOrCreateDirectConversation(bob.id(), alice.id());

        assertThat(fromAlice.id()).isEqualTo(fromBob.id());
    }

    @Test
    void createGroupConversation_createsWithParticipants() {
        CreateGroupConversationRequest request = new CreateGroupConversationRequest(
                "Test Group",
                List.of(bob.id(), charlie.id())
        );

        ConversationDto group = conversationService.createGroupConversation(alice.id(), request);

        assertThat(group).isNotNull();
        assertThat(group.type()).isEqualTo(ConversationType.GROUP);
        assertThat(group.name()).isEqualTo("Test Group");
        assertThat(group.participants()).hasSize(3);
    }

    @Test
    void createGroupConversation_withNonFriend_throws() {
        UserDto stranger = userService.getOrCreateUser(
                buildToken(UUID.randomUUID(), "stranger_" + UUID.randomUUID(), "stranger@test.dev"));

        CreateGroupConversationRequest request = new CreateGroupConversationRequest(
                "Bad Group",
                List.of(stranger.id())
        );

        assertThatThrownBy(() -> conversationService.createGroupConversation(alice.id(), request))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getConversationsForUser_returnsAllConversations() {
        conversationService.getOrCreateDirectConversation(alice.id(), bob.id());
        conversationService.createGroupConversation(alice.id(),
                new CreateGroupConversationRequest("Group", List.of(bob.id(), charlie.id())));

        List<ConversationDto> conversations = conversationService.getConversationsForUser(alice.id());

        assertThat(conversations).hasSize(2);
    }

    @Test
    void getConversation_returnsConversationForParticipant() {
        ConversationDto created = conversationService.getOrCreateDirectConversation(alice.id(), bob.id());

        ConversationDto fetched = conversationService.getConversation(created.id(), alice.id());

        assertThat(fetched.id()).isEqualTo(created.id());
    }

    @Test
    void getConversation_throwsForNonParticipant() {
        ConversationDto conversation = conversationService.getOrCreateDirectConversation(alice.id(), bob.id());

        assertThatThrownBy(() -> conversationService.getConversation(conversation.id(), charlie.id()))
                .isInstanceOf(Exception.class);
    }
}
