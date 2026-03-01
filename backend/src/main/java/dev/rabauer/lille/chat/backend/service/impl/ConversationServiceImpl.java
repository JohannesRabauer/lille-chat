package dev.rabauer.lille.chat.backend.service.impl;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import dev.rabauer.lille.chat.backend.dto.ConversationDto;
import dev.rabauer.lille.chat.backend.dto.ConversationType;
import dev.rabauer.lille.chat.backend.dto.CreateGroupConversationRequest;
import dev.rabauer.lille.chat.backend.dto.FriendshipStatus;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.entity.ChatMessageEntity;
import dev.rabauer.lille.chat.backend.entity.ConversationEntity;
import dev.rabauer.lille.chat.backend.entity.UserEntity;
import dev.rabauer.lille.chat.backend.repository.ConversationRepository;
import dev.rabauer.lille.chat.backend.repository.FriendshipRepository;
import dev.rabauer.lille.chat.backend.repository.UserRepository;
import dev.rabauer.lille.chat.backend.service.ConversationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public ConversationServiceImpl(ConversationRepository conversationRepository,
            UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @Override
    public ConversationDto getOrCreateDirectConversation(UUID userId1, UUID userId2) {
        return conversationRepository.findDirectConversation(userId1, userId2, ConversationType.DIRECT)
                .map(this::toDto)
                .orElseGet(() -> {
                    UserEntity user1 = userRepository.findById(userId1)
                            .orElseThrow(() -> new NoSuchElementException("User not found: " + userId1));
                    UserEntity user2 = userRepository.findById(userId2)
                            .orElseThrow(() -> new NoSuchElementException("User not found: " + userId2));

                    ConversationEntity conversation = new ConversationEntity();
                    conversation.setType(ConversationType.DIRECT);
                    conversation.getParticipants().add(user1);
                    conversation.getParticipants().add(user2);

                    return toDto(conversationRepository.save(conversation));
                });
    }

    @Override
    public ConversationDto createGroupConversation(UUID creatorId, CreateGroupConversationRequest request) {
        UserEntity creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + creatorId));

        Set<UserEntity> participants = new HashSet<>();
        participants.add(creator);

        for (UUID participantId : request.participantIds()) {
            if (!friendshipRepository.existsByUsersAndStatus(creatorId, participantId, FriendshipStatus.ACCEPTED)) {
                throw new IllegalArgumentException(
                        "User " + participantId + " is not an accepted friend of the creator");
            }
            UserEntity participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new NoSuchElementException("User not found: " + participantId));
            participants.add(participant);
        }

        ConversationEntity conversation = new ConversationEntity();
        conversation.setName(request.name());
        conversation.setType(ConversationType.GROUP);
        conversation.setParticipants(participants);

        return toDto(conversationRepository.save(conversation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDto> getConversationsForUser(UUID userId) {
        return conversationRepository.findByParticipantId(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationDto getConversation(UUID conversationId, UUID userId) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId));
        if (!isParticipant) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }

        return toDto(conversation);
    }

    private ConversationDto toDto(ConversationEntity entity) {
        List<UserDto> participants = entity.getParticipants().stream()
                .map(u -> new UserDto(u.getId(), u.getUsername(), u.getDisplayName(), u.getEmail()))
                .toList();

        ChatMessageDto lastMessage = null;
        if (entity.getLastMessage() != null) {
            ChatMessageEntity msg = entity.getLastMessage();
            UserDto sender = new UserDto(msg.getSender().getId(), msg.getSender().getUsername(),
                    msg.getSender().getDisplayName(), msg.getSender().getEmail());
            lastMessage = new ChatMessageDto(msg.getId(), entity.getId(), sender,
                    msg.getContent(), msg.getSentAt());
        }

        return new ConversationDto(entity.getId(), entity.getName(), entity.getType(), participants, lastMessage);
    }
}
