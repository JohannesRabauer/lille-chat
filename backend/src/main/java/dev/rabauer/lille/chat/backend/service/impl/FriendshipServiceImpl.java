package dev.rabauer.lille.chat.backend.service.impl;

import dev.rabauer.lille.chat.backend.dto.FriendshipDto;
import dev.rabauer.lille.chat.backend.dto.FriendshipStatus;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.entity.FriendshipEntity;
import dev.rabauer.lille.chat.backend.entity.UserEntity;
import dev.rabauer.lille.chat.backend.repository.FriendshipRepository;
import dev.rabauer.lille.chat.backend.repository.UserRepository;
import dev.rabauer.lille.chat.backend.service.FriendshipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendshipServiceImpl(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Override
    public FriendshipDto sendRequest(UUID requesterId, UUID addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }
        if (friendshipRepository.existsByRequesterIdAndAddresseeId(requesterId, addresseeId)
                || friendshipRepository.existsByRequesterIdAndAddresseeId(addresseeId, requesterId)) {
            throw new IllegalStateException("Friendship already exists between these users");
        }

        UserEntity requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + requesterId));
        UserEntity addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + addresseeId));

        FriendshipEntity friendship = new FriendshipEntity();
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setCreatedAt(Instant.now());

        return toDto(friendshipRepository.save(friendship), requesterId);
    }

    @Override
    public FriendshipDto acceptRequest(UUID friendshipId, UUID userId) {
        FriendshipEntity friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new NoSuchElementException("Friendship not found: " + friendshipId));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the addressee can accept a friendship request");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        return toDto(friendshipRepository.save(friendship), userId);
    }

    @Override
    public FriendshipDto declineRequest(UUID friendshipId, UUID userId) {
        FriendshipEntity friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new NoSuchElementException("Friendship not found: " + friendshipId));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the addressee can decline a friendship request");
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
        return toDto(friendshipRepository.save(friendship), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendshipDto> listFriends(UUID userId) {
        return friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED)
                .stream()
                .map(f -> toDto(f, userId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendshipDto> listPendingRequests(UUID userId) {
        return friendshipRepository.findByAddresseeIdAndStatus(userId, FriendshipStatus.PENDING)
                .stream()
                .map(f -> toDto(f, userId))
                .toList();
    }

    private FriendshipDto toDto(FriendshipEntity entity, UUID currentUserId) {
        UserEntity friend = entity.getRequester().getId().equals(currentUserId)
                ? entity.getAddressee()
                : entity.getRequester();

        UserDto friendDto = new UserDto(friend.getId(), friend.getUsername(),
                friend.getDisplayName(), friend.getEmail());
        return new FriendshipDto(entity.getId(), friendDto, entity.getStatus(), entity.getCreatedAt());
    }
}
