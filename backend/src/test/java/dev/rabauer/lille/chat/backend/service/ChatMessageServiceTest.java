package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.TestcontainersConfiguration;
import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import dev.rabauer.lille.chat.backend.dto.ConversationDto;
import dev.rabauer.lille.chat.backend.dto.FriendshipDto;
import dev.rabauer.lille.chat.backend.dto.SendMessageRequest;
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
class ChatMessageServiceTest {

    @Autowired
    ChatMessageService chatMessageService;

    @Autowired
    ConversationService conversationService;

    @Autowired
    UserService userService;

    @Autowired
    FriendshipService friendshipService;

    private UserDto alice;
    private UserDto bob;
    private ConversationDto conversation;

    @BeforeEach
    void setUp() {
        alice = userService.getOrCreateUser(buildToken(UUID.randomUUID(), "alice_" + UUID.randomUUID(), "alice@test.dev"));
        bob = userService.getOrCreateUser(buildToken(UUID.randomUUID(), "bob_" + UUID.randomUUID(), "bob@test.dev"));

        FriendshipDto friendship = friendshipService.sendRequest(alice.id(), bob.id());
        friendshipService.acceptRequest(friendship.id(), bob.id());

        conversation = conversationService.getOrCreateDirectConversation(alice.id(), bob.id());
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
    void sendMessage_persistsAndReturnsMessage() {
        ChatMessageDto message = chatMessageService.sendMessage(
                conversation.id(), alice.id(), new SendMessageRequest("Hello Bob!"));

        assertThat(message).isNotNull();
        assertThat(message.id()).isNotNull();
        assertThat(message.conversationId()).isEqualTo(conversation.id());
        assertThat(message.sender().id()).isEqualTo(alice.id());
        assertThat(message.content()).isEqualTo("Hello Bob!");
        assertThat(message.sentAt()).isNotNull();
    }

    @Test
    void sendMessage_byNonParticipant_throws() {
        UserDto stranger = userService.getOrCreateUser(
                buildToken(UUID.randomUUID(), "stranger_" + UUID.randomUUID(), "stranger@test.dev"));

        assertThatThrownBy(() -> chatMessageService.sendMessage(
                conversation.id(), stranger.id(), new SendMessageRequest("Intruder!")))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getMessages_returnsMessagesInOrder() {
        chatMessageService.sendMessage(conversation.id(), alice.id(), new SendMessageRequest("First"));
        chatMessageService.sendMessage(conversation.id(), bob.id(), new SendMessageRequest("Second"));
        chatMessageService.sendMessage(conversation.id(), alice.id(), new SendMessageRequest("Third"));

        List<ChatMessageDto> messages = chatMessageService.getMessages(conversation.id(), alice.id(), 0, 20);

        assertThat(messages).hasSize(3);
        assertThat(messages.get(0).content()).isEqualTo("First");
        assertThat(messages.get(1).content()).isEqualTo("Second");
        assertThat(messages.get(2).content()).isEqualTo("Third");
    }

    @Test
    void getMessages_respectsPagination() {
        for (int i = 0; i < 15; i++) {
            chatMessageService.sendMessage(conversation.id(), alice.id(), new SendMessageRequest("Message " + i));
        }

        List<ChatMessageDto> firstPage = chatMessageService.getMessages(conversation.id(), alice.id(), 0, 10);
        List<ChatMessageDto> secondPage = chatMessageService.getMessages(conversation.id(), alice.id(), 1, 10);

        assertThat(firstPage).hasSize(10);
        assertThat(secondPage).hasSize(5);
    }

    @Test
    void getMessages_byNonParticipant_throws() {
        UserDto stranger = userService.getOrCreateUser(
                buildToken(UUID.randomUUID(), "stranger_" + UUID.randomUUID(), "stranger@test.dev"));

        assertThatThrownBy(() -> chatMessageService.getMessages(conversation.id(), stranger.id(), 0, 20))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getMessages_emptyConversation_returnsEmptyList() {
        List<ChatMessageDto> messages = chatMessageService.getMessages(conversation.id(), alice.id(), 0, 20);

        assertThat(messages).isEmpty();
    }
}
