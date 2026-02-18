package dev.rabauer.lille.chat.backend.repository;

import dev.rabauer.lille.chat.backend.dto.FriendshipStatus;
import dev.rabauer.lille.chat.backend.entity.Friendship;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

  @Query("""
      SELECT f FROM Friendship f
      WHERE (f.requester.id = :userId1 AND f.addressee.id = :userId2)
         OR (f.requester.id = :userId2 AND f.addressee.id = :userId1)
      """)
  Optional<Friendship> findBetweenUsers(@Param("userId1") UUID userId1,
                                        @Param("userId2") UUID userId2);

  @Query("""
      SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
      FROM Friendship f
      WHERE f.status = 'ACCEPTED'
        AND ((f.requester.id = :userId1 AND f.addressee.id = :userId2)
          OR (f.requester.id = :userId2 AND f.addressee.id = :userId1))
      """)
  boolean areFriends(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);

  @Query("""
      SELECT f FROM Friendship f
      WHERE f.status = :status
        AND (f.requester.id = :userId OR f.addressee.id = :userId)
      """)
  List<Friendship> findByUserIdAndStatus(@Param("userId") UUID userId,
                                         @Param("status") FriendshipStatus status);

  @Query("""
      SELECT f FROM Friendship f
      WHERE f.status = :status AND f.addressee.id = :userId
      """)
  List<Friendship> findIncomingByUserIdAndStatus(@Param("userId") UUID userId,
                                                  @Param("status") FriendshipStatus status);
}
