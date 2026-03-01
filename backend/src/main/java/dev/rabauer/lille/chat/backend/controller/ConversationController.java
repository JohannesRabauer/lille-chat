package dev.rabauer.lille.chat.backend.controller;

import dev.rabauer.lille.chat.backend.dto.ChatMessageDto;
import dev.rabauer.lille.chat.backend.dto.ConversationDto;
import dev.rabauer.lille.chat.backend.dto.CreateGroupConversationRequest;
import dev.rabauer.lille.chat.backend.dto.SendMessageRequest;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.service.ChatMessageService;
import dev.rabauer.lille.chat.backend.service.ConversationService;
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

    @PostMapping("/direct/{userId}")
    public ConversationDto getOrCreateDirect(@PathVariable UUID userId, JwtAuthenticationToken token) {
        UserDto currentUser = userService.getOrCreateUser(token);
        return conversationService.getOrCreateDirectConversation(currentUser.id(), userId);
    }

    @PostMapping("/group")
    public ConversationDto createGroup(@RequestBody CreateGroupConversationRequest request,
                                       JwtAuthenticationToken token) {
        UserDto currentUser = userService.getOrCreateUser(token);
        return conversationService.createGroupConversation(currentUser.id(), request);
    }

    @GetMapping
    public List<ConversationDto> listConversations(JwtAuthenticationToken token) {
        UserDto currentUser = userService.getOrCreateUser(token);
        return conversationService.getConversationsForUser(currentUser.id());
    }

    @GetMapping("/{id}")
    public ConversationDto getConversation(@PathVariable UUID id, JwtAuthenticationToken token) {
        UserDto currentUser = userService.getOrCreateUser(token);
        return conversationService.getConversation(id, currentUser.id());
    }

    @PostMapping("/{id}/messages")
    public ChatMessageDto sendMessage(@PathVariable UUID id,
                                      @RequestBody SendMessageRequest request,
                                      JwtAuthenticationToken token) {
        UserDto currentUser = userService.getOrCreateUser(token);
        return chatMessageService.sendMessage(id, currentUser.id(), request);
    }

    @GetMapping("/{id}/messages")
    public List<ChatMessageDto> getMessages(@PathVariable UUID id,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "50") int size,
                                            JwtAuthenticationToken token) {
        UserDto currentUser = userService.getOrCreateUser(token);
        return chatMessageService.getMessages(id, currentUser.id(), page, size);
    }
}
