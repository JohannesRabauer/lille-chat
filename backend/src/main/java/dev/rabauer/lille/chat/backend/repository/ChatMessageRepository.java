package dev.rabauer.lille.chat.backend.repository;

import dev.rabauer.lille.chat.backend.entity.ChatMessageEntity;
import dev.rabauer.lille.chat.backend.entity.ConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, UUID> {

    Page<ChatMessageEntity> findByConversationOrderBySentAtAsc(
            ConversationEntity conversation, Pageable pageable);

    Optional<ChatMessageEntity> findTopByConversationOrderBySentAtDesc(
            ConversationEntity conversation);
}
