package dev.rabauer.lille.chat.backend.repository;

import dev.rabauer.lille.chat.backend.entity.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

    Page<ChatMessageEntity> findByConversationIdOrderBySentAtAsc(UUID conversationId, Pageable pageable);
}
