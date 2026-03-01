package dev.rabauer.lille.chat.backend.controller;

import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.service.SseService;
import dev.rabauer.lille.chat.backend.service.UserService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/sse")
public class SseController {

  private final SseService sseService;
  private final UserService userService;

  public SseController(SseService sseService, UserService userService) {
    this.sseService = sseService;
    this.userService = userService;
  }

  @GetMapping("/subscribe")
  public SseEmitter subscribe(JwtAuthenticationToken token) {
    UserDto user = userService.getOrCreateUser(token);
    return sseService.subscribe(user.id());
  }
}
