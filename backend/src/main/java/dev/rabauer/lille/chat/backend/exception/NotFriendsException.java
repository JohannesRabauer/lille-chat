package dev.rabauer.lille.chat.backend.exception;

import java.util.UUID;

public class NotFriendsException extends RuntimeException {

  public NotFriendsException(UUID userId1, UUID userId2) {
    super("Users " + userId1 + " and " + userId2 + " are not friends");
  }
}
