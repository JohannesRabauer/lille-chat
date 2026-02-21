package dev.rabauer.lille.chat.backend.service;

import dev.rabauer.lille.chat.backend.dto.FriendshipDto;
import dev.rabauer.lille.chat.backend.dto.FriendshipStatus;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.entity.FriendshipEntity;
import dev.rabauer.lille.chat.backend.entity.UserEntity;
import dev.rabauer.lille.chat.backend.repository.FriendshipRepository;
import dev.rabauer.lille.chat.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    FriendshipServiceImpl(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Override
    public FriendshipDto sendRequest(UUID requesterId, UUID addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        UserEntity requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + requesterId));
        UserEntity addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + addresseeId));

        List<FriendshipEntity> existing = friendshipRepository.findByUsers(requester, addressee);
        if (!existing.isEmpty()) {
            throw new IllegalStateException("Friendship already exists between these users");
        }

        FriendshipEntity friendship = new FriendshipEntity();
        friendship.setRequester(requester);
        friendship.setAddressee(addressee);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendship.setCreatedAt(Instant.now());

        friendship = friendshipRepository.save(friendship);
        return toDto(friendship, requesterId);
    }

    @Override
    public FriendshipDto acceptRequest(UUID friendshipId, UUID userId) {
        FriendshipEntity friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Friendship not found: " + friendshipId));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the addressee can accept a friend request");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship = friendshipRepository.save(friendship);
        return toDto(friendship, userId);
    }

    @Override
    public FriendshipDto declineRequest(UUID friendshipId, UUID userId) {
        FriendshipEntity friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Friendship not found: " + friendshipId));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the addressee can decline a friend request");
        }

        friendship.setStatus(FriendshipStatus.DECLINED);
        friendship = friendshipRepository.save(friendship);
        return toDto(friendship, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendshipDto> listFriends(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        return friendshipRepository.findByUserAndStatus(user, FriendshipStatus.ACCEPTED)
                .stream()
                .map(f -> toDto(f, userId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendshipDto> listPendingRequests(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        return friendshipRepository.findByAddresseeAndStatus(user, FriendshipStatus.PENDING)
                .stream()
                .map(f -> toDto(f, userId))
                .toList();
    }

    private FriendshipDto toDto(FriendshipEntity entity, UUID perspectiveUserId) {
        UserEntity friendEntity = entity.getRequester().getId().equals(perspectiveUserId)
                ? entity.getAddressee()
                : entity.getRequester();

        UserDto friend = new UserDto(friendEntity.getId(), friendEntity.getUsername(),
                friendEntity.getDisplayName(), friendEntity.getEmail());

        return new FriendshipDto(entity.getId(), friend, entity.getStatus(), entity.getCreatedAt());
    }
}
