package dev.rabauer.lille.chat.backend.controller;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import dev.rabauer.lille.chat.backend.service.SseService;
import dev.rabauer.lille.chat.backend.service.UserService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/sse")
class SseController {

    private final SseService sseService;
    private final UserService userService;

    SseController(SseService sseService, UserService userService) {
        this.sseService = sseService;
        this.userService = userService;
    }

    @GetMapping("/subscribe")
    SseEmitter subscribe(JwtAuthenticationToken token) {
        UUID userId = userService.getOrCreateUser(token).id();
        return sseService.subscribe(userId);
    }
}
