package dev.rabauer.lille.chat.backend.repository;

import dev.rabauer.lille.chat.backend.dto.FriendshipStatus;
import dev.rabauer.lille.chat.backend.entity.FriendshipEntity;
import dev.rabauer.lille.chat.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<FriendshipEntity, UUID> {

    @Query("SELECT f FROM FriendshipEntity f WHERE "
            + "(f.requester = :u1 AND f.addressee = :u2) OR "
            + "(f.requester = :u2 AND f.addressee = :u1)")
    List<FriendshipEntity> findByUsers(@Param("u1") UserEntity u1, @Param("u2") UserEntity u2);

    @Query("SELECT f FROM FriendshipEntity f WHERE "
            + "(f.requester = :user OR f.addressee = :user) AND f.status = :status")
    List<FriendshipEntity> findByUserAndStatus(
            @Param("user") UserEntity user, @Param("status") FriendshipStatus status);

    List<FriendshipEntity> findByAddresseeAndStatus(UserEntity addressee, FriendshipStatus status);
}
