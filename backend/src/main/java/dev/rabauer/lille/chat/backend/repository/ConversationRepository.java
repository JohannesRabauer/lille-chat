package dev.rabauer.lille.chat.backend.repository;

import dev.rabauer.lille.chat.backend.dto.ConversationType;
import dev.rabauer.lille.chat.backend.entity.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    @Query("SELECT DISTINCT c FROM ConversationEntity c JOIN c.participants p WHERE p.id = :userId")
    List<ConversationEntity> findByParticipantId(@Param("userId") UUID userId);

    @Query("SELECT c FROM ConversationEntity c WHERE c.type = :type AND SIZE(c.participants) = 2 " +
            "AND EXISTS (SELECT p FROM c.participants p WHERE p.id = :userId1) " +
            "AND EXISTS (SELECT p FROM c.participants p WHERE p.id = :userId2)")
    Optional<ConversationEntity> findDirectConversation(@Param("userId1") UUID userId1,
            @Param("userId2") UUID userId2, @Param("type") ConversationType type);
}
