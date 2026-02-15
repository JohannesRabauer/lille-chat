package dev.rabauer.lille.chat.backend.repository;

import dev.rabauer.lille.chat.backend.entity.ConversationParticipant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationParticipantRepository
    extends JpaRepository<ConversationParticipant, UUID> {

  List<ConversationParticipant> findByConversationId(UUID conversationId);
}
