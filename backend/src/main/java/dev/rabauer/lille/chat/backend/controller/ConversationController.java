package dev.rabauer.lille.chat.backend.controller;

import dev.rabauer.lille.chat.backend.dto.ConversationDto;
import dev.rabauer.lille.chat.backend.dto.CreateGroupConversationRequest;
import dev.rabauer.lille.chat.backend.service.ConversationService;
import dev.rabauer.lille.chat.backend.service.UserService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
class ConversationController {

    private final ConversationService conversationService;
    private final UserService userService;

    ConversationController(ConversationService conversationService,
                           UserService userService) {
        this.conversationService = conversationService;
        this.userService = userService;
    }

    @PostMapping("/direct/{userId}")
    ConversationDto getOrCreateDirect(@PathVariable UUID userId,
                                      JwtAuthenticationToken token) {
        UUID currentUserId = userService.getOrCreateUser(token).id();
        return conversationService.getOrCreateDirectConversation(currentUserId, userId);
    }

    @PostMapping("/group")
    ConversationDto createGroup(@RequestBody CreateGroupConversationRequest request,
                                JwtAuthenticationToken token) {
        UUID creatorId = userService.getOrCreateUser(token).id();
        return conversationService.createGroupConversation(creatorId, request);
    }

    @GetMapping
    List<ConversationDto> listConversations(JwtAuthenticationToken token) {
        UUID userId = userService.getOrCreateUser(token).id();
        return conversationService.getConversationsForUser(userId);
    }

    @GetMapping("/{id}")
    ConversationDto getConversation(@PathVariable UUID id,
                                    JwtAuthenticationToken token) {
        UUID userId = userService.getOrCreateUser(token).id();
        return conversationService.getConversation(id, userId);
    }
}
