package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseEmitterService {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterService.class);
    private static final long SSE_TIMEOUT = 0L;

    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        List<SseEmitter> userEmitters = emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
        userEmitters.add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        return emitter;
    }

    public void sendToUser(UUID userId, ChatMessageDto message) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }

        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event().data(message));
            } catch (IOException e) {
                log.debug("Failed to send SSE event to user {}", userId, e);
                deadEmitters.add(emitter);
            }
        }
        userEmitters.removeAll(deadEmitters);
    }

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(userId);
            }
        }
    }
}
