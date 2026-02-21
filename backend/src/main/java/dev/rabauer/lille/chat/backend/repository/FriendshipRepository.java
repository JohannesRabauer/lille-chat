package dev.rabauer.lille.chat.backend.repository;

import dev.rabauer.lille.chat.backend.dto.FriendshipStatus;
import dev.rabauer.lille.chat.backend.entity.FriendshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<FriendshipEntity, UUID> {

    boolean existsByRequesterIdAndAddresseeId(UUID requesterId, UUID addresseeId);

    List<FriendshipEntity> findByAddresseeIdAndStatus(UUID addresseeId, FriendshipStatus status);

    @Query("SELECT f FROM FriendshipEntity f WHERE f.status = :status AND (f.requester.id = :userId OR f.addressee.id = :userId)")
    List<FriendshipEntity> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") FriendshipStatus status);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END FROM FriendshipEntity f " +
            "WHERE f.status = :status AND " +
            "((f.requester.id = :userId1 AND f.addressee.id = :userId2) OR " +
            "(f.requester.id = :userId2 AND f.addressee.id = :userId1))")
    boolean existsByUsersAndStatus(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2,
            @Param("status") FriendshipStatus status);
}
