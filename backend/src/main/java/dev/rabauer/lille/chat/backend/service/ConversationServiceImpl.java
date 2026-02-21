package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import dev.rabauer.lille.chat.backend.dto.ConversationDto;
import dev.rabauer.lille.chat.backend.dto.ConversationType;
import dev.rabauer.lille.chat.backend.dto.CreateGroupConversationRequest;
import dev.rabauer.lille.chat.backend.dto.FriendshipStatus;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.entity.ChatMessageEntity;
import dev.rabauer.lille.chat.backend.entity.ConversationEntity;
import dev.rabauer.lille.chat.backend.entity.UserEntity;
import dev.rabauer.lille.chat.backend.repository.ChatMessageRepository;
import dev.rabauer.lille.chat.backend.repository.ConversationRepository;
import dev.rabauer.lille.chat.backend.repository.FriendshipRepository;
import dev.rabauer.lille.chat.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final ChatMessageRepository chatMessageRepository;

    ConversationServiceImpl(ConversationRepository conversationRepository,
                            UserRepository userRepository,
                            FriendshipRepository friendshipRepository,
                            ChatMessageRepository chatMessageRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public ConversationDto getOrCreateDirectConversation(UUID userId1, UUID userId2) {
        UserEntity user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId1));
        UserEntity user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId2));

        return conversationRepository.findDirectConversation(ConversationType.DIRECT, user1, user2)
                .map(this::toDto)
                .orElseGet(() -> {
                    ConversationEntity conversation = new ConversationEntity();
                    conversation.setType(ConversationType.DIRECT);
                    conversation.setParticipants(new ArrayList<>(List.of(user1, user2)));
                    conversation = conversationRepository.save(conversation);
                    return toDto(conversation);
                });
    }

    @Override
    public ConversationDto createGroupConversation(UUID creatorId,
                                                   CreateGroupConversationRequest request) {
        UserEntity creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + creatorId));

        List<UserEntity> participants = new ArrayList<>();
        participants.add(creator);

        for (UUID participantId : request.participantIds()) {
            UserEntity participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new NoSuchElementException(
                            "User not found: " + participantId));

            boolean areFriends = !friendshipRepository.findByUsers(creator, participant).stream()
                    .filter(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                    .toList().isEmpty();

            if (!areFriends) {
                throw new IllegalArgumentException(
                        "Cannot add non-friend to group conversation: " + participantId);
            }

            participants.add(participant);
        }

        ConversationEntity conversation = new ConversationEntity();
        conversation.setName(request.name());
        conversation.setType(ConversationType.GROUP);
        conversation.setParticipants(participants);
        conversation = conversationRepository.save(conversation);
        return toDto(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDto> getConversationsForUser(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        return conversationRepository.findByParticipant(user)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDto getConversation(UUID conversationId, UUID userId) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Conversation not found: " + conversationId));

        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId));

        if (!isParticipant) {
            throw new IllegalArgumentException(
                    "User is not a participant of this conversation");
        }

        return toDto(conversation);
    }

    private ConversationDto toDto(ConversationEntity entity) {
        List<UserDto> participants = entity.getParticipants().stream()
                .map(u -> new UserDto(u.getId(), u.getUsername(),
                        u.getDisplayName(), u.getEmail()))
                .toList();

        ChatMessageDto lastMessage = chatMessageRepository
                .findTopByConversationOrderBySentAtDesc(entity)
                .map(this::toMessageDto)
                .orElse(null);

        return new ConversationDto(entity.getId(), entity.getName(),
                entity.getType(), participants, lastMessage);
    }

    private ChatMessageDto toMessageDto(ChatMessageEntity entity) {
        UserDto sender = new UserDto(entity.getSender().getId(),
                entity.getSender().getUsername(),
                entity.getSender().getDisplayName(),
                entity.getSender().getEmail());
        return new ChatMessageDto(entity.getId(),
                entity.getConversation().getId(), sender,
                entity.getContent(), entity.getSentAt());
    }
}
