package dev.rabauer.lille.chat.backend.controller;

import dev.rabauer.lille.chat.backend.dto.FriendshipDto;
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
class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserService userService;

    FriendshipController(FriendshipService friendshipService, UserService userService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
    }

    @PostMapping("/request/{addresseeId}")
    FriendshipDto sendRequest(@PathVariable UUID addresseeId,
                              JwtAuthenticationToken token) {
        UUID requesterId = userService.getOrCreateUser(token).id();
        return friendshipService.sendRequest(requesterId, addresseeId);
    }

    @PutMapping("/{id}/accept")
    FriendshipDto acceptRequest(@PathVariable UUID id,
                                JwtAuthenticationToken token) {
        UUID userId = userService.getOrCreateUser(token).id();
        return friendshipService.acceptRequest(id, userId);
    }

    @PutMapping("/{id}/decline")
    FriendshipDto declineRequest(@PathVariable UUID id,
                                 JwtAuthenticationToken token) {
        UUID userId = userService.getOrCreateUser(token).id();
        return friendshipService.declineRequest(id, userId);
    }

    @GetMapping
    List<FriendshipDto> listFriends(JwtAuthenticationToken token) {
        UUID userId = userService.getOrCreateUser(token).id();
        return friendshipService.listFriends(userId);
    }

    @GetMapping("/requests")
    List<FriendshipDto> listPendingRequests(JwtAuthenticationToken token) {
        UUID userId = userService.getOrCreateUser(token).id();
        return friendshipService.listPendingRequests(userId);
    }
}
