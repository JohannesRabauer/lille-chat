package dev.rabauer.lille.chat.backend.controller;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import dev.rabauer.lille.chat.backend.dto.ConversationDto;
import dev.rabauer.lille.chat.backend.dto.CreateGroupConversationRequest;
import dev.rabauer.lille.chat.backend.dto.SendMessageRequest;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.service.ChatMessageService;
import dev.rabauer.lille.chat.backend.service.ConversationService;
import dev.rabauer.lille.chat.backend.service.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

  private final ConversationService conversationService;
  private final ChatMessageService chatMessageService;
  private final UserService userService;

  public ConversationController(ConversationService conversationService,
                                ChatMessageService chatMessageService,
                                UserService userService) {
    this.conversationService = conversationService;
    this.chatMessageService = chatMessageService;
    this.userService = userService;
  }

  @GetMapping
  public List<ConversationDto> getConversations(JwtAuthenticationToken token) {
    UserDto user = userService.getOrCreateUser(token);
    return conversationService.getConversationsForUser(user.id());
  }

  @GetMapping("/{id}")
  public ConversationDto getConversation(@PathVariable UUID id, JwtAuthenticationToken token) {
    UserDto user = userService.getOrCreateUser(token);
    return conversationService.getConversation(id, user.id());
  }

  @PostMapping("/direct/{userId}")
  public ConversationDto createDirectConversation(@PathVariable UUID userId,
                                                  JwtAuthenticationToken token) {
    UserDto user = userService.getOrCreateUser(token);
    return conversationService.getOrCreateDirectConversation(user.id(), userId);
  }

  @PostMapping("/group")
  public ConversationDto createGroupConversation(@RequestBody CreateGroupConversationRequest req,
                                                 JwtAuthenticationToken token) {
    UserDto user = userService.getOrCreateUser(token);
    return conversationService.createGroupConversation(user.id(), req);
  }

  @GetMapping("/{id}/messages")
  public List<ChatMessageDto> getMessages(@PathVariable UUID id,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          JwtAuthenticationToken token) {
    UserDto user = userService.getOrCreateUser(token);
    return chatMessageService.getMessages(id, user.id(), page, size);
  }

  @PostMapping("/{id}/messages")
  public ChatMessageDto sendMessage(@PathVariable UUID id,
                                    @RequestBody SendMessageRequest request,
                                    JwtAuthenticationToken token) {
    UserDto user = userService.getOrCreateUser(token);
    return chatMessageService.sendMessage(id, user.id(), request);
  }
}
