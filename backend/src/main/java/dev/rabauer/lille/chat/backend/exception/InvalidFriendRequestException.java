package dev.rabauer.lille.chat.backend.exception;

public class InvalidFriendRequestException extends RuntimeException {

  public InvalidFriendRequestException(String message) {
    super(message);
  }
}
