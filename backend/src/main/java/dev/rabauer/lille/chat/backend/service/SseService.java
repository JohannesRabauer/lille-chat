package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SseService {

  private static final Logger logger = LoggerFactory.getLogger(SseService.class);
  private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30 minutes

  private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

  public SseEmitter subscribe(UUID userId) {
    SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

    emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

    emitter.onCompletion(() -> removeEmitter(userId, emitter));
    emitter.onTimeout(() -> removeEmitter(userId, emitter));
    emitter.onError(e -> removeEmitter(userId, emitter));

    return emitter;
  }

  public void broadcastToParticipants(List<UUID> participantIds, ChatMessageDto message) {
    for (UUID participantId : participantIds) {
      List<SseEmitter> userEmitters = emitters.get(participantId);
      if (userEmitters != null) {
        for (SseEmitter emitter : userEmitters) {
          try {
            emitter.send(SseEmitter.event()
                .name("message")
                .data(message));
          } catch (IOException e) {
            logger.debug("Failed to send SSE message to user {}", participantId);
            removeEmitter(participantId, emitter);
          }
        }
      }
    }
  }

  @Scheduled(fixedRate = 30000)
  public void sendHeartbeats() {
    emitters.forEach((userId, userEmitters) -> {
      for (SseEmitter emitter : userEmitters) {
        try {
          emitter.send(SseEmitter.event()
              .name("heartbeat")
              .data("ping"));
        } catch (IOException e) {
          logger.debug("Heartbeat failed for user {}", userId);
          removeEmitter(userId, emitter);
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
