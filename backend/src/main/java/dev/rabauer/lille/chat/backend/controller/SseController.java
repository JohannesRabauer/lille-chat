package dev.rabauer.lille.chat.backend.controller;

import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.service.SseEmitterService;
import dev.rabauer.lille.chat.backend.service.UserService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
public class SseController {

    private final SseEmitterService sseEmitterService;
    private final UserService userService;

    public SseController(SseEmitterService sseEmitterService, UserService userService) {
        this.sseEmitterService = sseEmitterService;
        this.userService = userService;
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(JwtAuthenticationToken token) {
        UserDto currentUser = userService.getOrCreateUser(token);
        return sseEmitterService.subscribe(currentUser.id());
    }
}
