package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.dto.FriendshipDto;

import java.util.List;
import java.util.UUID;

public interface FriendshipService {

    FriendshipDto sendRequest(UUID requesterId, UUID addresseeId);

    FriendshipDto acceptRequest(UUID friendshipId, UUID userId);

    FriendshipDto declineRequest(UUID friendshipId, UUID userId);

    List<FriendshipDto> listFriends(UUID userId);

    List<FriendshipDto> listPendingRequests(UUID userId);
}
