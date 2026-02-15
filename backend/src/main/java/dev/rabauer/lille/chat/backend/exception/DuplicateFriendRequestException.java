package dev.rabauer.lille.chat.backend.exception;

import java.util.UUID;

public class DuplicateFriendRequestException extends RuntimeException {

  public DuplicateFriendRequestException(UUID requesterId, UUID addresseeId) {
    super("Friendship already exists between " + requesterId + " and " + addresseeId);
  }
}
