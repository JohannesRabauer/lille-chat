package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

public interface SseService {

    SseEmitter subscribe(UUID userId);

    void broadcast(UUID conversationId, ChatMessageDto message);
}
