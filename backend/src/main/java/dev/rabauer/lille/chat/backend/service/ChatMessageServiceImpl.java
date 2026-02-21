package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import dev.rabauer.lille.chat.backend.dto.SendMessageRequest;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.entity.ChatMessageEntity;
import dev.rabauer.lille.chat.backend.entity.ConversationEntity;
import dev.rabauer.lille.chat.backend.entity.UserEntity;
import dev.rabauer.lille.chat.backend.repository.ChatMessageRepository;
import dev.rabauer.lille.chat.backend.repository.ConversationRepository;
import dev.rabauer.lille.chat.backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SseEmitterService sseEmitterService;

    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository,
                                  ConversationRepository conversationRepository,
                                  UserRepository userRepository,
                                  SseEmitterService sseEmitterService) {
        this.chatMessageRepository = chatMessageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.sseEmitterService = sseEmitterService;
    }

    @Override
    public ChatMessageDto sendMessage(UUID conversationId, UUID senderId, SendMessageRequest request) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(senderId));

        if (!isParticipant) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }

        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + senderId));

        ChatMessageEntity message = new ChatMessageEntity();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.content());
        message.setSentAt(Instant.now());

        ChatMessageDto dto = toDto(chatMessageRepository.save(message));

        conversation.getParticipants().forEach(participant ->
                sseEmitterService.sendToUser(participant.getId(), dto));

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(UUID conversationId, UUID userId, int page, int size) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));

        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId));

        if (!isParticipant) {
            throw new IllegalArgumentException("User is not a participant of this conversation");
        }

        return chatMessageRepository
                .findByConversationIdOrderBySentAtAsc(conversationId, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ChatMessageDto toDto(ChatMessageEntity entity) {
        UserEntity sender = entity.getSender();
        UserDto senderDto = new UserDto(sender.getId(), sender.getUsername(),
                sender.getDisplayName(), sender.getEmail());
        return new ChatMessageDto(entity.getId(), entity.getConversation().getId(),
                senderDto, entity.getContent(), entity.getSentAt());
    }
}
