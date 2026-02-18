package dev.rabauer.lille.chat.backend.repository;

import dev.rabauer.lille.chat.backend.entity.ChatMessage;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

  Page<ChatMessage> findByConversationIdOrderBySentAtAsc(UUID conversationId, Pageable pageable);

  @Query("""
      SELECT m FROM ChatMessage m
      WHERE m.conversation.id = :conversationId
      ORDER BY m.sentAt DESC
      LIMIT 1
      """)
  Optional<ChatMessage> findLastMessageByConversationId(
      @Param("conversationId") UUID conversationId);
}
