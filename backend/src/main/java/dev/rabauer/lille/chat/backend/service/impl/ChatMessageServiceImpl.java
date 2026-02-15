package dev.rabauer.lille.chat.backend.service.impl;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import dev.rabauer.lille.chat.backend.dto.SendMessageRequest;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.entity.ChatMessage;
import dev.rabauer.lille.chat.backend.entity.Conversation;
import dev.rabauer.lille.chat.backend.entity.User;
import dev.rabauer.lille.chat.backend.exception.ConversationNotFoundException;
import dev.rabauer.lille.chat.backend.exception.NotParticipantException;
import dev.rabauer.lille.chat.backend.exception.UserNotFoundException;
import dev.rabauer.lille.chat.backend.repository.ChatMessageRepository;
import dev.rabauer.lille.chat.backend.repository.ConversationRepository;
import dev.rabauer.lille.chat.backend.repository.UserRepository;
import dev.rabauer.lille.chat.backend.service.ChatMessageService;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {

  private final ChatMessageRepository chatMessageRepository;
  private final ConversationRepository conversationRepository;
  private final UserRepository userRepository;

  public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository,
                                ConversationRepository conversationRepository,
                                UserRepository userRepository) {
    this.chatMessageRepository = chatMessageRepository;
    this.conversationRepository = conversationRepository;
    this.userRepository = userRepository;
  }

  @Override
  public ChatMessageDto sendMessage(UUID conversationId, UUID senderId,
                                    SendMessageRequest request) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ConversationNotFoundException(conversationId));

    if (!conversationRepository.isParticipant(conversationId, senderId)) {
      throw new NotParticipantException(senderId, conversationId);
    }

    User sender = userRepository.findById(senderId)
        .orElseThrow(() -> new UserNotFoundException(senderId));

    ChatMessage message = new ChatMessage(conversation, sender, request.content());
    message = chatMessageRepository.save(message);

    return toDto(message);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChatMessageDto> getMessages(UUID conversationId, UUID userId, int page, int size) {
    if (!conversationRepository.existsById(conversationId)) {
      throw new ConversationNotFoundException(conversationId);
    }

    if (!conversationRepository.isParticipant(conversationId, userId)) {
      throw new NotParticipantException(userId, conversationId);
    }

    Pageable pageable = PageRequest.of(page, size);
    return chatMessageRepository.findByConversationIdOrderBySentAtAsc(conversationId, pageable)
        .getContent()
        .stream()
        .map(this::toDto)
        .toList();
  }

  private ChatMessageDto toDto(ChatMessage message) {
    return new ChatMessageDto(
        message.getId(),
        message.getConversation().getId(),
        toUserDto(message.getSender()),
        message.getContent(),
        message.getSentAt()
    );
  }

  private UserDto toUserDto(User user) {
    return new UserDto(user.getId(), user.getUsername(), user.getDisplayName(), user.getEmail());
  }
}
