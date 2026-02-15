package dev.rabauer.lille_chat.frontend.service;

import dev.rabauer.lille_chat.frontend.dto.ConversationDto;
import dev.rabauer.lille_chat.frontend.dto.CreateGroupConversationRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@Service
public class ConversationClientService {

    private final WebClient webClient;

    public ConversationClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public ConversationDto getOrCreateDirect(UUID userId) {
        return webClient.post()
                .uri("/api/conversations/direct/{userId}", userId)
                .retrieve()
                .bodyToMono(ConversationDto.class)
                .block();
    }

    public ConversationDto createGroup(CreateGroupConversationRequest request) {
        return webClient.post()
                .uri("/api/conversations/group")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ConversationDto.class)
                .block();
    }

    public List<ConversationDto> listConversations() {
        return webClient.get()
                .uri("/api/conversations")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ConversationDto>>() {})
                .block();
    }

    public ConversationDto getConversation(UUID conversationId) {
        return webClient.get()
                .uri("/api/conversations/{id}", conversationId)
                .retrieve()
                .bodyToMono(ConversationDto.class)
                .block();
    }
}
