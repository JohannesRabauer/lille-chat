package dev.rabauer.lille_chat.frontend.service;

import dev.rabauer.lille_chat.frontend.dto.FriendshipDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@Service
public class FriendClientService {

    private final WebClient webClient;

    public FriendClientService(WebClient webClient) {
        this.webClient = webClient;
    }

    public FriendshipDto sendRequest(UUID addresseeId) {
        return webClient.post()
                .uri("/api/friends/request/{addresseeId}", addresseeId)
                .retrieve()
                .bodyToMono(FriendshipDto.class)
                .block();
    }

    public FriendshipDto acceptRequest(UUID friendshipId) {
        return webClient.put()
                .uri("/api/friends/{id}/accept", friendshipId)
                .retrieve()
                .bodyToMono(FriendshipDto.class)
                .block();
    }

    public FriendshipDto declineRequest(UUID friendshipId) {
        return webClient.put()
                .uri("/api/friends/{id}/decline", friendshipId)
                .retrieve()
                .bodyToMono(FriendshipDto.class)
                .block();
    }

    public List<FriendshipDto> listFriends() {
        return webClient.get()
                .uri("/api/friends")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FriendshipDto>>() {})
                .block();
    }

    public List<FriendshipDto> listPendingRequests() {
        return webClient.get()
                .uri("/api/friends/requests")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<FriendshipDto>>() {})
                .block();
    }
}
