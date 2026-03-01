package dev.rabauer.lille.chat.backend.controller;

import dev.rabauer.lille.chat.backend.dto.FriendshipDto;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.service.FriendshipService;
import dev.rabauer.lille.chat.backend.service.UserService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserService userService;

    public FriendshipController(FriendshipService friendshipService, UserService userService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
    }

    @PostMapping("/request/{addresseeId}")
    public FriendshipDto sendRequest(@PathVariable UUID addresseeId, JwtAuthenticationToken token) {
        UserDto currentUser = userService.getOrCreateUser(token);
        return friendshipService.sendRequest(currentUser.id(), addresseeId);
    }

    @PutMapping("/{id}/accept")
    public FriendshipDto acceptRequest(@PathVariable UUID id, JwtAuthenticationToken token) {
        UserDto currentUser = userService.getOrCreateUser(token);
        return friendshipService.acceptRequest(id, currentUser.id());
    }

    @PutMapping("/{id}/decline")
    public FriendshipDto declineRequest(@PathVariable UUID id, JwtAuthenticationToken token) {
        UserDto currentUser = userService.getOrCreateUser(token);
        return friendshipService.declineRequest(id, currentUser.id());
    }

    @GetMapping
    public List<FriendshipDto> listFriends(JwtAuthenticationToken token) {
        UserDto currentUser = userService.getOrCreateUser(token);
        return friendshipService.listFriends(currentUser.id());
    }

    @GetMapping("/requests")
    public List<FriendshipDto> listPendingRequests(JwtAuthenticationToken token) {
        UserDto currentUser = userService.getOrCreateUser(token);
        return friendshipService.listPendingRequests(currentUser.id());
    }
}
