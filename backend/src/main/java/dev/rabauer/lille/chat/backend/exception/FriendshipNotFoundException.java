package dev.rabauer.lille.chat.backend.exception;

import java.util.UUID;

public class FriendshipNotFoundException extends RuntimeException {

  public FriendshipNotFoundException(UUID friendshipId) {
    super("Friendship not found: " + friendshipId);
  }
}
