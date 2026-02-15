package dev.rabauer.lille_chat.frontend.service;

import dev.rabauer.lille_chat.frontend.dto.UserDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class UserClientService {

    private final WebClient webClient;

    public UserClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public UserDto getCurrentUser() {
        return webClient.get()
                .uri("/api/users/me")
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
    }

    public List<UserDto> searchUsers(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/users/search")
                        .queryParam("q", query)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UserDto>>() {})
                .block();
    }
}
