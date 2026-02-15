package dev.rabauer.lille.chat.backend.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(ConversationNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleConversationNotFound(
      ConversationNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(FriendshipNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleFriendshipNotFound(
      FriendshipNotFoundException ex) {
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(NotParticipantException.class)
  public ResponseEntity<Map<String, Object>> handleNotParticipant(NotParticipantException ex) {
    return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  @ExceptionHandler(NotFriendsException.class)
  public ResponseEntity<Map<String, Object>> handleNotFriends(NotFriendsException ex) {
    return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  @ExceptionHandler(UnauthorizedFriendshipActionException.class)
  public ResponseEntity<Map<String, Object>> handleUnauthorizedFriendshipAction(
      UnauthorizedFriendshipActionException ex) {
    return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  @ExceptionHandler(InvalidFriendRequestException.class)
  public ResponseEntity<Map<String, Object>> handleInvalidFriendRequest(
      InvalidFriendRequestException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(DuplicateFriendRequestException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateFriendRequest(
      DuplicateFriendRequestException ex) {
    return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(Map.of(
        "timestamp", Instant.now().toString(),
        "status", status.value(),
        "error", status.getReasonPhrase(),
        "message", message
    ));
  }
}
