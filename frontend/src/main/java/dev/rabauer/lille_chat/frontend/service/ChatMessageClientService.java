package dev.rabauer.lille_chat.frontend.service;

import dev.rabauer.lille_chat.frontend.dto.ChatMessageDto;
import dev.rabauer.lille_chat.frontend.dto.SendMessageRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@Service
public class ChatMessageClientService {

    private final WebClient webClient;

    public ChatMessageClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public ChatMessageDto sendMessage(UUID conversationId, SendMessageRequest request) {
        return webClient.post()
                .uri("/api/conversations/{id}/messages", conversationId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatMessageDto.class)
                .block();
    }

    public List<ChatMessageDto> getMessages(UUID conversationId, int page, int size) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/conversations/{id}/messages")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(conversationId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ChatMessageDto>>() {})
                .block();
    }
}
