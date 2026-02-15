package dev.rabauer.lille.chat.backend.service.impl;

import dev.rabauer.lille.chat.backend.dto.FriendshipDto;
import dev.rabauer.lille.chat.backend.dto.FriendshipStatus;
import dev.rabauer.lille.chat.backend.dto.UserDto;
import dev.rabauer.lille.chat.backend.entity.Friendship;
import dev.rabauer.lille.chat.backend.entity.User;
import dev.rabauer.lille.chat.backend.exception.DuplicateFriendRequestException;
import dev.rabauer.lille.chat.backend.exception.FriendshipNotFoundException;
import dev.rabauer.lille.chat.backend.exception.InvalidFriendRequestException;
import dev.rabauer.lille.chat.backend.exception.UnauthorizedFriendshipActionException;
import dev.rabauer.lille.chat.backend.exception.UserNotFoundException;
import dev.rabauer.lille.chat.backend.repository.FriendshipRepository;
import dev.rabauer.lille.chat.backend.repository.UserRepository;
import dev.rabauer.lille.chat.backend.service.FriendshipService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FriendshipServiceImpl implements FriendshipService {

  private final FriendshipRepository friendshipRepository;
  private final UserRepository userRepository;

  public FriendshipServiceImpl(FriendshipRepository friendshipRepository,
                               UserRepository userRepository) {
    this.friendshipRepository = friendshipRepository;
    this.userRepository = userRepository;
  }

  @Override
  public FriendshipDto sendRequest(UUID requesterId, UUID addresseeId) {
    if (requesterId.equals(addresseeId)) {
      throw new InvalidFriendRequestException("Cannot send friend request to yourself");
    }

    if (friendshipRepository.findBetweenUsers(requesterId, addresseeId).isPresent()) {
      throw new DuplicateFriendRequestException(requesterId, addresseeId);
    }

    User requester = userRepository.findById(requesterId)
        .orElseThrow(() -> new UserNotFoundException(requesterId));
    User addressee = userRepository.findById(addresseeId)
        .orElseThrow(() -> new UserNotFoundException(addresseeId));

    Friendship friendship = new Friendship(requester, addressee);
    friendship = friendshipRepository.save(friendship);

    return toDto(friendship, requesterId);
  }

  @Override
  public FriendshipDto acceptRequest(UUID friendshipId, UUID userId) {
    Friendship friendship = friendshipRepository.findById(friendshipId)
        .orElseThrow(() -> new FriendshipNotFoundException(friendshipId));

    if (!friendship.getAddressee().getId().equals(userId)) {
      throw new UnauthorizedFriendshipActionException(userId, friendshipId);
    }

    friendship.setStatus(FriendshipStatus.ACCEPTED);
    friendship = friendshipRepository.save(friendship);

    return toDto(friendship, userId);
  }

  @Override
  public FriendshipDto declineRequest(UUID friendshipId, UUID userId) {
    Friendship friendship = friendshipRepository.findById(friendshipId)
        .orElseThrow(() -> new FriendshipNotFoundException(friendshipId));

    if (!friendship.getAddressee().getId().equals(userId)) {
      throw new UnauthorizedFriendshipActionException(userId, friendshipId);
    }

    friendship.setStatus(FriendshipStatus.DECLINED);
    friendship = friendshipRepository.save(friendship);

    return toDto(friendship, userId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<FriendshipDto> listFriends(UUID userId) {
    return friendshipRepository.findByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED).stream()
        .map(f -> toDto(f, userId))
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<FriendshipDto> listPendingRequests(UUID userId) {
    return friendshipRepository.findIncomingByUserIdAndStatus(userId, FriendshipStatus.PENDING)
        .stream()
        .map(f -> toDto(f, userId))
        .toList();
  }

  private FriendshipDto toDto(Friendship friendship, UUID currentUserId) {
    User friend = friendship.getRequester().getId().equals(currentUserId)
        ? friendship.getAddressee()
        : friendship.getRequester();

    return new FriendshipDto(
        friendship.getId(),
        toUserDto(friend),
        friendship.getStatus(),
        friendship.getCreatedAt()
    );
  }

  private UserDto toUserDto(User user) {
    return new UserDto(user.getId(), user.getUsername(), user.getDisplayName(), user.getEmail());
  }
}
