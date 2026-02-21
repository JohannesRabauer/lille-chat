package dev.rabauer.lille.chat.backend.controller;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import dev.rabauer.lille.chat.backend.dto.SendMessageRequest;
import dev.rabauer.lille.chat.backend.service.ChatMessageService;
import dev.rabauer.lille.chat.backend.service.UserService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final UserService userService;

    ChatMessageController(ChatMessageService chatMessageService,
                          UserService userService) {
        this.chatMessageService = chatMessageService;
        this.userService = userService;
    }

    @PostMapping
    ChatMessageDto sendMessage(@PathVariable UUID conversationId,
                               @RequestBody SendMessageRequest request,
                               JwtAuthenticationToken token) {
        UUID senderId = userService.getOrCreateUser(token).id();
        return chatMessageService.sendMessage(conversationId, senderId, request);
    }

    @GetMapping
    List<ChatMessageDto> getMessages(@PathVariable UUID conversationId,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     JwtAuthenticationToken token) {
        UUID userId = userService.getOrCreateUser(token).id();
        return chatMessageService.getMessages(conversationId, userId, page, size);
    }
}
