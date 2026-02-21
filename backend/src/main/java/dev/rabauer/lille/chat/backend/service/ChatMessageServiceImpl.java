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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SseService sseService;

    ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository,
                           ConversationRepository conversationRepository,
                           UserRepository userRepository,
                           SseService sseService) {
        this.chatMessageRepository = chatMessageRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.sseService = sseService;
    }

    @Override
    public ChatMessageDto sendMessage(UUID conversationId, UUID senderId,
                                      SendMessageRequest request) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Conversation not found: " + conversationId));

        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NoSuchElementException(
                        "User not found: " + senderId));

        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(senderId));

        if (!isParticipant) {
            throw new IllegalArgumentException(
                    "User is not a participant of this conversation");
        }

        ChatMessageEntity message = new ChatMessageEntity();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.content());
        message.setSentAt(Instant.now());

        message = chatMessageRepository.save(message);
        ChatMessageDto dto = toDto(message);
        sseService.broadcast(conversationId, dto);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(UUID conversationId, UUID userId,
                                            int page, int size) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Conversation not found: " + conversationId));

        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId));

        if (!isParticipant) {
            throw new IllegalArgumentException(
                    "User is not a participant of this conversation");
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("sentAt").ascending());
        return chatMessageRepository
                .findByConversationOrderBySentAtAsc(conversation, pageRequest)
                .getContent()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private ChatMessageDto toDto(ChatMessageEntity entity) {
        UserDto sender = new UserDto(entity.getSender().getId(),
                entity.getSender().getUsername(),
                entity.getSender().getDisplayName(),
                entity.getSender().getEmail());

        return new ChatMessageDto(entity.getId(),
                entity.getConversation().getId(), sender,
                entity.getContent(), entity.getSentAt());
    }
}
