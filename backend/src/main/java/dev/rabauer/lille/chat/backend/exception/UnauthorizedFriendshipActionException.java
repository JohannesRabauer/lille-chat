package dev.rabauer.lille.chat.backend.exception;

import java.util.UUID;

public class UnauthorizedFriendshipActionException extends RuntimeException {

  public UnauthorizedFriendshipActionException(UUID userId, UUID friendshipId) {
    super("User " + userId + " is not authorized to modify friendship " + friendshipId);
  }
}
