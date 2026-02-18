package dev.rabauer.lille.chat.backend.exception;

import java.util.UUID;

public class NotParticipantException extends RuntimeException {

  public NotParticipantException(UUID userId, UUID conversationId) {
    super("User " + userId + " is not a participant in conversation " + conversationId);
  }
}
