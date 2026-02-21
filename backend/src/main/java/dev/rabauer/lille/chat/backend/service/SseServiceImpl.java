package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import dev.rabauer.lille.chat.backend.entity.ConversationEntity;
import dev.rabauer.lille.chat.backend.repository.ConversationRepository;
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
class SseServiceImpl implements SseService {

    private static final Logger log = LoggerFactory.getLogger(SseServiceImpl.class);
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutes

    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final ConversationRepository conversationRepository;

    SseServiceImpl(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Override
    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        return emitter;
    }

    @Override
    public void broadcast(UUID conversationId, ChatMessageDto message) {
        ConversationEntity conversation = conversationRepository.findById(conversationId)
                .orElse(null);
        if (conversation == null) {
            log.warn("Broadcast attempted for non-existent conversation {}", conversationId);
            return;
        }

        conversation.getParticipants().forEach(participant -> {
            List<SseEmitter> userEmitters = emitters.get(participant.getId());
            if (userEmitters != null) {
                for (SseEmitter emitter : userEmitters) {
                    try {
                        emitter.send(SseEmitter.event().data(message));
                    } catch (IOException e) {
                        log.debug("Failed to send SSE event for conversation {} to user {}",
                                conversationId, participant.getId(), e);
                        removeEmitter(participant.getId(), emitter);
                    }
                }
            }
        });
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
