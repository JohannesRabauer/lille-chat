package dev.rabauer.lille.chat.backend.service.impl;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import dev.rabauer.lille.chat.backend.dto.ConversationDto;
import dev.rabauer.lille.chat.backend.dto.ConversationType;
import dev.rabauer.lille.chat.backend.dto.CreateGroupConversationRequest;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.entity.ChatMessage;
import dev.rabauer.lille.chat.backend.entity.Conversation;
import dev.rabauer.lille.chat.backend.entity.ConversationParticipant;
import dev.rabauer.lille.chat.backend.entity.User;
import dev.rabauer.lille.chat.backend.exception.ConversationNotFoundException;
import dev.rabauer.lille.chat.backend.exception.NotFriendsException;
import dev.rabauer.lille.chat.backend.exception.NotParticipantException;
import dev.rabauer.lille.chat.backend.exception.UserNotFoundException;
import dev.rabauer.lille.chat.backend.repository.ChatMessageRepository;
import dev.rabauer.lille.chat.backend.repository.ConversationRepository;
import dev.rabauer.lille.chat.backend.repository.FriendshipRepository;
import dev.rabauer.lille.chat.backend.repository.UserRepository;
import dev.rabauer.lille.chat.backend.service.ConversationService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConversationServiceImpl implements ConversationService {

  private final ConversationRepository conversationRepository;
  private final UserRepository userRepository;
  private final FriendshipRepository friendshipRepository;
  private final ChatMessageRepository chatMessageRepository;

  public ConversationServiceImpl(ConversationRepository conversationRepository,
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
    if (!friendshipRepository.areFriends(userId1, userId2)) {
      throw new NotFriendsException(userId1, userId2);
    }

    return conversationRepository.findDirectConversation(userId1, userId2, ConversationType.DIRECT)
        .map(this::toDto)
        .orElseGet(() -> createDirectConversation(userId1, userId2));
  }

  private ConversationDto createDirectConversation(UUID userId1, UUID userId2) {
    User user1 = userRepository.findById(userId1)
        .orElseThrow(() -> new UserNotFoundException(userId1));
    User user2 = userRepository.findById(userId2)
        .orElseThrow(() -> new UserNotFoundException(userId2));

    Conversation conversation = new Conversation(null, ConversationType.DIRECT);
    conversation.addParticipant(new ConversationParticipant(conversation, user1));
    conversation.addParticipant(new ConversationParticipant(conversation, user2));

    conversation = conversationRepository.save(conversation);
    return toDto(conversation);
  }

  @Override
  public ConversationDto createGroupConversation(UUID creatorId,
                                                 CreateGroupConversationRequest request) {
    User creator = userRepository.findById(creatorId)
        .orElseThrow(() -> new UserNotFoundException(creatorId));

    for (UUID participantId : request.participantIds()) {
      if (!friendshipRepository.areFriends(creatorId, participantId)) {
        throw new NotFriendsException(creatorId, participantId);
      }
    }

    Conversation conversation = new Conversation(request.name(), ConversationType.GROUP);
    conversation.addParticipant(new ConversationParticipant(conversation, creator));

    for (UUID participantId : request.participantIds()) {
      User participant = userRepository.findById(participantId)
          .orElseThrow(() -> new UserNotFoundException(participantId));
      conversation.addParticipant(new ConversationParticipant(conversation, participant));
    }

    conversation = conversationRepository.save(conversation);
    return toDto(conversation);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ConversationDto> getConversationsForUser(UUID userId) {
    return conversationRepository.findByParticipantUserId(userId).stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ConversationDto getConversation(UUID conversationId, UUID userId) {
    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ConversationNotFoundException(conversationId));

    if (!conversationRepository.isParticipant(conversationId, userId)) {
      throw new NotParticipantException(userId, conversationId);
    }

    return toDto(conversation);
  }

  private ConversationDto toDto(Conversation conversation) {
    List<UserDto> participants = conversation.getParticipants().stream()
        .map(p -> toUserDto(p.getUser()))
        .toList();

    ChatMessageDto lastMessage = chatMessageRepository
        .findLastMessageByConversationId(conversation.getId())
        .map(this::toMessageDto)
        .orElse(null);

    return new ConversationDto(
        conversation.getId(),
        conversation.getName(),
        conversation.getType(),
        participants,
        lastMessage
    );
  }

  private UserDto toUserDto(User user) {
    return new UserDto(user.getId(), user.getUsername(), user.getDisplayName(), user.getEmail());
  }

  private ChatMessageDto toMessageDto(ChatMessage message) {
    return new ChatMessageDto(
        message.getId(),
        message.getConversation().getId(),
        toUserDto(message.getSender()),
        message.getContent(),
        message.getSentAt()
    );
  }
}
