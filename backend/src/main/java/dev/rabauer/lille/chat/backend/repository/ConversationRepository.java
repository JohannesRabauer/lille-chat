package dev.rabauer.lille.chat.backend.repository;

import dev.rabauer.lille.chat.backend.dto.ConversationType;
import dev.rabauer.lille.chat.backend.entity.ConversationEntity;
import dev.rabauer.lille.chat.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    @Query("SELECT c FROM ConversationEntity c JOIN c.participants p WHERE p = :user")
    List<ConversationEntity> findByParticipant(@Param("user") UserEntity user);

    @Query("SELECT c FROM ConversationEntity c "
            + "WHERE c.type = :type "
            + "AND :u1 MEMBER OF c.participants "
            + "AND :u2 MEMBER OF c.participants "
            + "AND SIZE(c.participants) = 2")
    Optional<ConversationEntity> findDirectConversation(
            @Param("type") ConversationType type,
            @Param("u1") UserEntity u1,
            @Param("u2") UserEntity u2);
}
