package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import dev.rabauer.lille.chat.backend.dto.SendMessageRequest;

import java.util.List;
import java.util.UUID;

public interface ChatMessageService {

    ChatMessageDto sendMessage(UUID conversationId, UUID senderId, SendMessageRequest request);

    List<ChatMessageDto> getMessages(UUID conversationId, UUID userId, int page, int size);
}
