package dev.rabauer.lille.chat.backend.repository;

import dev.rabauer.lille.chat.backend.dto.ConversationType;
import dev.rabauer.lille.chat.backend.entity.Conversation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

  @Query("""
      SELECT c FROM Conversation c
      WHERE c.type = :type
      AND EXISTS (SELECT p1 FROM ConversationParticipant p1 WHERE p1.conversation = c
        AND p1.user.id = :userId1)
      AND EXISTS (SELECT p2 FROM ConversationParticipant p2 WHERE p2.conversation = c
        AND p2.user.id = :userId2)
      AND (SELECT COUNT(p) FROM ConversationParticipant p WHERE p.conversation = c) = 2
      """)
  Optional<Conversation> findDirectConversation(
      @Param("userId1") UUID userId1,
      @Param("userId2") UUID userId2,
      @Param("type") ConversationType type);

  @Query("""
      SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
      FROM ConversationParticipant p
      WHERE p.conversation.id = :conversationId AND p.user.id = :userId
      """)
  boolean isParticipant(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);

  @Query("""
      SELECT DISTINCT c FROM Conversation c
      JOIN c.participants p
      WHERE p.user.id = :userId
      """)
  List<Conversation> findByParticipantUserId(@Param("userId") UUID userId);
}
