package dev.rabauer.lille.chat.backend.dto;

import java.util.List;
import java.util.UUID;

public record CreateGroupConversationRequest(
        String name,
        List<UUID> participantIds
) {
}
