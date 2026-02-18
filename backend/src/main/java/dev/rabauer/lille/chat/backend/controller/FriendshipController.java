package dev.rabauer.lille.chat.backend.controller;

import dev.rabauer.lille.chat.backend.dto.FriendshipDto;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.service.FriendshipService;
import dev.rabauer.lille.chat.backend.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

  private final FriendshipService friendshipService;
  private final UserService userService;

  public FriendshipController(FriendshipService friendshipService, UserService userService) {
    this.friendshipService = friendshipService;
    this.userService = userService;
  }

  @GetMapping
  public List<FriendshipDto> listFriends(JwtAuthenticationToken token) {
    UserDto user = userService.getOrCreateUser(token);
    return friendshipService.listFriends(user.id());
  }

  @GetMapping("/requests")
  public List<FriendshipDto> listPendingRequests(JwtAuthenticationToken token) {
    UserDto user = userService.getOrCreateUser(token);
    return friendshipService.listPendingRequests(user.id());
  }

  @PostMapping("/request/{addresseeId}")
  public FriendshipDto sendRequest(@PathVariable UUID addresseeId, JwtAuthenticationToken token) {
    UserDto user = userService.getOrCreateUser(token);
    return friendshipService.sendRequest(user.id(), addresseeId);
  }

  @PutMapping("/{id}/accept")
  public FriendshipDto acceptRequest(@PathVariable UUID id, JwtAuthenticationToken token) {
    UserDto user = userService.getOrCreateUser(token);
    return friendshipService.acceptRequest(id, user.id());
  }

  @PutMapping("/{id}/decline")
  public FriendshipDto declineRequest(@PathVariable UUID id, JwtAuthenticationToken token) {
    UserDto user = userService.getOrCreateUser(token);
    return friendshipService.declineRequest(id, user.id());
  }
}
