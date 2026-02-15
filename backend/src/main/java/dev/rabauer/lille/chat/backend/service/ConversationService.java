package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.dto.ConversationDto;
import dev.rabauer.lille.chat.backend.dto.CreateGroupConversationRequest;

import java.util.List;
import java.util.UUID;

public interface ConversationService {

    ConversationDto getOrCreateDirectConversation(UUID userId1, UUID userId2);

    ConversationDto createGroupConversation(UUID creatorId, CreateGroupConversationRequest request);

    List<ConversationDto> getConversationsForUser(UUID userId);

    ConversationDto getConversation(UUID conversationId, UUID userId);
}
